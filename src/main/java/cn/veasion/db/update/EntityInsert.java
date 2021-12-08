package cn.veasion.db.update;

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

    public void check() {
        fieldValueMap = new HashMap<>();
        Map<String, String> fieldColumns = FieldUtils.entityFieldColumns(entity.getClass());
        for (String field : fieldColumns.keySet()) {
            fieldValueMap.put(field, FieldUtils.getValue(entity, field));
        }
    }

}
