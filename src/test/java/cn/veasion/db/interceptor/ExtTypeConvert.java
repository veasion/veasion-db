package cn.veasion.db.interceptor;

import cn.veasion.db.utils.TypeConvert;
import cn.veasion.db.utils.TypeUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 扩展类型转换
 *
 * @author luozhuowei
 * @date 2022/12/01
 */
public class ExtTypeConvert implements TypeConvert {

    private static final Map<Class<?>, Map<Object, IEnum<?>>> ENUM_VALUE_CACHE = new ConcurrentHashMap<>();

    @Override
    public <T> T convert(Object object, Class<T> clazz) {
        if (clazz.isEnum() && IEnum.class.isAssignableFrom(clazz)) {
            // 把值转换成枚举
            return valueToEnum(object, clazz);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> T valueToEnum(Object object, Class<T> clazz) {
        Map<Object, IEnum<?>> enumMap = ENUM_VALUE_CACHE.get(clazz);
        if (enumMap == null) {
            try {
                Method valuesMethod = clazz.getDeclaredMethod("values");
                valuesMethod.setAccessible(true);
                Object values = valuesMethod.invoke(clazz);
                enumMap = new HashMap<>();
                int length = Array.getLength(values);
                for (int i = 0; i < length; i++) {
                    IEnum<?> iEnum = (IEnum<?>) Array.get(values, i);
                    if (iEnum.getValue() == null) {
                        continue;
                    }
                    enumMap.put(iEnum.getValue(), iEnum);
                }
                ENUM_VALUE_CACHE.put(clazz, enumMap);
            } catch (Exception e) {
                return null;
            }
        }
        T value = (T) enumMap.get(object);
        if (value != null) {
            return value;
        } else if (enumMap.size() > 0) {
            Class<?> valueClass = enumMap.keySet().iterator().next().getClass();
            if (valueClass != object.getClass()) {
                return (T) enumMap.get(TypeUtils.convert(object, valueClass));
            }
        }
        return null;
    }

    @Override
    public Object convertValue(Object value) {
        if (value instanceof IEnum) {
            // 把枚举转换成值
            return ((IEnum<?>) value).getValue();
        }
        return value;
    }

    @Override
    public boolean isSimpleClass(Class<?> clazz) {
        return IEnum.class.isAssignableFrom(clazz);
    }

}
