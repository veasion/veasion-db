package cn.veasion.db.query;

import cn.veasion.db.base.Expression;
import cn.veasion.db.base.Filter;
import cn.veasion.db.utils.FilterUtils;

/**
 * SubQuery
 *
 * @author luozhuowei
 * @date 2021/12/10
 */
public class SubQuery extends AbstractQuery<SubQuery> {

    private String tableAs;
    private AbstractQuery<?> subQuery;

    public SubQuery(AbstractQuery<?> subQuery) {
        this(subQuery, null);
    }

    public SubQuery(AbstractQuery<?> subQuery, String alias) {
        this.subQuery = subQuery;
        this.tableAs = alias == null || "".equals(alias) ? "t" : alias;
    }

    public String getTableAs() {
        return tableAs;
    }

    public AbstractQuery<?> getSubQuery() {
        return subQuery;
    }

    @Override
    public SubQuery selectExpression(Expression expression) {
        return selectExpression(expression, true);
    }

    public SubQuery selectExpression(Expression expression, boolean hasTableAs) {
        if (expression == null) {
            return this;
        }
        return super.selectExpression(hasTableAs ? expression.tableAs(tableAs) : expression);
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

    @Override
    protected String handleField(String field) {
        return FilterUtils.tableAsField(tableAs, field);
    }

    @Override
    protected Filter handleFilter(Filter filter) {
        return filter.fieldAs(tableAs);
    }

    @Override
    public void check() {
        boolean _selectAll = selectAll;
        if (subQuery.getEntityClass() == null) {
            subQuery.setEntityClass(getEntityClass());
        }
        subQuery.check();
        selectAll = false;
        super.check();
        boolean emptySelect = getSelects().isEmpty() && getSelectExpression() == null;
        if (emptySelect || _selectAll) {
            selects.add(0, handleField("*"));
        }
    }

}
