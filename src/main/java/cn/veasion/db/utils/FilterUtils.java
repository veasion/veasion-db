package cn.veasion.db.utils;

import cn.veasion.db.FilterException;
import cn.veasion.db.base.Filter;
import cn.veasion.db.base.Operator;

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
        // TODO 安全检查
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

    public static Filter getFilter(String field, Operator operator, Object value) {
        if (Operator.EQ.equals(operator)) {
            return Filter.eq(field, value);
        } else if (Operator.NEQ.equals(operator)) {
            return Filter.neq(field, value);
        } else if (Operator.GT.equals(operator)) {
            return Filter.gt(field, value);
        } else if (Operator.GTE.equals(operator)) {
            return Filter.gte(field, value);
        } else if (Operator.LT.equals(operator)) {
            return Filter.lt(field, value);
        } else if (Operator.LTE.equals(operator)) {
            return Filter.lte(field, value);
        } else if (Operator.IN.equals(operator)) {
            if (value instanceof Collection) {
                return Filter.in(field, (Collection<?>) value);
            } else if (value instanceof Object[]) {
                return Filter.in(field, (Object[]) value);
            } else {
                throw new FilterException(field + " 字段 Operator.IN 类型必须是集合或者数组");
            }
        } else if (Operator.NOT_IN.equals(operator)) {
            if (value instanceof Collection) {
                return Filter.notIn(field, (Collection<?>) value);
            } else if (value instanceof Object[]) {
                return Filter.notIn(field, (Object[]) value);
            } else {
                throw new FilterException(field + " 字段 Operator.IN 类型必须是集合或者数组");
            }
        } else if (Operator.LIKE.equals(operator)) {
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
        } else if (Operator.BETWEEN.equals(operator)) {
            if (value instanceof Collection) {
                Iterator<?> iterator = ((Collection<?>) value).iterator();
                return Filter.between(field, iterator.next(), iterator.next());
            } else if (value instanceof Object[]) {
                Object[] objects = (Object[]) value;
                return Filter.between(field, objects[0], objects[1]);
            } else {
                throw new FilterException(field + " 字段 Operator.BETWEEN 类型必须是集合或者数组");
            }
        } else if (Operator.NULL.equals(operator) && !Boolean.FALSE.equals(value)) {
            return Filter.isNull(field);
        } else if (Operator.NOT_NULL.equals(operator) && !Boolean.FALSE.equals(value)) {
            return Filter.isNotNull(field);
        } else {
            throw new FilterException(field + " 不支持 Operator." + operator.name());
        }
    }

}
