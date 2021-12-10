package cn.veasion.db.jdbc;

import cn.veasion.db.DbException;
import cn.veasion.db.query.AbstractQuery;
import cn.veasion.db.update.BatchEntityInsert;
import cn.veasion.db.update.EntityInsert;
import cn.veasion.db.utils.FieldUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
            insert(entityInsert.getEntityClass(), Collections.singletonList(entityInsert.getFieldValueMap()));
        } else {
            AbstractQuery<?> insertSelectQuery = batchEntityInsert.getInsertSelectQuery();
            if (insertSelectQuery != null) {
                insertSelect(batchEntityInsert.getEntityClass(), insertSelectQuery);
            } else {
                insert(batchEntityInsert.getEntityClass(), batchEntityInsert.getFieldValueMapList());
            }
        }
        return this;
    }

    private void insert(Class<?> entityClazz, List<Map<String, Object>> fieldValueMapList) {
        if (fieldValueMapList == null || fieldValueMapList.isEmpty()) {
            throw new DbException("fieldValueMapList is empty");
        }
        Map<String, String> fieldColumns = FieldUtils.entityFieldColumns(entityClazz);
        Set<String> fields = fieldValueMapList.get(0).keySet();

        sql.append("INSERT INTO ");
        sql.append(getTableName(entityClazz)).append(" (");
        for (String field : fields) {
            sql.append("`").append(fieldColumns.get(field)).append("`").append(",");
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

        List<String> insertFields = new ArrayList<>();
        QuerySQL querySQL = QuerySQL.build(query, insertFields);

        sql.append("INSERT INTO ");
        sql.append(getTableName(entityClass)).append(" (");
        for (String field : insertFields) {
            sql.append("`").append(fieldColumns.getOrDefault(field, field)).append("`").append(",");
        }
        trimEndSql(",");
        sql.append(") ");
        sql.append(querySQL.getSQL());
        values.addAll(Arrays.asList(querySQL.getValues()));
    }

}
