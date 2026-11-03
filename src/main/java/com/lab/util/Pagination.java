package com.lab.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用分页结果封装。
 *
 * @param <T> 列表元素类型
 */
public class Pagination<T> {

    /** 默认每页条数。 */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /** 每页条数上限，防止 pageSize 被恶意放大拖垮数据库。 */
    public static final int MAX_PAGE_SIZE = 200;

    private int pageNum = 1;
    private int pageSize = DEFAULT_PAGE_SIZE;
    private int total = 0;
    private List<T> data = new ArrayList<>();

    /** 无参构造，页码 1、每页 10 条。 */
    public Pagination() {
    }

    /**
     * 指定页码与每页条数构造。
     *
     * @param pageNum  页码，小于 1 时按 1 处理
     * @param pageSize 每页条数，越界时自动收敛
     */
    public Pagination(int pageNum, int pageSize) {
        setPageNum(pageNum);
        setPageSize(pageSize);
    }

    public int getPageNum() {
        return pageNum;
    }

    /**
     * 设置页码，小于 1 时按 1 处理。
     *
     * @param pageNum 页码
     */
    public void setPageNum(int pageNum) {
        this.pageNum = pageNum < 1 ? 1 : pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    /**
     * 设置每页条数，小于 1 取默认值，大于上限取上限。
     *
     * @param pageSize 每页条数
     */
    public void setPageSize(int pageSize) {
        if (pageSize < 1) {
            this.pageSize = DEFAULT_PAGE_SIZE;
        } else if (pageSize > MAX_PAGE_SIZE) {
            this.pageSize = MAX_PAGE_SIZE;
        } else {
            this.pageSize = pageSize;
        }
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total < 0 ? 0 : total;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data == null ? new ArrayList<>() : data;
    }

    /** @return 总页数，总数为 0 时返回 1 */
    public int getTotalPages() {
        if (total <= 0) {
            return 1;
        }
        return (int) Math.ceil(total * 1.0 / pageSize);
    }

    /** @return 总页数别名，与页面变量 pageTotal 对齐 */
    public int getPageTotal() {
        return getTotalPages();
    }

    /** @return SQL LIMIT 偏移量 */
    public int getStartIndex() {
        return (pageNum - 1) * pageSize;
    }

    /** @return SQL LIMIT 偏移量（getStartIndex 的别名） */
    public int getOffset() {
        return getStartIndex();
    }

    public boolean isHasPrev() {
        return pageNum > 1;
    }

    public boolean isHasNext() {
        return pageNum < getTotalPages();
    }
}
