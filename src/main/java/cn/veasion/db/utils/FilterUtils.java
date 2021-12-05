package cn.veasion.db.utils;

import cn.veasion.db.base.Filter;

/**
 * FilterUtils
 *
 * @author luozhuowei
 * @date 2021/12/2
 */
public class FilterUtils {

    public static Filter checkFilter(Filter filter) {
        return filter;
    }

    public static boolean hasFilter(Filter filter) {
        if (filter == null || filter.getField() == null) {
            return false;
        }
        if (filter.getSql().contains("?")) {
            Object value = filter.getValue();
            return value != null;
        }
        return true;
    }

    public static String tableAsField(String tableAs, String field) {
        if (field == null) {
            return null;
        }
        if (tableAs != null && !field.contains(".")) {
            field = tableAs + ".";
        }
        return field;
    }

}
