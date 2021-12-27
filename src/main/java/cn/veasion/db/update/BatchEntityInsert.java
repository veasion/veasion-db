package cn.veasion.db.update;

import cn.veasion.db.DbException;
import cn.veasion.db.jdbc.InsertSQL;
import cn.veasion.db.query.AbstractQuery;
import cn.veasion.db.utils.FieldUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * BatchEntityInsert
 *
 * @author luozhuowei
 * @date 2021/12/5
 */
public class BatchEntityInsert {

    private List<?> entityList;
    private Class<?> entityClass;
    private Set<String> skipFields;
    private AbstractQuery<?> insertSelectQuery;
    private List<Map<String, Object>> fieldValueMapList;

    public <T> BatchEntityInsert(List<T> entityList) {
        this.entityList = Objects.requireNonNull(entityList);
    }

    public <T> BatchEntityInsert(List<T> entityList, String... skipFields) {
        this(entityList);
        if (skipFields.length > 0) {
            this.skipFields = new HashSet<>(Arrays.asList(skipFields));
        }
    }

    public BatchEntityInsert(AbstractQuery<?> insertSelectQuery) {
        this.insertSelectQuery = Objects.requireNonNull(insertSelectQuery);
    }

    public List<?> getEntityList() {
        return entityList;
    }

    public List<Map<String, Object>> getFieldValueMapList() {
        return fieldValueMapList;
    }

    public AbstractQuery<?> getInsertSelectQuery() {
        return insertSelectQuery;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    public void check(Class<?> mainEntityClass) {
        if (entityClass == null) {
            setEntityClass(mainEntityClass);
        }
        if (insertSelectQuery != null) {
            return;
        }
        if (entityList == null || entityList.isEmpty()) {
            throw new DbException("批量新增对象不能为空");
        }
        fieldValueMapList = new ArrayList<>(entityList.size());
        Map<String, String> fieldColumns = FieldUtils.entityFieldColumns(entityClass);
        Map<String, Object> fieldValueMap;
        for (Object entity : entityList) {
            fieldValueMap = new HashMap<>();
            for (String field : fieldColumns.keySet()) {
                if (skipFields != null && skipFields.contains(field)) {
                    continue;
                }
                fieldValueMap.put(field, FieldUtils.getValue(entity, field, false));
            }
            fieldValueMapList.add(fieldValueMap);
        }
    }

    public InsertSQL sqlValue() {
        return InsertSQL.build(this);
    }

}
