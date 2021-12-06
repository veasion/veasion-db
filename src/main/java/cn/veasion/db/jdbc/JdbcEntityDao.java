package cn.veasion.db.jdbc;

import cn.veasion.db.interceptor.EntityDaoInvocation;
import cn.veasion.db.interceptor.InterceptorUtils;
import cn.veasion.db.query.AbstractQuery;
import cn.veasion.db.update.AbstractUpdate;
import cn.veasion.db.update.BatchEntityInsert;
import cn.veasion.db.update.Delete;
import cn.veasion.db.update.EntityInsert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ID add(EntityInsert entityInsert) {
        return InterceptorUtils.intercept(new EntityDaoInvocation<>(this, "add", new Object[]{entityInsert}, () -> {
            return null;
        }));
    }

    @Override
    public ID[] batchAdd(BatchEntityInsert batchEntityInsert) {
        return InterceptorUtils.intercept(new EntityDaoInvocation<>(this, "batchAdd", new Object[]{batchEntityInsert}, () -> {
            return null;
        }));
    }

    @Override
    public <E> E queryForType(AbstractQuery<?> query, Class<E> clazz) {
        return InterceptorUtils.intercept(new EntityDaoInvocation<>(this, "queryForType", new Object[]{query, clazz}, () -> {
            return null;
        }));
    }

    @Override
    public Map<String, Object> queryForMap(AbstractQuery<?> query, boolean mapUnderscoreToCamelCase) {
        return InterceptorUtils.intercept(new EntityDaoInvocation<>(this, "queryForMap", new Object[]{query, mapUnderscoreToCamelCase}, () -> {
            return null;
        }));
    }

    @Override
    public <E> List<E> queryList(AbstractQuery<?> query, Class<E> clazz) {
        return InterceptorUtils.intercept(new EntityDaoInvocation<>(this, "queryList", new Object[]{query, clazz}, () -> {
            return null;
        }));
    }

    @Override
    public int update(AbstractUpdate<?> update) {
        return InterceptorUtils.intercept(new EntityDaoInvocation<>(this, "update", new Object[]{update}, () -> {
            return null;
        }));
    }

    @Override
    public int delete(Delete delete) {
        return InterceptorUtils.intercept(new EntityDaoInvocation<>(this, "delete", new Object[]{delete}, () -> {
            return null;
        }));
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

    protected JdbcDao jdbcDao = jdbcDao();

    private JdbcDao jdbcDao() {
        ServiceLoader<JdbcDao> serviceLoader = ServiceLoader.load(JdbcDao.class);
        Iterator<JdbcDao> iterator = serviceLoader.iterator();
        if (iterator.hasNext()) {
            jdbcDao = iterator.next();
        }
        if (iterator.hasNext()) {
            logger.warn("发现多个jdbcDao实例");
        }
        return jdbcDao;
    }

}
