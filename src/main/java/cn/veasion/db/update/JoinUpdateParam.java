package cn.veasion.db.update;

import cn.veasion.db.AbstractFilter;
import cn.veasion.db.base.Expression;
import cn.veasion.db.base.Filter;
import cn.veasion.db.base.JoinTypeEnum;
import cn.veasion.db.utils.FilterUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * JoinUpdateParam
 *
 * @author luozhuowei
 * @date 2021/12/3
 */
public class JoinUpdateParam {

    private JoinTypeEnum joinType;
    private EntityUpdate mainEntityUpdate;
    private EntityUpdate joinEntityUpdate;
    private List<Filter> onFilters;

    public JoinUpdateParam(EntityUpdate mainEntityUpdate, JoinTypeEnum joinType, EntityUpdate joinEntityUpdate) {
        this.joinType = joinType;
        this.mainEntityUpdate = mainEntityUpdate;
        this.joinEntityUpdate = joinEntityUpdate;
    }

    public JoinUpdateParam on(Filter filter) {
        onFilters.add(filter);
        AbstractFilter.checkFilter(onFilters, false);
        return this;
    }

    public JoinUpdateParam on(String mainField, String joinField) {
        if (onFilters == null) onFilters = new ArrayList<>();
        mainField = FilterUtils.tableAsField(mainEntityUpdate.getTableAs(), mainField);
        joinField = FilterUtils.tableAsField(joinEntityUpdate.getTableAs(), joinField);
        return on(Filter.expression(mainField, Filter.Operator.EQ, Expression.filter("${" + joinField + "}")));
    }

    public JoinTypeEnum getJoinType() {
        return joinType;
    }

    public EntityUpdate getJoinEntityUpdate() {
        return joinEntityUpdate;
    }

    public List<Filter> getOnFilters() {
        return onFilters;
    }
}
