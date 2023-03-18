package cn.veasion.db.update;

import cn.veasion.db.DbException;
import cn.veasion.db.jdbc.InsertSQL;
import cn.veasion.db.utils.FieldUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * EntityInsert
 *
 * @author luozhuowei
 * @date 2021/12/5
 */
public class EntityInsert implements Serializable {

    private Object entity;
    private Class<?> entityClass;
    private Set<String> skipFields;
    private boolean useGeneratedKeys = true;
    private Map<String, Object> fieldValueMap;
    private boolean replace;
    private Set<String> duplicateKeyUpdateByFields;

    public EntityInsert(Object entity) {
        this.entity = Objects.requireNonNull(entity);
    }

    public EntityInsert(Object entity, String... skipFields) {
        this(entity);
        if (skipFields.length > 0) {
            this.skipFields = new HashSet<>(Arrays.asList(skipFields));
        }
    }

    public Object getEntity() {
        return entity;
    }

    public Map<String, Object> getFieldValueMap() {
        return fieldValueMap;
    }

    public EntityInsert setUseGeneratedKeys(boolean useGeneratedKeys) {
        this.useGeneratedKeys = useGeneratedKeys;
        return this;
    }

    public boolean isUseGeneratedKeys() {
        return useGeneratedKeys;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    public EntityInsert withReplace() {
        if (this.duplicateKeyUpdateByFields != null) {
            throw new DbException("withDuplicateKeyUpdate 不能和 withReplace同时调用");
        }
        this.replace = true;
        return this;
    }

    public EntityInsert withDuplicateKeyUpdate(String... fields) {
        if (fields.length == 0) {
            throw new DbException("fields 不能为空");
        }
        if (this.replace) {
            throw new DbException("withDuplicateKeyUpdate 不能和 withReplace同时调用");
        }
        this.duplicateKeyUpdateByFields = new HashSet<>(Arrays.asList(fields));
        return this;
    }

    public boolean isReplace() {
        return replace;
    }

    public Set<String> getDuplicateKeyUpdateByFields() {
        return duplicateKeyUpdateByFields;
    }

    public void check(Class<?> mainEntityClass) {
        if (entityClass == null) {
            setEntityClass(mainEntityClass);
        }
        fieldValueMap = new HashMap<>();
        Map<String, String> fieldColumns = FieldUtils.entityFieldColumns(entityClass);
        for (String field : fieldColumns.keySet()) {
            if (skipFields != null && skipFields.contains(field)) {
                continue;
            }
            fieldValueMap.put(field, FieldUtils.getValue(entity, field, false));
        }
    }

    public InsertSQL sqlValue() {
        return InsertSQL.build(this);
    }

}
