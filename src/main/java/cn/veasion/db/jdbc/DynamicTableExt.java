package cn.veasion.db.jdbc;

import cn.veasion.db.AbstractFilter;

/**
 * DynamicTableExt
 *
 * @author luozhuowei
 * @date 2022/1/30
 */
public interface DynamicTableExt {

    String getTableName(String tableName, Class<?> entityClazz, AbstractFilter<?> filter, Object source);

    default int sort() {
        return 0;
    }

}
