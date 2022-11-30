package cn.veasion.db.utils;

import cn.veasion.db.DbException;
import cn.veasion.db.FilterException;
import cn.veasion.db.base.Filter;
import cn.veasion.db.base.Operator;
import cn.veasion.db.lambda.LambdaFunction;

import java.util.Collection;
import java.util.Iterator;

/**
 * FilterUtils
 *
 * @author luozhuowei
 * @date 2021/12/2
 */
public class FilterUtils {

    public static Filter checkFilter(Filter filter) {
        // 安全检查
        return filter;
    }

    public static boolean hasFilter(Filter filter) {
        if (filter == null) {
            return false;
        }
        if (filter.getSql() != null && filter.getSql().contains("?")) {
            return filter.getValue() != null;
        }
        return true;
    }

    public static String tableAsField(String tableAs, String field) {
        if (field == null) {
            return null;
        }
        if ("-".equals(tableAs)) {
            int idx = field.indexOf(".");
            if (idx > -1) {
                field = field.substring(idx + 1);
            }
        } else if (tableAs != null && !field.contains(".")) {
            field = tableAs + "." + field;
        }
        return field;
    }

    public static <T, R> Filter getFilter(LambdaFunction<T, R> fieldLambda, String operator, Object value) {
        String field = FieldUtils.getFieldName(fieldLambda);
        return getFilter(field, operator, value);
    }

    public static <T, R> Filter getFilter(LambdaFunction<T, R> fieldLambda, Operator operator, Object value) {
        String field = FieldUtils.getFieldName(fieldLambda);
        return getFilter(field, operator, value);
    }

    public static Filter getFilter(String field, String operator, Object value) {
        Operator _operator = Operator.of(operator);
        if (_operator == null) {
            throw new DbException("操作符不支持：" + operator);
        }
        return getFilter(field, _operator, value);
    }

    public static Filter getFilter(String field, Operator operator, Object value) {
        switch (operator) {
            case EQ:
                return Filter.eq(field, value);
            case NEQ:
                return Filter.neq(field, value);
            case GT:
                return Filter.gt(field, value);
            case GTE:
                return Filter.gte(field, value);
            case LT:
                return Filter.lt(field, value);
            case LTE:
                return Filter.lte(field, value);
            case IN:
                if (value instanceof Collection) {
                    return Filter.in(field, (Collection<?>) value);
                } else if (value instanceof Object[]) {
                    return Filter.in(field, (Object[]) value);
                } else {
                    throw new FilterException(field + " 字段 Operator.IN 类型必须是集合或者数组");
                }
            case NOT_IN:
                if (value instanceof Collection) {
                    return Filter.notIn(field, (Collection<?>) value);
                } else if (value instanceof Object[]) {
                    return Filter.notIn(field, (Object[]) value);
                } else {
                    throw new FilterException(field + " 字段 Operator.NOT_IN 类型必须是集合或者数组");
                }
            case LIKE:
                if (value instanceof String) {
                    String str = (String) value;
                    if (str.startsWith("%") && str.endsWith("%")) {
                        return Filter.like(field, str.substring(1, str.length() - 1));
                    } else if (str.startsWith("%")) {
                        return Filter.likeLeft(field, str.substring(1));
                    } else if (str.endsWith("%")) {
                        return Filter.likeRight(field, str.substring(0, str.length() - 1));
                    }
                }
                return Filter.like(field, value);
            case NOT_LIKE:
                if (value instanceof String) {
                    String str = (String) value;
                    if (str.startsWith("%") && str.endsWith("%")) {
                        return Filter.notLike(field, str.substring(1, str.length() - 1));
                    } else if (str.startsWith("%")) {
                        return Filter.notLikeLeft(field, str.substring(1));
                    } else if (str.endsWith("%")) {
                        return Filter.notLikeRight(field, str.substring(0, str.length() - 1));
                    }
                }
                return Filter.notLike(field, value);
            case BETWEEN:
                if (value instanceof Collection) {
                    Iterator<?> iterator = ((Collection<?>) value).iterator();
                    return Filter.between(field, iterator.next(), iterator.next());
                } else if (value instanceof Object[]) {
                    Object[] objects = (Object[]) value;
                    return Filter.between(field, objects[0], objects[1]);
                } else {
                    throw new FilterException(field + " 字段 Operator.BETWEEN 类型必须是集合或者数组");
                }
            case NULL:
                return Filter.isNull(field);
            case NOT_NULL:
                return Filter.isNotNull(field);
            default:
                throw new FilterException(field + " 不支持 Operator." + operator.name());
        }
    }

}
