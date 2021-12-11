package cn.veasion.db.update;

import cn.veasion.db.AbstractFilter;
import cn.veasion.db.DbException;
import cn.veasion.db.base.Expression;
import cn.veasion.db.base.Filter;
import cn.veasion.db.base.JoinType;
import cn.veasion.db.query.SubQueryParam;
import cn.veasion.db.utils.FilterUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JoinUpdateParam
 *
 * @author luozhuowei
 * @date 2021/12/3
 */
public class JoinUpdateParam {

    private JoinType joinType;
    private EntityUpdate mainUpdate;
    private EntityUpdate joinUpdate;
    private List<Filter> onFilters;

    public JoinUpdateParam(EntityUpdate mainUpdate, JoinType joinType, EntityUpdate joinUpdate) {
        this.joinType = joinType;
        this.mainUpdate = mainUpdate;
        this.joinUpdate = joinUpdate;
    }

    public JoinUpdateParam on(Filter filter) {
        Objects.requireNonNull(filter, "过滤不能为空");
        if (onFilters == null) onFilters = new ArrayList<>();
        if (filter.isSpecial() && filter.getValue() instanceof SubQueryParam) {
            throw new DbException("on条件不支持子查询");
        }
        onFilters.add(filter);
        AbstractFilter.checkFilter(null, onFilters, false);
        return this;
    }

    public JoinUpdateParam on(String mainField, String joinField) {
        mainField = FilterUtils.tableAsField(mainUpdate.getTableAs(), mainField);
        joinField = FilterUtils.tableAsField(joinUpdate.getTableAs(), joinField);
        return on(Filter.expression(mainField, Filter.Operator.EQ, Expression.filter("${" + joinField + "}")));
    }

    public JoinType getJoinType() {
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
