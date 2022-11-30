package cn.veasion.db.query;

import cn.veasion.db.base.Filter;
import cn.veasion.db.lambda.ILambdaFilter;
import cn.veasion.db.lambda.LambdaFunction;
import cn.veasion.db.utils.FieldUtils;

/**
 * LambdaQuery
 *
 * @author luozhuowei
 * @date 2022/11/30
 */
public class LambdaQuery<E> extends AbstractQuery<LambdaQuery<E>> implements ILambdaFilter<LambdaQuery<E>, E> {

    public LambdaQuery() {
    }

    public LambdaQuery(String... fields) {
        selects(fields);
    }

    @SafeVarargs
    public LambdaQuery(LambdaFunction<E, ?>... lambdaFields) {
        selects(FieldUtils.getFieldNames(lambdaFields));
    }

    public <R> LambdaQuery<E> select(LambdaFunction<E, R> lambdaField) {
        return select(lambdaField, null);
    }

    public <R> LambdaQuery<E> select(LambdaFunction<E, R> lambdaField, String alias) {
        return select(FieldUtils.getFieldName(lambdaField), alias);
    }

    @SafeVarargs
    public final LambdaQuery<E> selects(LambdaFunction<E, ?>... lambdaFields) {
        return selects(FieldUtils.getFieldNames(lambdaFields));
    }

    public <R> LambdaQuery<E> alias(LambdaFunction<E, R> lambdaField, String alias) {
        return alias(FieldUtils.getFieldName(lambdaField), alias);
    }

    @SafeVarargs
    public final LambdaQuery<E> excludeFields(LambdaFunction<E, ?>... lambdaFields) {
        return excludeFields(FieldUtils.getFieldNames(lambdaFields));
    }

    @SafeVarargs
    public final LambdaQuery<E> groupBy(LambdaFunction<E, ?>... lambdaFields) {
        return groupBy(FieldUtils.getFieldNames(lambdaFields));
    }

    public <R> LambdaQuery<E> asc(LambdaFunction<E, R> lambdaField) {
        return asc(FieldUtils.getFieldName(lambdaField));
    }

    public <R> LambdaQuery<E> desc(LambdaFunction<E, R> lambdaField) {
        return desc(FieldUtils.getFieldName(lambdaField));
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
    public void check(Class<?> mainEntityClass) {
        boolean emptySelect = getSelects().isEmpty() && getSelectExpression() == null && getSelectSubQueryList() == null && !isSelectAll();
        if (emptySelect) {
            super.selectAll();
        }
        super.check(mainEntityClass);
    }

    @Override
    protected LambdaQuery<E> getSelf() {
        return this;
    }

}
