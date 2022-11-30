package cn.veasion.db.update;

import cn.veasion.db.base.Expression;
import cn.veasion.db.base.Filter;
import cn.veasion.db.base.Operator;
import cn.veasion.db.lambda.ILambdaFilter;
import cn.veasion.db.lambda.LambdaFunction;
import cn.veasion.db.utils.FieldUtils;

/**
 * LambdaEntityUpdate
 *
 * @author luozhuowei
 * @date 2022/11/30
 */
public class LambdaEntityUpdate<E> extends AbstractJoinUpdate<LambdaEntityUpdate<E>> implements ILambdaFilter<LambdaEntityUpdate<E>, E> {

    public LambdaEntityUpdate(E entity) {
        super(entity);
    }

    public LambdaEntityUpdate(E entity, String alias) {
        super(entity, alias);
    }

    public LambdaEntityUpdate(Class<E> clazz) {
        super(clazz);
    }

    public LambdaEntityUpdate(Class<E> clazz, String alias) {
        super(clazz, alias);
    }

    @SafeVarargs
    public final LambdaEntityUpdate<E> updateFields(LambdaFunction<E, ?>... lambdaFields) {
        return updateFields(FieldUtils.getFieldNames(lambdaFields));
    }

    @SuppressWarnings("unchecked")
    public <R> LambdaEntityUpdate<E> eq(LambdaFunction<E, R> lambdaField) {
        return addFilter(Filter.eq(FieldUtils.getFieldName(lambdaField), lambdaField.apply((E) entity)));
    }

    public <R> LambdaEntityUpdate<E> update(LambdaFunction<E, R> lambdaField, Object value) {
        return super.update(FieldUtils.getFieldName(lambdaField), value);
    }

    public <R> LambdaEntityUpdate<E> updateExpression(LambdaFunction<E, R> lambdaField, String expression, Object... values) {
        return updateExpression(lambdaField, Expression.update(expression, values));
    }

    public <R> LambdaEntityUpdate<E> updateExpression(LambdaFunction<E, R> lambdaField, Expression expression) {
        return updateExpression(FieldUtils.getFieldName(lambdaField), expression);
    }

    @SafeVarargs
    public final LambdaEntityUpdate<E> excludeUpdates(LambdaFunction<E, ?>... lambdaFields) {
        return excludeUpdates(FieldUtils.getFieldNames(lambdaFields));
    }

    @Override
    public <R> LambdaEntityUpdate<E> filterExpression(LambdaFunction<E, R> lambdaField, Operator operator, Expression expression) {
        return filterExpression(FieldUtils.getFieldName(lambdaField), operator, expression);
    }

    @Override
    protected LambdaEntityUpdate<E> getSelf() {
        return this;
    }

}
