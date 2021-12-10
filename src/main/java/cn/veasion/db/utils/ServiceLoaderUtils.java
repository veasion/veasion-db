package cn.veasion.db.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * ServiceLoaderUtils
 *
 * @author luozhuowei
 * @date 2021/12/10
 */
public class ServiceLoaderUtils {

    public static <T> T loadOne(Class<T> clazz) {
        List<T> list = loadList(clazz);
        return list.size() > 0 ? list.get(0) : null;
    }

    public static <T> List<T> loadList(Class<T> clazz) {
        List<T> list = new ArrayList<>();
        ServiceLoader.load(clazz).forEach(list::add);
        return list;
    }

}
