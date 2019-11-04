package com.hxjc.springboot.paginator;


import org.apache.ibatis.mapping.MappedStatement;

/**
 * @ClassName MySQLDialect
 * @Description TODO
 * @Author admin
 * @Date 2019/10/17 18:05
 **/
public class MySQLDialect extends Dialect {
    public MySQLDialect(MappedStatement mappedStatement, Object parameterObject, Page page) {
        super(mappedStatement, parameterObject, page);
    }

    protected String getLimitString(String sql, int offset, int limit) {
        StringBuffer buffer = new StringBuffer(sql);
        buffer.append(" limit " + offset + ", " + limit + "");
        return buffer.toString();
    }
}
