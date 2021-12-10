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
    private EntityUpdate mainUpdate;
    private EntityUpdate joinUpdate;
    private List<Filter> onFilters;

    public JoinUpdateParam(EntityUpdate mainUpdate, JoinTypeEnum joinType, EntityUpdate joinUpdate) {
        this.joinType = joinType;
        this.mainUpdate = mainUpdate;
        this.joinUpdate = joinUpdate;
    }

    public JoinUpdateParam on(Filter filter) {
        onFilters.add(filter);
        AbstractFilter.checkFilter(onFilters, false);
        return this;
    }

    public JoinUpdateParam on(String mainField, String joinField) {
        if (onFilters == null) onFilters = new ArrayList<>();
        mainField = FilterUtils.tableAsField(mainUpdate.getTableAs(), mainField);
        joinField = FilterUtils.tableAsField(joinUpdate.getTableAs(), joinField);
        return on(Filter.expression(mainField, Filter.Operator.EQ, Expression.filter("${" + joinField + "}")));
    }

    public JoinTypeEnum getJoinType() {
        return joinType;
    }

    public EntityUpdate getMainUpdate() {
        return mainUpdate;
    }

    public EntityUpdate getJoinUpdate() {
        return joinUpdate;
    }

    public List<Filter> getOnFilters() {
        return onFilters;
    }
}
