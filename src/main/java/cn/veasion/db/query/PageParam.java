package cn.veasion.db.query;

import cn.veasion.db.utils.ISort;

import java.util.List;

/**
 * PageParam
 *
 * @author luozhuowei
 * @date 2021/12/8
 */
public abstract class PageParam implements ISort {

    protected int page = 1;
    protected int size = 10;

    public PageParam() {
    }

    public PageParam(int page, int size) {
        this.page = page;
        this.size = size;
    }

    public abstract void handleSqlValue(StringBuilder sql, List<Object> values);

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

}
