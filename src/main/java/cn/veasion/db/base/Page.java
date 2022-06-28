package cn.veasion.db.base;

import java.util.List;

/**
 * Page
 *
 * @author luozhuowei
 * @date 2021/12/10
 */
public class Page<T> {

    private int page;
    private int size;
    private long count;
    private List<T> list;

    public Page() {
    }

    public Page(int page, int size, long count, List<T> list) {
        this.count = count;
        this.list = list;
    }

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

    public long getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

}
