package cn.veasion.db.update;

import cn.veasion.db.base.Expression;
import cn.veasion.db.base.Filter;
import cn.veasion.db.lambda.ILambdaFilter;
import cn.veasion.db.lambda.LambdaFunction;
import cn.veasion.db.utils.FieldUtils;

/**
 * LambdaUpdate
 *
 * @author luozhuowei
 * @date 2022/11/30
 */
public class LambdaUpdate<E> extends AbstractUpdate<LambdaUpdate<E>> implements ILambdaFilter<LambdaUpdate<E>, E> {

    public LambdaUpdate() {
    }

    public LambdaUpdate(LambdaFunction<E, ?> lambdaField, Object value) {
        update(lambdaField, value);
    }

    public LambdaUpdate(LambdaFunction<E, ?> lambdaField1, Object value1, LambdaFunction<E, ?> lambdaField2, Object value2) {
        update(lambdaField1, value1).update(lambdaField2, value2);
    }

    public LambdaUpdate(String field, Object value) {
        update(field, value);
    }

    public LambdaUpdate(String field1, Object value1, String field2, Object value2) {
        update(field1, value1).update(field2, value2);
    }

    public <R> LambdaUpdate<E> update(LambdaFunction<E, R> lambdaField, Object value) {
        String field = FieldUtils.getFieldName(lambdaField);
        return super.update(field, value);
    }

    public <R> LambdaUpdate<E> updateExpression(LambdaFunction<E, R> lambdaField, String expression, Object... values) {
        return updateExpression(lambdaField, Expression.update(expression, values));
    }

    public <R> LambdaUpdate<E> updateExpression(LambdaFunction<E, R> lambdaField, Expression expression) {
        String field = FieldUtils.getFieldName(lambdaField);
        return updateExpression(field, expression);
    }

    @SafeVarargs
    public final LambdaUpdate<E> excludeUpdates(LambdaFunction<E, ?>... lambdaFields) {
        return excludeUpdates(FieldUtils.getFieldNames(lambdaFields));
    }

    @Override
    protected Filter handleFilter(Filter filter) {
        return filter;
    }

    @Override
    protected String handleField(String field) {
        return field;
    }

    @Override
    protected LambdaUpdate<E> getSelf() {
        return this;
    }

}
