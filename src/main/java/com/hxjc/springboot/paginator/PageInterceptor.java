package com.hxjc.springboot.paginator;



import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;

/**
 * @ClassName PageInterceptor
 * @Description TODO
 * @Author admin
 * @Date 2019/10/17 18:07
 **/
@Intercepts({@Signature(
        type = Executor.class,
        method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
)})
public class PageInterceptor implements Interceptor {
    private static Logger logger = Logger.getLogger(PageInterceptor.class);
    private static int MAPPED_STATEMENT_INDEX = 0;
    private static int PARAMETER_INDEX = 1;
    private static int ROW_BOUNDS_INDEX = 2;
    static ExecutorService Pool;
    String dialectStr;
    boolean asyncTotalCount;
    int poolMaxSize;

    public PageInterceptor() {
    }

    public void setDialectStr(String dialectStr) {
        this.dialectStr = dialectStr;
    }

    public void setAsyncTotalCount(boolean asyncTotalCount) {
        this.asyncTotalCount = asyncTotalCount;
    }

    public void setPoolMaxSize(int poolMaxSize) {
        this.poolMaxSize = poolMaxSize;
    }

    public void setProperties(Properties properties) {
        this.dialectStr = properties.getProperty("dialectStr");
    }

    public void init() {
        if(this.poolMaxSize > 0) {
            logger.debug("poolMaxSize: " + this.poolMaxSize);
            Pool = Executors.newFixedThreadPool(this.poolMaxSize);
        } else {
            Pool = Executors.newCachedThreadPool();
        }

    }

    public Object intercept(final Invocation invocation) throws Throwable {
        final Executor executor = (Executor)invocation.getTarget();
        Object[] queryArgs = invocation.getArgs();
        final MappedStatement ms = (MappedStatement)queryArgs[MAPPED_STATEMENT_INDEX];
        final Object parameter = queryArgs[PARAMETER_INDEX];
        RowBounds rowBounds = (RowBounds)queryArgs[ROW_BOUNDS_INDEX];
       Page page;
        if(rowBounds instanceof Page) {
            page = (Page)rowBounds;
        } else {
            page = new Page(rowBounds);
        }

        final Dialect dialect;
        try {
            Class clazz = Class.forName(this.dialectStr);
            Constructor constructor = clazz.getConstructor(new Class[]{MappedStatement.class, Object.class, Page.class});
            dialect = (Dialect)constructor.newInstance(new Object[]{ms, parameter, page});
        } catch (Exception var14) {
            throw new ClassNotFoundException("Cannot create dialect instance: " + page, var14);
        }

        final BoundSql boundSql = ms.getBoundSql(parameter);
        queryArgs[MAPPED_STATEMENT_INDEX] = this.copyFromNewSql(ms, boundSql, dialect.getPageSQL(), dialect.getParameterMappings(), dialect.getParameterObject());
        queryArgs[PARAMETER_INDEX] = dialect.getParameterObject();
        queryArgs[ROW_BOUNDS_INDEX] = new RowBounds(0, 2147483647);
        Boolean async = Boolean.valueOf(page.getAsyncTotalCount() == null?this.asyncTotalCount:page.getAsyncTotalCount().booleanValue());
        Future<List> list = this.call(new Callable<List>() {
            public List call() throws Exception {
                return (List)invocation.proceed();
            }
        }, async.booleanValue());
        if(page.isContainsTotalCount()) {
            Callable<Integer> countTask = new Callable() {
                public Object call() throws Exception {
                    Cache cache = ms.getCache();
                    Integer count;
                    if(cache != null && ms.isUseCache()) {
                        CacheKey cacheKey = executor.createCacheKey(ms, parameter, new Page(), PageInterceptor.this.copyFromBoundSql(ms, boundSql, dialect.getCountSQL(), boundSql.getParameterMappings(), boundSql.getParameterObject()));
                        count = (Integer)cache.getObject(cacheKey);
                        if(count == null) {
                            count = Integer.valueOf(PageInterceptor.this.getCount(ms, parameter, boundSql, dialect));
                            cache.putObject(cacheKey, count);
                        }
                    } else {
                        count = Integer.valueOf(PageInterceptor.this.getCount(ms, parameter, boundSql, dialect));
                    }

                    return count;
                }
            };
            Future<Integer> countFutrue = this.call(countTask, async.booleanValue());
            page.setTotalRecord((Integer)countFutrue.get());
        }

        return list.get();
    }

    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    private <T> Future<T> call(Callable callable, boolean async) {
        if(async) {
            return Pool.submit(callable);
        } else {
            FutureTask<T> future = new FutureTask(callable);
            future.run();
            return future;
        }
    }

    private MappedStatement copyFromNewSql(MappedStatement ms, BoundSql boundSql, String sql, List<ParameterMapping> parameterMappings, Object parameter) {
        BoundSql newBoundSql = this.copyFromBoundSql(ms, boundSql, sql, parameterMappings, parameter);
        return this.copyFromMappedStatement(ms, new BoundSqlSqlSource(newBoundSql));
    }

    private BoundSql copyFromBoundSql(MappedStatement ms, BoundSql boundSql, String sql, List<ParameterMapping> parameterMappings, Object parameter) {
        BoundSql newBoundSql = new BoundSql(ms.getConfiguration(), sql, parameterMappings, parameter);
        Iterator var7 = boundSql.getParameterMappings().iterator();

        while(var7.hasNext()) {
            ParameterMapping mapping = (ParameterMapping)var7.next();
            String prop = mapping.getProperty();
            if(boundSql.hasAdditionalParameter(prop)) {
                newBoundSql.setAdditionalParameter(prop, boundSql.getAdditionalParameter(prop));
            }
        }

        return newBoundSql;
    }

    private MappedStatement copyFromMappedStatement(MappedStatement ms, SqlSource newSqlSource) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId(), newSqlSource, ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        if(ms.getKeyProperties() != null && ms.getKeyProperties().length != 0) {
            StringBuffer keyProperties = new StringBuffer();
            String[] var5 = ms.getKeyProperties();
            int var6 = var5.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                String keyProperty = var5[var7];
                keyProperties.append(keyProperty).append(",");
            }

            keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
            builder.keyProperty(keyProperties.toString());
        }

        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        builder.resultMaps(ms.getResultMaps());
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());
        return builder.build();
    }

    public int getCount(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql, Dialect dialect) throws SQLException {
        String count_sql = dialect.getCountSQL();
        logger.debug("Total count SQL [" + count_sql + "]");
        logger.debug("Total count Parameters: " + parameterObject);
        Connection connection = null;
        PreparedStatement countStmt = null;
        ResultSet rs = null;

        int var11;
        try {
            connection = mappedStatement.getConfiguration().getEnvironment().getDataSource().getConnection();
            countStmt = connection.prepareStatement(count_sql);
            DefaultParameterHandler handler = new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
            handler.setParameters(countStmt);
            rs = countStmt.executeQuery();
            int count = 0;
            if(rs.next()) {
                count = rs.getInt(1);
            }

            logger.debug("Total count: " + count);
            var11 = count;
        } finally {
            try {
                if(rs != null) {
                    rs.close();
                }
            } finally {
                try {
                    if(countStmt != null) {
                        countStmt.close();
                    }
                } finally {
                    if(connection != null && !connection.isClosed()) {
                        connection.close();
                    }

                }

            }

        }

        return var11;
    }

    public static class BoundSqlSqlSource implements SqlSource {
        BoundSql boundSql;

        public BoundSqlSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }

        public BoundSql getBoundSql(Object parameterObject) {
            return this.boundSql;
        }
    }
}

