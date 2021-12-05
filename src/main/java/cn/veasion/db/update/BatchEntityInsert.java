package cn.veasion.db.update;

import cn.veasion.db.utils.FieldUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BatchEntityInsert
 *
 * @author luozhuowei
 * @date 2021/12/5
 */
public class BatchEntityInsert {

    private List<?> entityList;
    private List<Map<String, Object>> fieldValueMapList;

    public <T> BatchEntityInsert(List<T> entityList, Class<T> clazz) {
        this.entityList = entityList;
        fieldValueMapList = new ArrayList<>(entityList.size());
        Map<String, String> fieldColumns = FieldUtils.entityFieldColumns(clazz);
        Map<String, Object> fieldValueMap;
        for (T entity : entityList) {
            fieldValueMap = new HashMap<>();
            for (String field : fieldColumns.keySet()) {
                fieldValueMap.put(field, FieldUtils.getValue(entity, field));
            }
            fieldValueMapList.add(fieldValueMap);
        }
    }

    public List<?> getEntityList() {
        return entityList;
    }

    public List<Map<String, Object>> getFieldValueMapList() {
        return fieldValueMapList;
    }
}
