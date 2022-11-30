package cn.veasion.db.query;

import cn.veasion.db.base.Expression;
import cn.veasion.db.base.Operator;
import cn.veasion.db.lambda.ILambdaFilter;
import cn.veasion.db.lambda.LambdaFunction;
import cn.veasion.db.utils.FieldUtils;

/**
 * LambdaEntityQuery
 *
 * @author luozhuowei
 * @date 2022/11/30
 */
public class LambdaEntityQuery<E> extends AbstractJoinQuery<LambdaEntityQuery<E>> implements ILambdaFilter<LambdaEntityQuery<E>, E> {

    public LambdaEntityQuery(Class<E> entityClass) {
        this(entityClass, null);
    }

    public LambdaEntityQuery(Class<E> entityClass, String alias) {
        this.tableAs = alias == null || "".equals(alias) ? null : alias;
        setEntityClass(entityClass);
    }

    public <R> LambdaEntityQuery<E> select(LambdaFunction<E, R> lambdaField) {
        return select(lambdaField, null);
    }

    public <R> LambdaEntityQuery<E> select(LambdaFunction<E, R> lambdaField, String alias) {
        return select(FieldUtils.getFieldName(lambdaField), alias);
    }

    @SafeVarargs
    public final LambdaEntityQuery<E> selects(LambdaFunction<E, ?>... lambdaFields) {
        return selects(FieldUtils.getFieldNames(lambdaFields));
    }

    public <R> LambdaEntityQuery<E> alias(LambdaFunction<E, R> lambdaField, String alias) {
        return alias(FieldUtils.getFieldName(lambdaField), alias);
    }

    @SafeVarargs
    public final LambdaEntityQuery<E> excludeFields(LambdaFunction<E, ?>... lambdaFields) {
        return excludeFields(FieldUtils.getFieldNames(lambdaFields));
    }

    @SafeVarargs
    public final LambdaEntityQuery<E> groupBy(LambdaFunction<E, ?>... lambdaFields) {
        return groupBy(FieldUtils.getFieldNames(lambdaFields));
    }

    public <R> LambdaEntityQuery<E> asc(LambdaFunction<E, R> lambdaField) {
        return asc(FieldUtils.getFieldName(lambdaField));
    }

    public <R> LambdaEntityQuery<E> desc(LambdaFunction<E, R> lambdaField) {
        return desc(FieldUtils.getFieldName(lambdaField));
    }

    @Override
    public <R> LambdaEntityQuery<E> filterExpression(LambdaFunction<E, R> lambdaField, Operator operator, Expression expression) {
        return filterExpression(FieldUtils.getFieldName(lambdaField), operator, expression);
    }

    @Override
    protected LambdaEntityQuery<E> getSelf() {
        return this;
    }

}
