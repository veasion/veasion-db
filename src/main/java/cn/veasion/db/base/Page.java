package cn.veasion.db.base;

import java.util.List;

/**
 * Page
 *
 * @author luozhuowei
 * @date 2021/12/10
 */
public class Page<T> {

    private int count;
    private List<T> list;

    public Page() {
    }

    public Page(int count, List<T> list) {
        this.count = count;
        this.list = list;
    }

    public int getCount() {
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
