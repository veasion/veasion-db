package cn.veasion.db.query;

import cn.veasion.db.base.Expression;
import cn.veasion.db.base.Filter;

/**
 * SubQuery
 *
 * @author luozhuowei
 * @date 2021/12/10
 */
public class SubQuery extends AbstractJoinQuery<SubQuery> {

    private AbstractQuery<?> subQuery;

    public SubQuery(AbstractQuery<?> subQuery, String alias) {
        this.tableAs = alias == null || "".equals(alias) ? "t" : alias;
        this.subQuery = subQuery;
    }

    public AbstractQuery<?> getSubQuery() {
        return subQuery;
    }

    public SubQuery realSelect(String field, String alias) {
        selects.add(field);
        if (alias != null) {
            aliasMap.put(field, alias);
        }
        return this;
    }

    public SubQuery realFilter(Filter filter) {
        addFilter(filter);
        filter.fieldAs("-");
        return this;
    }

    public SubQuery selectExpression(Expression expression, boolean hasTableAs) {
        if (expression == null) {
            return this;
        }
        return super.selectExpression(hasTableAs ? expression.tableAs(getTableAs()) : expression);
    }

    @Override
    public void check(Class<?> mainEntityClass) {
        boolean _selectAll = selectAll;
        subQuery.check(mainEntityClass);
        selectAll = false;
        super.check(mainEntityClass);
        if (_selectAll || (getSelects().isEmpty() && getSelectExpression() == null && getSelectSubQueryList() == null)) {
            selects.add(0, handleField("*"));
        }
    }

    @Override
    protected void check(Class<?> mainEntityClass, AbstractJoinQuery<?> mainQuery, boolean isMain) {
        subQuery.check(mainEntityClass);
        super.check(mainEntityClass, mainQuery, isMain);
    }

    @Override
    protected boolean isEmptySelects() {
        return false;
    }

    @Override
    protected SubQuery getSelf() {
        return this;
    }

}
