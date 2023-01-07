package cn.veasion.db.jdbc;

import cn.veasion.db.base.Page;
import cn.veasion.db.criteria.CommonQueryCriteria;
import cn.veasion.db.query.AbstractQuery;
import cn.veasion.db.query.EntityQuery;
import cn.veasion.db.query.Query;
import cn.veasion.db.query.SubQuery;
import cn.veasion.db.update.AbstractUpdate;
import cn.veasion.db.update.BatchEntityInsert;
import cn.veasion.db.update.Delete;
import cn.veasion.db.update.EntityInsert;
import cn.veasion.db.update.EntityUpdate;
import cn.veasion.db.utils.FieldUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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
        return batchAdd(entityList, 100);
    }

    ID[] batchAdd(BatchEntityInsert batchEntityInsert);

    default T getById(ID id) {
        return query(new Query().eq(getIdField(), id));
    }

    default T query(AbstractQuery<?> query) {
        return queryForType(query, getEntityClass());
    }

    <E> E queryForType(AbstractQuery<?> query, Class<E> clazz);

    default Map<String, Object> queryForMap(AbstractQuery<?> query) {
        return queryForMap(query, true);
    }

    Map<String, Object> queryForMap(AbstractQuery<?> query, boolean mapUnderscoreToCamelCase);

    default List<Map<String, Object>> listForMap(AbstractQuery<?> query) {
        return listForMap(query, true);
    }

    List<Map<String, Object>> listForMap(AbstractQuery<?> query, boolean mapUnderscoreToCamelCase);

    default List<T> queryList(AbstractQuery<?> query) {
        return queryList(query, getEntityClass());
    }

    <E> List<E> queryList(AbstractQuery<?> query, Class<E> clazz);

    <E> List<E> queryList(CommonQueryCriteria queryCriteria, Class<E> clazz, Consumer<EntityQuery> consumer);

    default <E> List<E> queryList(CommonQueryCriteria queryCriteria, Class<E> clazz) {
        return queryList(queryCriteria, clazz, null);
    }

    default List<T> queryList(CommonQueryCriteria queryCriteria) {
        return queryList(queryCriteria, getEntityClass());
    }

    default Page<T> queryPage(AbstractQuery<?> query) {
        return queryPage(query, getEntityClass());
    }

    <E> Page<E> queryPage(AbstractQuery<?> query, Class<E> clazz);

    <E> Page<E> queryPage(CommonQueryCriteria queryCriteria, Class<E> clazz, Consumer<EntityQuery> consumer);

    default <E> Page<E> queryPage(CommonQueryCriteria queryCriteria, Class<E> clazz) {
        return queryPage(queryCriteria, clazz, null);
    }

    default Page<T> queryPage(CommonQueryCriteria queryCriteria) {
        return queryPage(queryCriteria, getEntityClass());
    }

    default int updateById(T entity) {
        return update(new EntityUpdate(entity).eq(getIdField()).excludeUpdateFilterFields().skipNullField());
    }

    int update(AbstractUpdate<?> update);

    int delete(Delete delete);

    default int deleteById(ID id) {
        return deleteByIds(Collections.singletonList(id));
    }

    default int deleteByIds(List<ID> ids) {
        return delete(new Delete().in(getIdField(), ids));
    }

    default int queryCount(AbstractQuery<?> query) {
        Integer count = queryForType(new SubQuery(query, "t").selectExpression("count(1)", "count"), Integer.class);
        return count != null ? count : 0;
    }

    default <K, E, V> Map<K, V> groupQuery(AbstractQuery<?> query, Class<E> resultClass, Function<? super E, K> keyMapper, Function<? super E, V> valueMapper) {
        List<E> list = queryList(query, resultClass);
        if (list == null || list.isEmpty()) {
            return new HashMap<>();
        }
        return list.stream().collect(Collectors.toMap(keyMapper, valueMapper, (a, b) -> a));
    }

    default <K, E> Map<K, List<E>> groupListQuery(AbstractQuery<?> query, Class<E> resultClass, Function<? super E, K> keyMapper) {
        List<E> list = queryList(query, resultClass);
        if (list == null || list.isEmpty()) {
            return new HashMap<>();
        }
        return list.stream().collect(Collectors.groupingBy(keyMapper));
    }

    default <K, E, V> Map<K, List<V>> groupListQuery(AbstractQuery<?> query, Class<E> resultClass, Function<? super E, K> keyMapper, Function<? super E, V> valueMapper) {
        List<E> list = queryList(query, resultClass);
        if (list == null || list.isEmpty()) {
            return new HashMap<>();
        }
        return list.stream().collect(Collectors.groupingBy(keyMapper, Collector.of(ArrayList::new, (l, t) -> l.add(valueMapper.apply(t)), (a, b) -> {
            a.addAll(b);
            return a;
        })));
    }

    @SuppressWarnings("unchecked")
    default ID[] batchAdd(List<T> entityList, int maxBatchSize) {
        if (entityList == null || entityList.isEmpty()) {
            Field idField = FieldUtils.getIdField(getEntityClass());
            if (idField == null) {
                return null;
            }
            return (ID[]) Array.newInstance(idField.getType(), 0);
        }
        if (entityList.size() <= maxBatchSize) {
            return batchAdd(new BatchEntityInsert(entityList));
        } else {
            List<ID> idList = new ArrayList<>(entityList.size());
            int num = entityList.size() / maxBatchSize;
            for (int i = 0; i < num; i++) {
                ID[] ids = batchAdd(new BatchEntityInsert(entityList.subList(i * maxBatchSize, (i + 1) * maxBatchSize)));
                if (ids != null && ids.length > 0) {
                    idList.addAll(Arrays.asList(ids));
                }
            }
            int last = num * maxBatchSize;
            if (entityList.size() > last) {
                ID[] ids = batchAdd(new BatchEntityInsert(entityList.subList(last, entityList.size())));
                if (ids != null && ids.length > 0) {
                    idList.addAll(Arrays.asList(ids));
                }
            }
            Field idField = FieldUtils.getIdField(getEntityClass());
            if (idField == null) {
                return null;
            }
            return idList.toArray((ID[]) Array.newInstance(idField.getType(), idList.size()));
        }
    }

    default String getIdField() {
        return FieldUtils.getIdField(getEntityClass()).getName();
    }

    Class<T> getEntityClass();

}
