package cn.veasion.db.query;

import cn.veasion.db.AbstractFilter;
import cn.veasion.db.base.Expression;
import cn.veasion.db.base.Filter;
import cn.veasion.db.base.JoinTypeEnum;
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

    private boolean subJoinQuery;
    private JoinTypeEnum joinType;
    private EntityQuery mainEntityQuery;
    private EntityQuery joinEntityQuery;
    private List<Filter> onFilters;

    public JoinQueryParam(EntityQuery mainEntityQuery, JoinTypeEnum joinType, EntityQuery joinEntityQuery) {
        this.joinType = joinType;
        this.mainEntityQuery = mainEntityQuery;
        this.joinEntityQuery = joinEntityQuery;
    }

    public JoinQueryParam on(Filter filter) {
        onFilters.add(filter);
        AbstractFilter.checkFilter(onFilters, false);
        return this;
    }

    public JoinQueryParam on(String mainField, String joinField) {
        if (onFilters == null) onFilters = new ArrayList<>();
        mainField = FilterUtils.tableAsField(mainEntityQuery.getTableAs(), mainField);
        joinField = FilterUtils.tableAsField(joinEntityQuery.getTableAs(), joinField);
        return on(Filter.expression(mainField, Filter.Operator.EQ, Expression.filter("${" + joinField + "}")));
    }

    public JoinQueryParam subJoinQuery(boolean subJoinQuery) {
        this.subJoinQuery = subJoinQuery;
        return this;
    }

    public JoinTypeEnum getJoinType() {
        return joinType;
    }

    public boolean isSubJoinQuery() {
        return subJoinQuery;
    }

    public EntityQuery getJoinEntityQuery() {
        return joinEntityQuery;
    }

    public List<Filter> getOnFilters() {
        return onFilters;
    }

}
