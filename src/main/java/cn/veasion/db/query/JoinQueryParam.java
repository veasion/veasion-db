package cn.veasion.db.query;

import cn.veasion.db.AbstractFilter;
import cn.veasion.db.base.Expression;
import cn.veasion.db.base.Filter;
import cn.veasion.db.base.JoinType;
import cn.veasion.db.utils.FilterUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * JoinQueryParam
 *
 * @author luozhuowei
 * @date 2021/12/2
 */
public class JoinQueryParam {

    private JoinType joinType;
    private EntityQuery mainQuery;
    private EntityQuery joinQuery;
    private List<Filter> onFilters;

    public JoinQueryParam(EntityQuery mainQuery, JoinType joinType, EntityQuery joinQuery) {
        this.joinType = joinType;
        this.mainQuery = mainQuery;
        this.joinQuery = joinQuery;
    }

    public JoinQueryParam on(String mainField, String joinField) {
        mainField = FilterUtils.tableAsField(mainQuery.getTableAs(), mainField);
        joinField = FilterUtils.tableAsField(joinQuery.getTableAs(), joinField);
        return on(Filter.expression(mainField, Filter.Operator.EQ, Expression.filter("${" + joinField + "}")));
    }

    public JoinQueryParam on(Filter filter) {
        if (onFilters == null) onFilters = new ArrayList<>();
        onFilters.add(filter);
        AbstractFilter.checkFilter(onFilters, false);
        return this;
    }

    public JoinType getJoinType() {
        return joinType;
    }

    public EntityQuery getMainQuery() {
        return mainQuery;
    }

    public EntityQuery getJoinQuery() {
        return joinQuery;
    }

    public List<Filter> getOnFilters() {
        return onFilters;
    }

}
