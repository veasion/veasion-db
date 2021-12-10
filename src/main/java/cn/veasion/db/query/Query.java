package cn.veasion.db.query;

import cn.veasion.db.base.Filter;

/**
 * Query
 *
 * @author luozhuowei
 * @date 2021/12/2
 */
public class Query extends AbstractQuery<Query> {

    public Query() {
    }

    public Query(String... fields) {
        selects(fields);
    }

    @Override
    protected String handleField(String field) {
        return field;
    }

    @Override
    protected Filter handleFilter(Filter filter) {
        return filter;
    }

    @Override
    public void check() {
        boolean emptySelect = getSelects().isEmpty() && getSelectExpression() == null && !isSelectAll();
        if (emptySelect) {
            super.selectAll();
        }
        super.check();
    }

    @Override
    protected Query getSelf() {
        return this;
    }

}
