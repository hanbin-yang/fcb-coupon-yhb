package com.fcb.coupon.backend.remote.dto;

/**
 * 分页
 *
 * @Author Weihaiqi
 * @Date 2021-06-16 20:24
 **/
public class Pagination {

    private int currentPage;
    private int itemsPerPage;

    public Pagination() {
    }

    public int getStartItem() {
        int start = (this.currentPage - 1) * this.itemsPerPage;
        if (start < 0) {
            start = 0;
        }

        return start;
    }

    public static int getStartItem(int currentPage, int itemsPerPage) {
        int start = (currentPage - 1) * itemsPerPage;
        if (start < 0) {
            start = 0;
        }

        return start;
    }

    public int getCurrentPage() {
        return this.currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getItemsPerPage() {
        return this.itemsPerPage;
    }

    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }
}
