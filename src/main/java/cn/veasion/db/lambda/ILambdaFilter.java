package cn.veasion.db.lambda;

import cn.veasion.db.IFilter;
import cn.veasion.db.base.Expression;
import cn.veasion.db.base.Filter;
import cn.veasion.db.base.Operator;
import cn.veasion.db.query.SubQueryParam;

import java.util.Collection;

/**
 * ILambdaFilter
 *
 * @author luozhuowei
 * @date 2022/11/30
 */
public interface ILambdaFilter<T, E> extends IFilter<T> {

    default <R> T eq(LambdaFunction<E, R> lambdaField, Object value) {
        return addFilter(Filter.eq(lambdaField, value));
    }

    default <R> T neq(LambdaFunction<E, R> lambdaField, Object value) {
        return addFilter(Filter.neq(lambdaField, value));
    }

    default <R> T gt(LambdaFunction<E, R> lambdaField, Object value) {
        return addFilter(Filter.gt(lambdaField, value));
    }

    default <R> T gte(LambdaFunction<E, R> lambdaField, Object value) {
        return addFilter(Filter.gte(lambdaField, value));
    }

    default <R> T lt(LambdaFunction<E, R> lambdaField, Object value) {
        return addFilter(Filter.lt(lambdaField, value));
    }

    default <R> T lte(LambdaFunction<E, R> lambdaField, Object value) {
        return addFilter(Filter.lte(lambdaField, value));
    }

    default <R> T in(LambdaFunction<E, R> lambdaField, Collection<?> value) {
        return addFilter(Filter.in(lambdaField, value));
    }

    default <R> T in(LambdaFunction<E, R> lambdaField, Object[] value) {
        return addFilter(Filter.in(lambdaField, value));
    }

    default <R> T notIn(LambdaFunction<E, R> lambdaField, Collection<?> value) {
        return addFilter(Filter.notIn(lambdaField, value));
    }

    default <R> T notIn(LambdaFunction<E, R> lambdaField, Object[] value) {
        return addFilter(Filter.notIn(lambdaField, value));
    }

    default <R> T like(LambdaFunction<E, R> lambdaField, Object value) {
        return addFilter(Filter.like(lambdaField, value));
    }

    default <R> T likeLeft(LambdaFunction<E, R> lambdaField, Object value) {
        return addFilter(Filter.likeLeft(lambdaField, value));
    }

    default <R> T likeRight(LambdaFunction<E, R> lambdaField, Object value) {
        return addFilter(Filter.likeRight(lambdaField, value));
    }

    default <R> T notLike(LambdaFunction<E, R> lambdaField, Object value) {
        return addFilter(Filter.notLike(lambdaField, value));
    }

    default <R> T notLikeLeft(LambdaFunction<E, R> lambdaField, Object value) {
        return addFilter(Filter.notLikeLeft(lambdaField, value));
    }

    default <R> T notLikeRight(LambdaFunction<E, R> lambdaField, Object value) {
        return addFilter(Filter.notLikeRight(lambdaField, value));
    }

    default <R> T isNull(LambdaFunction<E, R> lambdaField) {
        return addFilter(Filter.isNull(lambdaField));
    }

    default <R> T isNotNull(LambdaFunction<E, R> lambdaField) {
        return addFilter(Filter.isNotNull(lambdaField));
    }

    default <R> T between(LambdaFunction<E, R> lambdaField, Object value1, Object value2) {
        return addFilter(Filter.between(lambdaField, value1, value2));
    }

    default <R> T filterSubQuery(LambdaFunction<E, R> lambdaField, Operator operator, SubQueryParam subQueryParam) {
        return addFilter(Filter.subQuery(lambdaField, operator, subQueryParam));
    }

    /**
     * 表达式过滤
     *
     * @param expression 表达式，其中#{}和${}中间可以使用占位字段，解析时#{}会默认替换成values对应的值，${}替换成字段对应表中的列名 <br><br>
     *                   示例一：NOW() <br>
     *                   示例二：DATE_FORMAT(#{value1},'%Y-%m-%d') <br>
     *                   示例二：${age} + #{value1} + #{age} <br>
     * @param values     占位值，对应 #{value1}, #{value2}, #{value3}, #{value...}，通过占位符拼接参数防SQL注入
     */
    default <R> T filterExpression(LambdaFunction<E, R> lambdaField, Operator operator, String expression, Object... values) {
        return filterExpression(lambdaField, operator, Expression.filter(expression, values));
    }

    default <R> T filterExpression(LambdaFunction<E, R> lambdaField, Operator operator, Expression expression) {
        return addFilter(Filter.expression(lambdaField, operator, expression));
    }

}
