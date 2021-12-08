package cn.veasion.db.utils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * TypeUtils
 *
 * @author luozhuowei
 * @date 2021/12/5
 */
@SuppressWarnings("unchecked")
public class TypeUtils {

    static TypeConvert typeConvert;

    static {
        ServiceLoader<TypeConvert> serviceLoader = ServiceLoader.load(TypeConvert.class);
        Iterator<TypeConvert> iterator = serviceLoader.iterator();
        if (iterator.hasNext()) {
            typeConvert = iterator.next();
        }
    }

    /**
     * 简单类型转换
     */
    public static <T> T convert(Object object, Class<T> clazz) {
        if (object == null || clazz == null || clazz.isAssignableFrom(object.getClass())) {
            return (T) object;
        }
        if (typeConvert != null) {
            T value = typeConvert.convert(object, clazz);
            if (value != null) {
                return value;
            }
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
            if (object instanceof Number) {
                return (T) new Long(((Number) object).longValue());
            }
            if (toStr.contains(".")) {
                toStr = toStr.split("\\.")[0];
            }
            return (T) Long.valueOf(toStr);
        } else if (clazz == Double.class) {
            if (object instanceof Number) {
                return (T) new Double(((Number) object).doubleValue());
            }
            return (T) Double.valueOf(toStr);
        } else if (clazz == Float.class) {
            if (object instanceof Number) {
                return (T) new Float(((Number) object).floatValue());
            }
            return (T) Float.valueOf(toStr);
        } else if (clazz == Integer.class) {
            if (object instanceof Number) {
                return (T) new Integer(((Number) object).intValue());
            }
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
        } else if (Date.class.isAssignableFrom(clazz) || Temporal.class.isAssignableFrom(clazz)) {
            T value = null;
            if (Date.class.isAssignableFrom(object.getClass())) {
                value = dateTo((Date) object, clazz);
            } else if (Temporal.class.isAssignableFrom(object.getClass())) {
                value = temporalTo((Temporal) object, clazz);
            }
            if (value == null) {
                Date date = toDate(toStr);
                value = dateTo(date, clazz);
            }
            if (value != null) {
                return value;
            }
        } else if (clazz == Byte.class) {
            if (toStr.matches("-?\\d+")) {
                return (T) new Byte(toStr);
            }
        }
        return (T) object;
    }

    private static <T> T dateTo(Date date, Class<T> clazz) {
        if (date == null) {
            return null;
        }
        if (clazz == Date.class) {
            return (T) date;
        } else if (clazz == java.sql.Date.class) {
            return (T) new java.sql.Date(date.getTime());
        } else if (clazz == java.sql.Timestamp.class) {
            return (T) new java.sql.Timestamp(date.getTime());
        } else if (clazz == LocalDateTime.class) {
            return (T) LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        } else if (clazz == LocalDate.class) {
            return (T) LocalDate.from(date.toInstant());
        } else if (clazz == LocalTime.class) {
            return (T) LocalTime.from(date.toInstant());
        }
        return null;
    }

    private static <T> T temporalTo(Temporal temporal, Class<T> clazz) {
        Date date = null;
        if (temporal instanceof LocalDateTime) {
            date = Date.from(((LocalDateTime) temporal).atZone(ZoneId.systemDefault()).toInstant());
        } else if (temporal instanceof LocalDate) {
            date = Date.from(((LocalDate) temporal).atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
        if (date == null) {
            return (T) temporal;
        }
        return dateTo(date, clazz);
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

    public static boolean isSimpleClass(Class<?> clazz) {
        return clazz == BigDecimal.class || clazz == Long.class || clazz == Integer.class || clazz == String.class
                || clazz == Double.class || clazz == Float.class || clazz == Boolean.class || Date.class.isAssignableFrom(clazz)
                || clazz == Byte.class || clazz == LocalDateTime.class || clazz == LocalDate.class;
    }

}
