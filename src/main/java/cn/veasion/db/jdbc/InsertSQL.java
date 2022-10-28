package cn.veasion.db.jdbc;

import cn.veasion.db.DbException;
import cn.veasion.db.query.AbstractQuery;
import cn.veasion.db.update.BatchEntityInsert;
import cn.veasion.db.update.EntityInsert;
import cn.veasion.db.utils.FieldUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * InsertSQL
 *
 * @author luozhuowei
 * @date 2021/12/10
 */
public class InsertSQL extends AbstractSQL<InsertSQL> {

    private EntityInsert entityInsert;
    private BatchEntityInsert batchEntityInsert;

    public InsertSQL(EntityInsert entityInsert) {
        this.entityInsert = entityInsert;
    }

    public InsertSQL(BatchEntityInsert batchEntityInsert) {
        this.batchEntityInsert = batchEntityInsert;
    }

    public static InsertSQL build(EntityInsert entityInsert) {
        return new InsertSQL(entityInsert).build();
    }

    public static InsertSQL build(BatchEntityInsert batchEntityInsert) {
        return new InsertSQL(batchEntityInsert).build();
    }

    @Override
    public InsertSQL build() {
        this.reset();
        if (entityInsert != null) {
            insert(entityInsert.getEntityClass(), Collections.singletonList(entityInsert.getFieldValueMap()), entityInsert);
        } else {
            AbstractQuery<?> insertSelectQuery = batchEntityInsert.getInsertSelectQuery();
            if (insertSelectQuery != null) {
                insertSelect(batchEntityInsert.getEntityClass(), insertSelectQuery);
            } else {
                insert(batchEntityInsert.getEntityClass(), batchEntityInsert.getFieldValueMapList(), batchEntityInsert);
            }
        }
        return this;
    }

    private void insert(Class<?> entityClazz, List<Map<String, Object>> fieldValueMapList, Object source) {
        if (fieldValueMapList == null || fieldValueMapList.isEmpty()) {
            throw new DbException("fieldValueMapList is empty");
        }
        Map<String, String> fieldColumns = FieldUtils.entityFieldColumns(entityClazz);
        Set<String> fields = fieldValueMapList.get(0).keySet();

        if ((source instanceof EntityInsert && ((EntityInsert) source).isReplace()) ||
                (source instanceof BatchEntityInsert && ((BatchEntityInsert) source).isReplace())) {
            sql.append("REPLACE INTO ");
        } else {
            sql.append("INSERT INTO ");
        }

        sql.append(getTableName(entityClazz, null, source)).append(" (");
        for (String field : fields) {
            appendInsertColumn(fieldColumns.get(field));
        }
        trimEndSql(",");
        sql.append(") VALUES");
        for (Map<String, Object> map : fieldValueMapList) {
            for (String field : fields) {
                values.add(map.get(field));
            }
            sql.append(" (").append(sqlPlaceholder(map.size())).append(")").append(",");
        }
        trimEndSql(",");
    }

    private void insertSelect(Class<?> entityClass, AbstractQuery<?> query) {
        Map<String, String> fieldColumns = FieldUtils.entityFieldColumns(entityClass);

        Map<String, String> selectFieldColumnMap = new LinkedHashMap<>();
        QuerySQL querySQL = QuerySQL.build(query, selectFieldColumnMap);

        sql.append("INSERT INTO ");
        sql.append(getTableName(entityClass, query, batchEntityInsert)).append(" (");
        for (String field : selectFieldColumnMap.keySet()) {
            appendInsertColumn(fieldColumns.getOrDefault(field, selectFieldColumnMap.getOrDefault(field, field)));
        }
        trimEndSql(",");
        sql.append(") ");
        sql.append(querySQL.getSQL());
        values.addAll(Arrays.asList(querySQL.getValues()));
    }

    private void appendInsertColumn(String column) {
        if (column.startsWith("`") && column.endsWith("`")) {
            sql.append(column).append(",");
        } else {
            sql.append("`").append(column).append("`").append(",");
        }
    }

}
