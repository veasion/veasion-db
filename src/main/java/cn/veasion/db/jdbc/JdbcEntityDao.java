package cn.veasion.db.jdbc;

import cn.veasion.db.query.AbstractQuery;
import cn.veasion.db.update.AbstractUpdate;
import cn.veasion.db.update.BatchEntityInsert;
import cn.veasion.db.update.Delete;
import cn.veasion.db.update.EntityInsert;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * JdbcEntityDao
 *
 * @author luozhuowei
 * @date 2021/12/5
 */
public abstract class JdbcEntityDao<T, ID> implements EntityDao<T, ID> {

    @Override
    public ID add(EntityInsert entityInsert) {
        return null;
    }

    @Override
    public ID[] batchAdd(BatchEntityInsert batchEntityInsert) {
        return null;
    }

    @Override
    public <E> E queryForEntity(AbstractQuery<?> query, Class<E> clazz) {
        return null;
    }

    @Override
    public Map<String, Object> queryForMap(AbstractQuery<?> query, boolean mapUnderscoreToCamelCase) {
        return null;
    }

    @Override
    public <E> List<E> queryList(AbstractQuery<?> query, Class<E> clazz) {
        return null;
    }

    @Override
    public int update(AbstractUpdate<?> update) {
        return 0;
    }

    @Override
    public int delete(Delete delete) {
        return 0;
    }

    private JdbcDao jdbcDao;

    private JdbcDao jdbcDao() {
        if (jdbcDao != null) {
            return jdbcDao;
        }
        ServiceLoader<JdbcDao> jdbcDaoServiceLoader = ServiceLoader.load(JdbcDao.class);
        Iterator<JdbcDao> iterator = jdbcDaoServiceLoader.iterator();
        if (iterator.hasNext()) {
            return (jdbcDao = iterator.next());
        }
        return jdbcDao;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<T> getEntityClass() {
        Type genericSuperclass = getClass().getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
            if (JdbcEntityDao.class.equals(parameterizedType.getRawType())) {
                return (Class<T>) parameterizedType.getActualTypeArguments()[0];
            }
        }
        throw new RuntimeException("获取实体类型失败，请重写 getEntityClass() 方法");
    }

}
