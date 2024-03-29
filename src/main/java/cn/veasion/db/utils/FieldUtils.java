package cn.veasion.db.utils;

import cn.veasion.db.DbException;
import cn.veasion.db.base.Column;
import cn.veasion.db.base.Table;
import cn.veasion.db.lambda.LambdaFunction;
import cn.veasion.db.lambda.SerializedLambdaUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FieldUtils
 *
 * @author luozhuowei
 * @date 2021/12/3
 */
public class FieldUtils {

    private static final Pattern LINE_PATTERN = Pattern.compile("_(\\w)");
    private static final Pattern HUMP_PATTERN = Pattern.compile("[a-z][A-Z]");

    private static final Map<Class<?>, Map<String, String>> FIELD_COLUMN_CACHE = new HashMap<>();
    private static final Map<Class<?>, Map<String, Field>> FIELD_CACHE = new HashMap<>();
    private static final Map<Class<?>, Map<String, Method>> METHOD_GET_CACHE = new HashMap<>();
    private static final Map<Class<?>, Map<String, List<Method>>> METHOD_SET_CACHE = new HashMap<>();

    public static Map<String, String> entityFieldColumns(Class<?> entityClazz) {
        Table annotation = entityClazz.getAnnotation(Table.class);
        if (annotation != null && annotation.entityClass() != Void.class) {
            return fieldColumns(annotation.entityClass());
        } else {
            return fieldColumns(entityClazz);
        }
    }

    public static Field getIdField(Class<?> entityClazz) {
        Table annotation = entityClazz.getAnnotation(Table.class);
        if (annotation != null) {
            String field = annotation.idField();
            if (!"".equals(field)) {
                return FieldUtils.getField(entityClazz, field);
            }
            if (annotation.entityClass() != Void.class) {
                return getIdField(annotation.entityClass());
            }
        }
        return FieldUtils.getField(entityClazz, "id");
    }

    public static <T, R> String getFieldName(LambdaFunction<T, R> lambdaFunction) {
        return SerializedLambdaUtils.getLambdaMeta(lambdaFunction).getFieldName();
    }

    @SafeVarargs
    public static <T> String[] getFieldNames(LambdaFunction<T, ?>... lambdaFields) {
        String[] fields = new String[lambdaFields.length];
        for (int i = 0; i < lambdaFields.length; i++) {
            fields[i] = getFieldName(lambdaFields[i]);
        }
        return fields;
    }

    /**
     * 获取字段
     */
    public static Field getField(Class<?> clazz, String field) {
        return fields(clazz).get(field);
    }

    /**
     * 字段 & 列
     */
    public static Map<String, String> fieldColumns(Class<?> entityClazz) {
        Map<String, String> fieldColumnMap = FIELD_COLUMN_CACHE.get(entityClazz);
        if (fieldColumnMap != null) {
            return fieldColumnMap;
        }
        if (TypeUtils.isSimpleClass(entityClazz)) {
            return new HashMap<>();
        }
        synchronized (FIELD_COLUMN_CACHE) {
            fieldColumnMap = new HashMap<>();
            Map<String, Field> fieldMap = fields(entityClazz);
            for (Field field : fieldMap.values()) {
                Column column = field.getAnnotation(Column.class);
                if (column == null || !column.ignore()) {
                    if (column != null && !"".equals(column.value())) {
                        fieldColumnMap.put(field.getName(), column.value());
                    } else {
                        fieldColumnMap.put(field.getName(), humpToLine(field.getName()));
                    }
                }
            }
            FIELD_COLUMN_CACHE.put(entityClazz, Collections.unmodifiableMap(fieldColumnMap));
        }
        return fieldColumnMap;
    }

    /**
     * 获取字段值
     */
    public static Object getValue(Object object, String field) {
        return getValue(object, field, true);
    }

    public static Object getValue(Object object, String field, boolean force) {
        try {
            Map<String, Method> methodMap = getterMethod(object.getClass());
            if (methodMap.containsKey(field)) {
                return methodMap.get(field).invoke(object);
            } else {
                Field f = getField(object, field);
                if (f != null) {
                    f.setAccessible(true);
                    return f.get(object);
                }
            }
            if (force) {
                throw new IllegalAccessException(field + " 字段不存在：" + object.getClass().getName());
            }
            return null;
        } catch (Exception e) {
            throw new DbException("字段获取值异常: " + field, e);
        }
    }

    /**
     * 赋值字段值
     */
    public static boolean setValue(Object object, String field, Object value) {
        return setValue(object, field, value, false);
    }

    /**
     * 赋值字段值
     */
    public static boolean setValue(Object object, String field, Object value, boolean typeAutoConvert) {
        try {
            Method method = getSetterMethodByField(object.getClass(), field, value != null ? value.getClass() : null);
            if (method != null) {
                if (typeAutoConvert) {
                    Class<?> parameterType = method.getParameterTypes()[0];
                    if (Object.class.equals(parameterType)) {
                        Field _field = getField(object.getClass(), field);
                        if (_field != null) {
                            parameterType = _field.getType();
                        }
                    }
                    method.invoke(object, TypeUtils.convert(value, parameterType));
                } else {
                    method.invoke(object, value);
                }
                return true;
            } else {
                Field f = getField(object, field);
                if (f != null) {
                    f.setAccessible(true);
                    if (typeAutoConvert) {
                        f.set(object, TypeUtils.convert(value, f.getType()));
                    } else {
                        f.set(object, value);
                    }
                }
                return f != null;
            }
        } catch (Exception e) {
            throw new DbException("字段赋值异常: " + field + " [" + (value == null ? null : value.getClass().getName()) + "]", e);
        }
    }

    public static Method getSetterMethodByField(Class<?> clazz, String field, Class<?> paramClass) {
        Map<String, List<Method>> methodMap = setterMethod(clazz);
        List<Method> methods = methodMap.get(field);
        if (methods == null || methods.isEmpty()) {
            return null;
        }
        if (methods.size() == 1) {
            return methods.get(0);
        }
        for (Method method : methods) {
            if (paramClass == null) {
                Field f = fields(clazz).get(field);
                if (f != null && f.getType().equals(method.getParameterTypes()[0])) {
                    return method;
                }
            } else {
                if (method.getParameterTypes()[0].isAssignableFrom(paramClass)) {
                    return method;
                }
            }
        }
        return methods.get(0);
    }

    private static Field getField(Object object, String field) {
        Map<String, Field> fieldMap = fields(object.getClass());
        Field f = fieldMap.get(field);
        if (f == null) {
            for (Field _field : fieldMap.values()) {
                Column annotation = _field.getAnnotation(Column.class);
                if (annotation != null && field.equals(annotation.value())) {
                    f = _field;
                    break;
                }
            }
        }
        return f;
    }

    public static Map<String, Method> getterMethod(Class<?> clazz) {
        Map<String, Method> result = METHOD_GET_CACHE.get(clazz);
        if (result != null) {
            return result;
        }
        synchronized (METHOD_GET_CACHE) {
            result = new HashMap<>();
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                String methodName = method.getName();
                if (!Modifier.isPublic(method.getModifiers()) || Modifier.isStatic(method.getModifiers())) {
                    continue;
                }
                if (Object.class.equals(method.getDeclaringClass())) {
                    continue;
                }
                if (method.getParameterCount() == 0 && (methodName.startsWith("get") || methodName.startsWith("is"))) {
                    String field = firstCase(methodName.substring(methodName.startsWith("is") ? 2 : 3), true);
                    result.put(field, method);
                }
            }
            METHOD_GET_CACHE.put(clazz, Collections.unmodifiableMap(result));
        }
        return result;
    }

    public static Map<String, List<Method>> setterMethod(Class<?> clazz) {
        Map<String, List<Method>> result = METHOD_SET_CACHE.get(clazz);
        if (result != null) {
            return result;
        }
        synchronized (METHOD_SET_CACHE) {
            result = new HashMap<>();
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                String methodName = method.getName();
                if (!Modifier.isPublic(method.getModifiers()) || Modifier.isStatic(method.getModifiers())) {
                    continue;
                }
                if (Object.class.equals(method.getDeclaringClass())) {
                    continue;
                }
                if (method.getParameterCount() == 1 && methodName.startsWith("set")) {
                    String field = firstCase(methodName.substring(3), true);
                    result.compute(field, (k, v) -> {
                        if (v == null) {
                            v = new ArrayList<>();
                        }
                        v.add(method);
                        return v;
                    });
                }
            }
            METHOD_SET_CACHE.put(clazz, Collections.unmodifiableMap(result));
        }
        return result;
    }

    public static Map<String, Field> fields(Class<?> clazz) {
        Map<String, Field> result = FIELD_CACHE.get(clazz);
        if (result != null) {
            return result;
        }
        synchronized (FIELD_CACHE) {
            result = new HashMap<>();
            List<Class<?>> classList = new ArrayList<>();
            Class<?> currentClazz = clazz;
            while (currentClazz != null) {
                classList.add(0, currentClazz);
                currentClazz = currentClazz.getSuperclass();
            }
            for (Class<?> c : classList) {
                Field[] fields = c.getDeclaredFields();
                for (Field field : fields) {
                    if (Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    result.put(field.getName(), field);
                }
            }
            FIELD_CACHE.put(clazz, Collections.unmodifiableMap(result));
        }
        return result;
    }

    /**
     * 下划线转驼峰
     */
    public static String lineToHump(String str) {
        str = str.trim();
        if (!str.contains("_")) {
            return str;
        }
        Matcher matcher = LINE_PATTERN.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        String result = sb.toString();
        if (str.startsWith("_")) {
            if (result.length() == 1) {
                result = result.toLowerCase();
            } else {
                result = result.substring(0, 1).toLowerCase() + result.substring(1);
            }
        }
        return result;
    }

    /**
     * 驼峰转下划线
     */
    public static String humpToLine(String str) {
        if (str == null || "".equals(str)) return str;
        Matcher matcher = HUMP_PATTERN.matcher(str.trim());
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String s = matcher.group();
            s = s.substring(0, 1) + "_" + s.substring(1).toLowerCase();
            matcher.appendReplacement(sb, s);
        }
        matcher.appendTail(sb);
        sb.setCharAt(0, String.valueOf(sb.charAt(0)).toLowerCase().charAt(0));
        return sb.toString();
    }

    public static String firstCase(String str, boolean toLowerCase) {
        if (str.length() == 1) {
            return toLowerCase ? str.toLowerCase() : str.toUpperCase();
        } else {
            return (toLowerCase ? str.substring(0, 1).toLowerCase() : str.substring(0, 1).toUpperCase()) + str.substring(1);
        }
    }

    public static Map<String, String> parsePlaceholder(String eval, String start, String end) {
        int index = -1;
        Map<String, String> result = new HashMap<>();
        while ((index = eval.indexOf(start, index + 1)) > -1) {
            int endIdx = eval.indexOf(end, index + start.length());
            if (endIdx > -1) {
                String key = eval.substring(index, endIdx + end.length());
                result.put(key, key.substring(start.length(), key.length() - 1).trim());
                index = endIdx + end.length() - 1;
            }
        }
        return result;
    }

    public static String replaceSqlPlaceholder(String eval, String tableAs, BiFunction<String, String, String> asFieldColumnFun) {
        return replaceSqlPlaceholder(eval, tableAs, asFieldColumnFun, "${", "}");
    }

    public static String replaceSqlPlaceholder(String eval, String tableAs, BiFunction<String, String, String> asFieldColumnFun, String start, String end) {
        int startIndex = 0, index, startLen = start.length();
        StringBuilder sb = new StringBuilder();
        while ((index = eval.indexOf(start, startIndex)) > -1) {
            int endIdx = eval.indexOf(end, index + startLen);
            if (endIdx > -1) {
                sb.append(eval.substring(startIndex, index));
                String key = eval.substring(index + startLen, endIdx).trim();
                int idx = key.indexOf(".");
                String as = tableAs;
                if (idx > -1) {
                    as = key.substring(0, idx);
                    sb.append(as).append(".");
                    key = key.substring(idx + 1).trim();
                } else if (tableAs != null) {
                    sb.append(tableAs).append(".");
                }
                if (asFieldColumnFun != null) {
                    sb.append(asFieldColumnFun.apply(as, key));
                } else {
                    sb.append(key);
                }
                startIndex = endIdx + end.length();
            } else {
                sb.append(eval.substring(startIndex, index));
                startIndex = index + 1;
            }
        }
        if (startIndex < eval.length()) {
            sb.append(eval.substring(startIndex));
        }
        return sb.toString();
    }

    public static List<Class<?>> fieldActualType(Field field) {
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            List<Class<?>> classes = new ArrayList<>();
            Type[] arguments = ((ParameterizedType) genericType).getActualTypeArguments();
            for (Type argument : arguments) {
                if (argument instanceof Class) {
                    classes.add((Class<?>) argument);
                } else if (argument instanceof WildcardType) {
                    classes.add((Class<?>) ((WildcardType) argument).getUpperBounds()[0]);
                }
            }
            return classes;
        }
        return null;
    }

}
