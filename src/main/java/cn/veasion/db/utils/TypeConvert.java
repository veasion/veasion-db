package cn.veasion.db.utils;

/**
 * TypeConvert
 *
 * @author luozhuowei
 * @date 2021/12/8
 */
public interface TypeConvert extends ISort {

    <T> T convert(Object object, Class<T> clazz);

    default Object convertValue(Object value) {
        return value;
    }

    default boolean isSimpleClass(Class<?> clazz) {
        return false;
    }

}
