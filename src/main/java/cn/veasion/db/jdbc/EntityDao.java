package cn.veasion.db.jdbc;

import cn.veasion.db.base.Table;
import cn.veasion.db.query.AbstractQuery;
import cn.veasion.db.query.Query;
import cn.veasion.db.update.AbstractUpdate;
import cn.veasion.db.update.BatchEntityInsert;
import cn.veasion.db.update.Delete;
import cn.veasion.db.update.EntityInsert;
import cn.veasion.db.update.EntityUpdate;
import cn.veasion.db.utils.FieldUtils;
import java.util.List;
import java.util.Map;

/**
 * EntityDao
 *
 * @author luozhuowei
 * @date 2021/12/5
 */
public interface EntityDao<T, ID> {

    default ID add(T entity) {
        return add(new EntityInsert(entity));
    }

    ID add(EntityInsert entityInsert);

    default ID[] batchAdd(List<T> entityList) {
        return batchAdd(new BatchEntityInsert(entityList, getEntityClass()));
    }

    ID[] batchAdd(BatchEntityInsert batchEntityInsert);

    default T getById(ID id) {
        return query(new Query().eq(getIdField(), id));
    }

    default T query(AbstractQuery<?> query) {
        return queryForEntity(query, getEntityClass());
    }

    <E> E queryForEntity(AbstractQuery<?> query, Class<E> clazz);

    default Map<String, Object> queryForMap(AbstractQuery<?> query) {
        return queryForMap(query, true);
    }

    Map<String, Object> queryForMap(AbstractQuery<?> query, boolean mapUnderscoreToCamelCase);

    default List<T> queryList(AbstractQuery<?> query) {
        return queryList(query, getEntityClass());
    }

    <E> List<E> queryList(AbstractQuery<?> query, Class<E> clazz);

    default int updateById(T entity) {
        String idField = getIdField();
        Object value = FieldUtils.getValue(entity, idField);
        return update(new EntityUpdate(entity).eq(idField, value));
    }

    default int updateById(EntityUpdate update) {
        String idField = getIdField();
        Object value = FieldUtils.getValue(update.getEntity(), idField);
        update.eq(idField, value);
        return update(update);
    }

    int update(AbstractUpdate<?> update);

    default int deleteById(ID id) {
        return delete(new Delete().eq(getIdField(), id));
    }

    int delete(Delete delete);

    default String getIdField() {
        Class<?> entityClass = getEntityClass();
        Table annotation = entityClass.getAnnotation(Table.class);
        String field = "id";
        if (annotation != null) {
            field = annotation.idField();
        }
        return field;
    }

    Class<T> getEntityClass();

}
