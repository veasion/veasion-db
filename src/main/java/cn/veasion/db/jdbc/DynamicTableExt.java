package cn.veasion.db.jdbc;

import cn.veasion.db.AbstractFilter;
import cn.veasion.db.utils.ISort;

/**
 * DynamicTableExt
 *
 * @author luozhuowei
 * @date 2022/1/30
 */
public interface DynamicTableExt extends ISort {

    String getTableName(String tableName, Class<?> entityClazz, AbstractFilter<?> filter, Object source);

}
