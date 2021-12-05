package cn.veasion.db.utils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * TypeUtils
 *
 * @author luozhuowei
 * @date 2021/12/5
 */
public class TypeUtils {

    /**
     * 简单类型转换
     */
    @SuppressWarnings("unchecked")
    public static <T> T convert(Object object, Class<T> clazz) {
        if (object == null || clazz == null || clazz.isAssignableFrom(object.getClass())) {
            return (T) object;
        }
        String toStr = object.toString();
        if (clazz == String.class) {
            return (T) toStr;
        }
        if ("".equals(toStr)) {
            return null;
        }
        if (clazz == BigDecimal.class) {
            return (T) new BigDecimal(toStr);
        } else if (clazz == Long.class) {
            if (toStr.contains(".")) {
                toStr = toStr.split("\\.")[0];
            }
            return (T) Long.valueOf(toStr);
        } else if (clazz == Double.class) {
            return (T) Double.valueOf(toStr);
        } else if (clazz == Float.class) {
            return (T) Float.valueOf(toStr);
        } else if (clazz == Integer.class) {
            if (toStr.contains(".")) {
                toStr = toStr.split("\\.")[0];
            }
            return (T) Integer.valueOf(toStr);
        } else if (clazz == Boolean.class) {
            if ("true".equalsIgnoreCase(toStr) || "1".equals(toStr)) {
                return (T) Boolean.TRUE;
            } else if ("false".equalsIgnoreCase(toStr) || "0".equals(toStr)) {
                return (T) Boolean.FALSE;
            }
        } else if (clazz == Date.class || clazz == java.sql.Date.class || clazz == java.sql.Timestamp.class) {
            Date date = toDate(toStr);
            if (date != null) {
                if (clazz == Date.class) {
                    return (T) date;
                } else if (clazz == java.sql.Date.class) {
                    return (T) new java.sql.Date(date.getTime());
                } else {
                    return (T) new java.sql.Timestamp(date.getTime());
                }
            }
        } else if (clazz == Byte.class) {
            if (toStr.matches("-?\\d+")) {
                return (T) new Byte(toStr);
            }
        }
        return (T) object;
    }

    private static Date toDate(String toStr) {
        try {
            if (toStr.matches("\\d+")) {
                return new Date(Long.parseLong(toStr));
            } else if (toStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
                // yyyy-MM-dd HH:mm:ss
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(toStr);
            } else if (toStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                // yyyy-MM-dd
                return new SimpleDateFormat("yyyy-MM-dd").parse(toStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
