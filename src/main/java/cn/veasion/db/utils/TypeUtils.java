package cn.veasion.db.utils;

import cn.veasion.db.base.Table;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TypeUtils
 *
 * @author luozhuowei
 * @date 2021/12/5
 */
@SuppressWarnings("unchecked")
public class TypeUtils {

    static TypeConvert typeConvert = ServiceLoaderUtils.typeConvert();
    private final static Map<Class<?>, String> TABLE_MAP = new ConcurrentHashMap<>();

    public static String getTableName(Class<?> entityClazz) {
        if (TABLE_MAP.containsKey(entityClazz)) {
            return TABLE_MAP.get(entityClazz);
        }
        Table annotation = entityClazz.getAnnotation(Table.class);
        if (annotation != null) {
            if (!"".equals(annotation.value())) {
                String tableName = annotation.value();
                TABLE_MAP.put(entityClazz, tableName);
                return tableName;
            }
            if (annotation.entityClass() != Void.class && annotation.entityClass() != entityClazz) {
                String tableName = getTableName(annotation.entityClass());
                TABLE_MAP.put(entityClazz, tableName);
                return tableName;
            }
        }
        String tableName = FieldUtils.humpToLine(entityClazz.getSimpleName());
        int idx = tableName.lastIndexOf("_");
        if (idx > -1 && tableName.length() - idx <= 4) {
            String end = tableName.substring(idx + 1);
            if ("vo".equals(end) || "vO".equals(end) || "po".equals(end) || "pO".equals(end)
                    || "do".equals(end) || "dO".equals(end) || "dto".equals(end) || "dTO".equals(end)) {
                tableName = tableName.substring(0, idx);
            }
        }
        TABLE_MAP.put(entityClazz, tableName);
        return tableName;
    }

    public static boolean isSimpleClass(Class<?> clazz) {
        boolean isSimpleClass = clazz == BigDecimal.class || clazz == Long.class || clazz == Integer.class || clazz == String.class
                || clazz == Double.class || clazz == Float.class || clazz == Boolean.class || Date.class.isAssignableFrom(clazz)
                || clazz == Byte.class || clazz == LocalDateTime.class || clazz == LocalDate.class;
        if (isSimpleClass) {
            return true;
        }
        if (typeConvert != null) {
            return typeConvert.isSimpleClass(clazz);
        }
        return false;
    }

    public static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("初始化对象失败: " + clazz.getName(), e);
        }
    }

    public static Class<?> loadClass(String className, ClassLoader classLoader) {
        try {
            return loadClass(className, getClassLoaders(classLoader));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("加载类失败：" + className, e);
        }
    }

    private static Class<?> loadClass(String className, ClassLoader[] classLoaders) throws ClassNotFoundException {
        for (ClassLoader classLoader : classLoaders) {
            if (classLoader != null) {
                try {
                    return Class.forName(className, true, classLoader);
                } catch (ClassNotFoundException ignored) {
                }
            }
        }
        throw new ClassNotFoundException("Cannot find class: " + className);
    }

    private static ClassLoader[] getClassLoaders(ClassLoader classLoader) {
        ClassLoader systemClassLoader = null;
        try {
            systemClassLoader = ClassLoader.getSystemClassLoader();
        } catch (Exception ignored) {
        }
        return new ClassLoader[]{classLoader, Thread.currentThread().getContextClassLoader(), TypeUtils.class.getClassLoader(), systemClassLoader};
    }

    public static <T extends Serializable> T serializableCopy(T obj) throws IOException, ClassNotFoundException {
        try (ByteArrayOutputStream byteOs = new ByteArrayOutputStream(); ObjectOutputStream objOs = new ObjectOutputStream(byteOs)) {
            objOs.writeObject(obj);
            objOs.flush();
            try (ObjectInputStream objIs = new ObjectInputStream(new ByteArrayInputStream(byteOs.toByteArray())) {
                @Override
                protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                    return super.resolveClass(desc);
                }
            }) {
                return (T) objIs.readObject();
            }
        }
    }

    public static <E> E map2Obj(Map<String, Object> map, Class<E> clazz) throws Exception {
        if (Map.class.isAssignableFrom(clazz) || Object.class.equals(clazz)) {
            return (E) map;
        }
        E instance = clazz.newInstance();
        Map<String, Field> fields = FieldUtils.fields(clazz);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (fields.containsKey(entry.getKey())) {
                FieldUtils.setValue(instance, entry.getKey(), entry.getValue(), true);
            }
        }
        return instance;
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
            if (object instanceof Boolean) {
                return (T) (Boolean.TRUE.equals(object) ? new Integer(1) : new Integer(0));
            }
            if (toStr.contains(".")) {
                toStr = toStr.split("\\.")[0];
            }
            return (T) Integer.valueOf(toStr);
        } else if (clazz == Boolean.class) {
            if ("1".equals(toStr) || "true".equalsIgnoreCase(toStr) || "Y".equalsIgnoreCase(toStr) || "Yes".equalsIgnoreCase(toStr) || "是".equals(toStr)) {
                return (T) Boolean.TRUE;
            } else if ("0".equals(toStr) || "false".equalsIgnoreCase(toStr) || "N".equalsIgnoreCase(toStr) || "No".equalsIgnoreCase(toStr) || "否".equals(toStr)) {
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
            int length = toStr.length();
            if ((length == 10 || length == 13) && toStr.matches("\\d+")) {
                long timestamp = Long.parseLong(toStr);
                if (length == 10) {
                    timestamp *= 1000;
                }
                return new Date(timestamp);
            }
            if (length == 10 && toStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                // yyyy-MM-dd
                return new SimpleDateFormat("yyyy-MM-dd").parse(toStr);
            } else if (length > 20 && toStr.contains("+")) {
                // -timezone
                toStr = toStr.split("\\+")[0];
            }
            if (length == 19) {
                // toStr.matches("\\d{4}-\\d{2}-\\d{2}(\\s|T)\\d{2}:\\d{2}:\\d{2}")
                if (toStr.charAt(10) == 'T') {
                    // yyyy-MM-dd'T'HH:mm:ss
                    return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(toStr);
                } else {
                    // yyyy-MM-dd HH:mm:ss
                    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(toStr);
                }
            } else if (length == 23) {
                // toStr.matches("\\d{4}-\\d{2}-\\d{2}(\\s|T)\\d{2}:\\d{2}:\\d{2}\\.\\d{3}")
                if (toStr.charAt(10) == 'T') {
                    // yyyy-MM-dd'T'HH:mm:ss.SSS
                    return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(toStr);
                } else {
                    // yyyy-MM-dd HH:mm:ss.SSS
                    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(toStr);
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

}
