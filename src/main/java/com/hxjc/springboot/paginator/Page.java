package com.hxjc.springboot.paginator;


import org.apache.ibatis.session.RowBounds;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName Page
 * @Description TODO
 * @Author admin
 * @Date 2019/10/17 18:07
 **/
public class Page extends RowBounds implements Serializable {
    private Integer currentPage;
    private Integer pageSize;
    private Integer totalRecord;
    private Integer totalPage;
    protected boolean containsTotalCount;
    protected Boolean asyncTotalCount;
    protected List<Order> orders;

    public Page() {
        this.orders = new ArrayList();
    }

    public Page(int currentPage, int pageSize) {
        this(currentPage, pageSize, (List)null, true);
    }

    public Page(List<Order> orders) {
        this(0, 2147483647, orders, true);
    }

    public Page(int currentPage, int pageSize, boolean containsTotalCount) {
        this(currentPage, pageSize, (List)null, containsTotalCount);
    }

    public Page(int currentPage, int pageSize, List<Order> orders) {
        this(currentPage, pageSize, orders, true);
    }

    public Page(int currentPage, int pageSize, List<Order> orders, boolean containsTotalCount) {
        super((currentPage - 1) * pageSize, pageSize);
        this.orders = new ArrayList();
        this.currentPage = Integer.valueOf(currentPage);
        this.pageSize = Integer.valueOf(pageSize);
        this.orders = orders;
        this.containsTotalCount = containsTotalCount;
    }

    public Page(RowBounds rowBounds) {
        this.orders = new ArrayList();
        this.currentPage = Integer.valueOf(rowBounds.getOffset() / rowBounds.getLimit() + 1);
        this.pageSize = Integer.valueOf(rowBounds.getLimit());
        this.containsTotalCount = true;
    }

    public Page(RowBounds rowBounds, List<Order> orders) {
        this.orders = new ArrayList();
        this.currentPage = Integer.valueOf(rowBounds.getOffset() / rowBounds.getLimit() + 1);
        this.pageSize = Integer.valueOf(rowBounds.getLimit());
        this.orders = orders;
        this.containsTotalCount = true;
    }

    private void refreshPage() {
        this.totalPage = Integer.valueOf(this.totalRecord.intValue() % this.pageSize.intValue() == 0?this.totalRecord.intValue() / this.pageSize.intValue():this.totalRecord.intValue() / this.pageSize.intValue() + 1);
        if(this.currentPage.intValue() > this.totalPage.intValue()) {
            this.currentPage = this.totalPage;
        }

    }

    public Integer getCurrentPage() {
        return this.currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getPageSize() {
        return this.pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getTotalRecord() {
        return this.totalRecord;
    }

    public void setTotalRecord(Integer totalRecord) {
        this.totalRecord = totalRecord;
        this.refreshPage();
    }

    public Integer getTotalPage() {
        return this.totalPage;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }

    public Boolean getAsyncTotalCount() {
        return this.asyncTotalCount;
    }

    public void setAsyncTotalCount(Boolean asyncTotalCount) {
        this.asyncTotalCount = asyncTotalCount;
    }

    public List<Order> getOrders() {
        return this.orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public boolean isContainsTotalCount() {
        return this.containsTotalCount;
    }

    public void setContainsTotalCount(boolean containsTotalCount) {
        this.containsTotalCount = containsTotalCount;
    }

    private static int computePageNumber(int page, int pageSize, int totalItems) {
        return page <= 1?1:(2147483647 != page && page <= computeLastPageNumber(totalItems, pageSize)?page:computeLastPageNumber(totalItems, pageSize));
    }

    private static int computeLastPageNumber(int totalItems, int pageSize) {
        if(pageSize <= 0) {
            return 1;
        } else {
            int result = totalItems % pageSize == 0?totalItems / pageSize:totalItems / pageSize + 1;
            if(result <= 1) {
                result = 1;
            }

            return result;
        }
    }
}

