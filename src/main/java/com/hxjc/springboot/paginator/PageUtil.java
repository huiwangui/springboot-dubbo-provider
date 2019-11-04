package com.hxjc.springboot.paginator;


import com.hxyc.common.page.Paginator;

/**
 * @ClassName PageUtil
 * @Description TODO
 * @Author admin
 * @Date 2019/10/17 18:08
 **/
public class PageUtil {
    private static int DEFAULT_PAGE_SIZE = 10;

    public PageUtil() {
    }

    public static Page transformToPage(Paginator paginator) {
        new Page();
        if(paginator.getCurrentPage() == null || paginator.getCurrentPage().intValue() <= 0) {
            paginator.setCurrentPage(Integer.valueOf(1));
        }

        if(paginator.getPageSize() == null || paginator.getPageSize().intValue() <= 0) {
            paginator.setPageSize(Integer.valueOf(DEFAULT_PAGE_SIZE));
        }

        return new Page(paginator.getCurrentPage().intValue(), paginator.getPageSize().intValue(), Order.formString(paginator.getOrderSegment()));
    }

    public static Paginator transformToPaginator(Page page) {
        Paginator paginator = new Paginator();
        paginator.setCurrentPage(page.getCurrentPage());
        paginator.setPageSize(page.getPageSize());
        paginator.setTotalPage(page.getTotalPage());
        paginator.setTotalRecord(page.getTotalRecord());
        return paginator;
    }
}
