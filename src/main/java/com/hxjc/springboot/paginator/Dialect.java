package com.hxjc.springboot.paginator;


import com.hxyc.common.util.StringUtil;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @ClassName Dialect
 * @Description TODO
 * @Author admin
 * @Date 2019/10/17 18:05
 **/
public class Dialect {
    protected MappedStatement mappedStatement;
    protected Object parameterObject;
    protected Page page;
    protected BoundSql boundSql;
    protected List<ParameterMapping> parameterMappings;
    private String pageSQL;
    private String countSQL;

    public Dialect(MappedStatement mappedStatement, Object parameterObject, Page page) {
        this.mappedStatement = mappedStatement;
        this.parameterObject = parameterObject;
        this.page = page;
        this.init(mappedStatement, parameterObject, page);
    }

    public void init(MappedStatement mappedStatement, Object parameterObject, Page page) {
        this.boundSql = mappedStatement.getBoundSql(parameterObject);
        this.parameterMappings = new ArrayList(this.boundSql.getParameterMappings());
        String sql = this.boundSql.getSql().trim();
        if(sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1);
        }

        this.pageSQL = sql;
        if(!CollectionUtils.isEmpty(page.getOrders())) {
            this.pageSQL = this.getSortString(sql, page.getOrders());
        }

        if(page.getLimit() != 2147483647) {
            this.pageSQL = this.getLimitString(this.pageSQL, page.getOffset(), page.getLimit());
        }

        this.countSQL = this.getCountString(sql);
    }

    public List<ParameterMapping> getParameterMappings() {
        return this.parameterMappings;
    }

    public Object getParameterObject() {
        return this.parameterObject;
    }

    public String getPageSQL() {
        return this.pageSQL;
    }

    public String getCountSQL() {
        return this.countSQL;
    }

    protected String getLimitString(String sql, int offset, int limit) {
        throw new UnsupportedOperationException("paged queries not supported");
    }

    protected String getCountString(String sql) {
        if(StringUtil.isEmpty(sql)) {
            return sql;
        } else {
            String[] sqlArr = sql.replaceAll("\n", "").split(" from ");
            if(sqlArr.length <= 1) {
                return sql;
            } else {
                List<String> sqlList = new ArrayList();
                sqlList.add("select count(1)");

                for(int i = 1; i < sqlArr.length; ++i) {
                    sqlList.add(sqlArr[i]);
                }

                return StringUtil.join(sqlList, " from ");
            }
        }
    }

    protected String getSortString(String sql, List<Order> orders) {
        if(orders != null && !orders.isEmpty()) {
            StringBuffer buffer;
            if(sql.contains(" order by")) {
                buffer = (new StringBuffer("select * from (")).append(sql).append(") temp_order order by ");
            } else {
                buffer = new StringBuffer(sql + " order by ");
            }

            Iterator var4 = orders.iterator();

            while(var4.hasNext()) {
                Order order = (Order)var4.next();
                if(order != null) {
                    buffer.append(order.toString()).append(", ");
                }
            }

            buffer.delete(buffer.length() - 2, buffer.length());
            return buffer.toString();
        } else {
            return sql;
        }
    }
}
