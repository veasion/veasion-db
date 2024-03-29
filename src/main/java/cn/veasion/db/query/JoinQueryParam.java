package cn.veasion.db.query;

import cn.veasion.db.AbstractFilter;
import cn.veasion.db.DbException;
import cn.veasion.db.base.Filter;
import cn.veasion.db.base.JoinType;
import cn.veasion.db.base.Operator;
import cn.veasion.db.lambda.LambdaFunction;
import cn.veasion.db.utils.FieldUtils;
import cn.veasion.db.utils.FilterUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * JoinQueryParam
 *
 * @author luozhuowei
 * @date 2021/12/2
 */
public class JoinQueryParam implements Serializable {

    private JoinType joinType;
    private AbstractJoinQuery<?> mainQuery;
    private AbstractJoinQuery<?> joinQuery;
    private List<Filter> onFilters;

    public JoinQueryParam(AbstractJoinQuery<?> mainQuery, JoinType joinType, AbstractJoinQuery<?> joinQuery) {
        this.joinType = joinType;
        this.mainQuery = mainQuery;
        this.joinQuery = joinQuery;
    }

    public JoinQueryParam on(String mainField, String joinField) {
        mainField = FilterUtils.tableAsField(mainQuery.getTableAs(), mainField);
        joinField = FilterUtils.tableAsField(joinQuery.getTableAs(), joinField);
        return on(Filter.expression(mainField, Operator.EQ, "${" + joinField + "}"));
    }

    public <T1, T2> JoinQueryParam on(LambdaFunction<T1, ?> mainField, LambdaFunction<T2, ?> joinField) {
        return on(FieldUtils.getFieldName(mainField), FieldUtils.getFieldName(joinField));
    }

    public JoinQueryParam on(Filter filter) {
        Objects.requireNonNull(filter, "过滤不能为空");
        if (onFilters == null) onFilters = new ArrayList<>();
        if (filter.isSpecial() && filter.getValue() instanceof SubQueryParam) {
            throw new DbException("on条件不支持子查询");
        }
        filter.fieldAs(joinQuery.getTableAs());
        onFilters.add(filter);
        AbstractFilter.checkFilter(null, onFilters, false);
        return this;
    }

    public JoinQueryParam exec(Consumer<AbstractJoinQuery<?>> mainQueryConsumer, Consumer<AbstractJoinQuery<?>> joinQueryConsumer) {
        if (mainQueryConsumer != null) {
            mainQueryConsumer.accept(mainQuery);
        }
        if (joinQueryConsumer != null) {
            joinQueryConsumer.accept(joinQuery);
        }
        return this;
    }

    public JoinType getJoinType() {
        return joinType;
    }

    public AbstractJoinQuery<?> getMainQuery() {
        return mainQuery;
    }

    public AbstractJoinQuery<?> getJoinQuery() {
        return joinQuery;
    }

    public List<Filter> getOnFilters() {
        return onFilters;
    }

}
