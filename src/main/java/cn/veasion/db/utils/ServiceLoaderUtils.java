package cn.veasion.db.utils;

import cn.veasion.db.jdbc.DataSourceProvider;
import cn.veasion.db.jdbc.DynamicTableExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static Logger logger = LoggerFactory.getLogger(ServiceLoaderUtils.class);

    private static DataSourceProvider dataSourceProvider;
    private static DynamicTableExt dynamicTableExt;

    public synchronized static DataSourceProvider dataSourceProvider() {
        if (dataSourceProvider != null) {
            return dataSourceProvider;
        }
        List<DataSourceProvider> list = loadList(DataSourceProvider.class);
        if (list.size() > 0) {
            dataSourceProvider = list.get(0);
        }
        if (list.size() > 1) {
            logger.warn("发现多个dataSourceProvider");
        }
        if (dataSourceProvider == null) {
            logger.warn("dataSourceProvider未获取到实例");
        }
        return dataSourceProvider;
    }

    public synchronized static DynamicTableExt dynamicTableExt() {
        if (dynamicTableExt != null) {
            return dynamicTableExt;
        }
        return dynamicTableExt = loadOne(DynamicTableExt.class);
    }

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
