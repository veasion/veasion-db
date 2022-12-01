package cn.veasion.db.update;

import cn.veasion.db.AbstractFilter;
import cn.veasion.db.DbException;
import cn.veasion.db.base.Filter;
import cn.veasion.db.base.JoinType;
import cn.veasion.db.base.Operator;
import cn.veasion.db.lambda.LambdaFunction;
import cn.veasion.db.query.SubQueryParam;
import cn.veasion.db.utils.FieldUtils;
import cn.veasion.db.utils.FilterUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JoinUpdateParam
 *
 * @author luozhuowei
 * @date 2021/12/3
 */
public class JoinUpdateParam implements Serializable {

    private JoinType joinType;
    private AbstractJoinUpdate<?> mainUpdate;
    private AbstractJoinUpdate<?> joinUpdate;
    private List<Filter> onFilters;

    public JoinUpdateParam(AbstractJoinUpdate<?> mainUpdate, JoinType joinType, AbstractJoinUpdate<?> joinUpdate) {
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
        return on(Filter.expression(mainField, Operator.EQ, "${" + joinField + "}"));
    }

    public <T1, T2> JoinUpdateParam on(LambdaFunction<T1, ?> mainField, LambdaFunction<T2, ?> joinField) {
        return on(FieldUtils.getFieldName(mainField), FieldUtils.getFieldName(joinField));
    }

    public JoinType getJoinType() {
        return joinType;
    }

    public AbstractJoinUpdate<?> getMainUpdate() {
        return mainUpdate;
    }

    public AbstractJoinUpdate<?> getJoinUpdate() {
        return joinUpdate;
    }

    public List<Filter> getOnFilters() {
        return onFilters;
    }

}
