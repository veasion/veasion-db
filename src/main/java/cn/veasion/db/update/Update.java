package cn.veasion.db.update;

import cn.veasion.db.base.Filter;

/**
 * Update
 *
 * @author luozhuowei
 * @date 2021/12/3
 */
public class Update extends AbstractUpdate<Update> {

    public Update() {
    }

    public Update(String field, Object value) {
        update(field, value);
    }

    public Update(String field1, Object value1, String field2, Object value2) {
        update(field1, value1).update(field2, value2);
    }

    @Override
    protected Filter handleFilter(Filter filter) {
        return filter;
    }

    @Override
    protected String handleField(String field) {
        return field;
    }

    @Override
    protected Update getSelf() {
        return this;
    }

}
