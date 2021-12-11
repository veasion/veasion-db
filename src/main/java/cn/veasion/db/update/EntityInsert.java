package cn.veasion.db.update;

import cn.veasion.db.jdbc.InsertSQL;
import cn.veasion.db.utils.FieldUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * EntityInsert
 *
 * @author luozhuowei
 * @date 2021/12/5
 */
public class EntityInsert {

    private Object entity;
    private Class<?> entityClass;
    private Map<String, Object> fieldValueMap;

    public EntityInsert(Object entity) {
        this.entity = Objects.requireNonNull(entity);
    }

    public Object getEntity() {
        return entity;
    }

    public Map<String, Object> getFieldValueMap() {
        return fieldValueMap;
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
        fieldValueMap = new HashMap<>();
        Map<String, String> fieldColumns = FieldUtils.entityFieldColumns(entityClass);
        for (String field : fieldColumns.keySet()) {
            fieldValueMap.put(field, FieldUtils.getValue(entity, field, false));
        }
    }

    public InsertSQL sqlValue() {
        return InsertSQL.build(this);
    }

}
