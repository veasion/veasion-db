package cn.veasion.db.utils;

import cn.veasion.db.jdbc.DataSourceProvider;
import cn.veasion.db.jdbc.DynamicTableExt;
import cn.veasion.db.query.PageParam;

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

    private static DataSourceProvider dataSourceProvider;
    private static DynamicTableExt dynamicTableExt;
    private static TypeConvert typeConvert;
    private static PageParam pageParam;

    public static DataSourceProvider dataSourceProvider() {
        if (dataSourceProvider != null) {
            return dataSourceProvider;
        }
        synchronized (ServiceLoaderUtils.class) {
            if (dataSourceProvider != null) {
                return dataSourceProvider;
            }
            return (dataSourceProvider = loadOne(DataSourceProvider.class));
        }
    }

    public static DynamicTableExt dynamicTableExt() {
        if (dynamicTableExt != null) {
            return dynamicTableExt;
        }
        synchronized (ServiceLoaderUtils.class) {
            if (dynamicTableExt != null) {
                return dynamicTableExt;
            }
            return (dynamicTableExt = loadOne(DynamicTableExt.class));
        }
    }

    public static TypeConvert typeConvert() {
        if (typeConvert != null) {
            return typeConvert;
        }
        synchronized (ServiceLoaderUtils.class) {
            if (typeConvert != null) {
                return typeConvert;
            }
            return (typeConvert = loadOne(TypeConvert.class));
        }
    }

    public static PageParam pageParam() {
        if (pageParam != null) {
            return pageParam;
        }
        synchronized (ServiceLoaderUtils.class) {
            if (pageParam != null) {
                return pageParam;
            }
            return (pageParam = loadOne(PageParam.class));
        }
    }

    public static <T> T loadOne(Class<T> clazz) {
        List<T> list = loadList(clazz);
        return list.size() > 0 ? list.get(0) : null;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> loadList(Class<T> clazz) {
        List<T> list = new ArrayList<>();
        ServiceLoader.load(clazz).forEach(list::add);
        if (list.size() > 1 && ISort.class.isAssignableFrom(clazz)) {
            ((List<ISort>) list).sort((a, b) -> -Integer.compare(a.sort(), b.sort()));
        }
        return list;
    }

}
