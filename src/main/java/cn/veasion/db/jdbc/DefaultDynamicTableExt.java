package cn.veasion.db.jdbc;

import cn.veasion.db.AbstractFilter;
import cn.veasion.db.DbException;
import cn.veasion.db.query.AbstractJoinQuery;
import cn.veasion.db.query.AbstractQuery;
import cn.veasion.db.query.JoinQueryParam;
import cn.veasion.db.update.AbstractUpdate;
import cn.veasion.db.update.BatchEntityInsert;
import cn.veasion.db.update.Delete;
import cn.veasion.db.update.EntityInsert;
import cn.veasion.db.update.EntityUpdate;
import cn.veasion.db.update.JoinUpdateParam;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * DefaultDynamicTableExt
 *
 * @author luozhuowei
 * @date 2022/1/30
 */
public class DefaultDynamicTableExt implements DynamicTableExt {

    private static final ThreadLocal<DynamicTableExt> threadLocal = new ThreadLocal<>();
    private static final Map<Class<?>, DynamicTableExt> globalMap = new ConcurrentHashMap<>();

    @Override
    public String getTableName(String tableName, Class<?> entityClazz, AbstractFilter<?> filter, Object source) {
        DynamicTableExt dynamicTableExt = threadLocal.get();
        if (dynamicTableExt != null) {
            String changeTableName = dynamicTableExt.getTableName(tableName, entityClazz, filter, source);
            if (changeTableName != null) {
                return changeTableName;
            }
        }
        if (globalMap.containsKey(entityClazz)) {
            String changeTableName = globalMap.get(entityClazz).getTableName(tableName, entityClazz, filter, source);
            if (changeTableName != null) {
                return changeTableName;
            }
        }
        return tableName;
    }

    public static <T> T withDynamicTableExt(DynamicTableExt dynamicTableExt, Supplier<T> supplier) {
        try {
            threadLocal.set(dynamicTableExt);
            return supplier.get();
        } finally {
            threadLocal.remove();
        }
    }

    public static void addGlobalDynamicTableExt(Class<?> entityClazz, DynamicTableExt tableExt) {
        globalMap.put(entityClazz, tableExt);
    }

    public static abstract class AbstractDynamicTableExt implements DynamicTableExt {

        @Override
        public String getTableName(String tableName, Class<?> entityClazz, AbstractFilter<?> filter, Object source) {
            if (source instanceof EntityInsert) {
                // insert
                tableName = handleAdd(tableName, entityClazz, (EntityInsert) source);
            } else if (source instanceof BatchEntityInsert) {
                if (filter != null) {
                    // insert select
                    tableName = handleBatchAdd(tableName, entityClazz, (BatchEntityInsert) source, true);
                } else {
                    // batch insert
                    tableName = handleBatchAdd(tableName, entityClazz, (BatchEntityInsert) source, false);
                }
            } else if (source instanceof JoinUpdateParam) {
                // update join
                tableName = handleUpdateJoin(tableName, entityClazz, (JoinUpdateParam) source, (EntityUpdate) filter);
            } else if (source instanceof JoinQueryParam) {
                // query join
                tableName = handleQueryJoin(tableName, entityClazz, (JoinQueryParam) source, (AbstractJoinQuery<?>) filter);
            } else if (source instanceof Delete) {
                // delete
                tableName = handleDelete(tableName, entityClazz, (Delete) source);
            } else if (filter instanceof AbstractQuery) {
                // query
                tableName = handleQuery(tableName, entityClazz, (AbstractQuery<?>) filter);
            } else if (filter instanceof AbstractUpdate) {
                // update
                tableName = handleUpdate(tableName, entityClazz, (AbstractUpdate<?>) filter);
            } else {
                // other filter
                tableName = handleFilter(tableName, entityClazz, filter);
            }
            return tableName;
        }

        protected abstract String handleAdd(String tableName, Class<?> entityClazz, EntityInsert entityInsert);

        protected String handleBatchAdd(String tableName, Class<?> entityClazz, BatchEntityInsert batchEntityInsert, boolean insertSelect) {
            throw new DbException("批量新增不支持动态分表，请通过路由条件拆分调用 DefaultDynamicTableExt.withDynamicTableExt 自定义处理");
        }

        protected String handleUpdateJoin(String tableName, Class<?> entityClazz, JoinUpdateParam join, EntityUpdate joinUpdate) {
            return handleFilter(tableName, entityClazz, joinUpdate);
        }

        protected String handleQueryJoin(String tableName, Class<?> entityClazz, JoinQueryParam join, AbstractJoinQuery<?> joinQuery) {
            return handleFilter(tableName, entityClazz, joinQuery);
        }

        protected String handleDelete(String tableName, Class<?> entityClazz, Delete delete) {
            return handleFilter(tableName, entityClazz, delete);
        }

        protected String handleQuery(String tableName, Class<?> entityClazz, AbstractQuery<?> query) {
            return handleFilter(tableName, entityClazz, query);
        }

        protected String handleUpdate(String tableName, Class<?> entityClazz, AbstractUpdate<?> update) {
            return handleFilter(tableName, entityClazz, update);
        }

        protected abstract String handleFilter(String tableName, Class<?> entityClazz, AbstractFilter<?> filter);
    }

}
