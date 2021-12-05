package cn.veasion.db.update;

import cn.veasion.db.utils.FieldUtils;

import java.util.HashMap;
import java.util.Map;

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
        this.entity = entity;
        fieldValueMap = new HashMap<>();
        Map<String, String> fieldColumns = FieldUtils.entityFieldColumns(entity.getClass());
        for (String field : fieldColumns.keySet()) {
            fieldValueMap.put(field, FieldUtils.getValue(entity, field));
        }
    }

    public Object getEntity() {
        return entity;
    }

    public Map<String, Object> getFieldValueMap() {
        return fieldValueMap;
    }
}
