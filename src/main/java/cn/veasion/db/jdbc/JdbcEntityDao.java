package cn.veasion.db.jdbc;

import cn.veasion.db.DbException;
import cn.veasion.db.base.Filter;
import cn.veasion.db.base.IBaseId;
import cn.veasion.db.base.JdbcTypeEnum;
import cn.veasion.db.interceptor.EntityDaoInvocation;
import cn.veasion.db.interceptor.InterceptorUtils;
import cn.veasion.db.query.AbstractQuery;
import cn.veasion.db.query.SubQueryParam;
import cn.veasion.db.update.AbstractUpdate;
import cn.veasion.db.update.BatchEntityInsert;
import cn.veasion.db.update.Delete;
import cn.veasion.db.update.EntityInsert;
import cn.veasion.db.utils.LeftRight;
import cn.veasion.db.utils.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * JdbcEntityDao
 *
 * @author luozhuowei
 * @date 2021/12/5
 */
@SuppressWarnings("unchecked")
public abstract class JdbcEntityDao<T, ID> implements EntityDao<T, ID> {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    private Class<T> entityClass;
    protected DataSourceProvider dataSourceProvider = DaoUtils.dataSourceProvider();

    @Override
    public ID add(EntityInsert entityInsert) {
        entityInsert.check();
        Object entity = entityInsert.getEntity();
        Field idField = DaoUtils.getIdField(entity.getClass());
        return InterceptorUtils.intercept(new EntityDaoInvocation<>(this, "add", new Object[]{entityInsert}, () -> {
            LeftRight<String, Object[]> leftRight = DaoUtils.insert(entity.getClass(), Collections.singletonList(entityInsert.getFieldValueMap()));
            try {
                Object[] objects = JdbcDao.executeInsert(getConnection(JdbcTypeEnum.INSERT), leftRight.getLeft(), leftRight.getRight());
                if (objects.length > 0) {
                    ID id = (ID) TypeUtils.convert(objects[0], idField.getType());
                    if (entity instanceof IBaseId) {
                        ((IBaseId<ID>) entity).setId(id);
                    }
                    return id;
                }
                return null;
            } catch (SQLException e) {
                logger.error("新增异常", e);
                throw new DbException(e);
            }
        }));
    }

    @Override
    public ID[] batchAdd(BatchEntityInsert batchEntityInsert) {
        batchEntityInsert.check();
        AbstractQuery<?> insertSelectQuery = batchEntityInsert.getInsertSelectQuery();
        if (insertSelectQuery != null) {
            beforeSelect(insertSelectQuery);
        }
        return InterceptorUtils.intercept(new EntityDaoInvocation<>(this, "batchAdd", new Object[]{batchEntityInsert}, () -> {
            LeftRight<String, Object[]> leftRight;
            if (insertSelectQuery != null) {
                leftRight = DaoUtils.insertSelect(getEntityClass(), insertSelectQuery);
            } else {
                leftRight = DaoUtils.insert(getEntityClass(), batchEntityInsert.getFieldValueMapList());
            }
            Field idField = DaoUtils.getIdField(getEntityClass());
            List<?> entityList = batchEntityInsert.getEntityList();
            try {
                Object[] objects = JdbcDao.executeInsert(getConnection(JdbcTypeEnum.INSERT), leftRight.getLeft(), leftRight.getRight());
                ID[] ids = (ID[]) Array.newInstance(idField.getType(), objects.length);
                for (int i = 0; i < objects.length; i++) {
                    ID id = (ID) TypeUtils.convert(objects[i], idField.getType());
                    Array.set(ids, i, id);
                    if (entityList != null && entityList.get(i) instanceof IBaseId) {
                        ((IBaseId<ID>) entityList.get(i)).setId(id);
                    }
                }
                return ids;
            } catch (SQLException e) {
                logger.error("批量新增异常", e);
                throw new DbException(e);
            }
        }));
    }

    @Override
    public <E> E queryForType(AbstractQuery<?> query, Class<E> clazz) {
        beforeSelect(query);
        return InterceptorUtils.intercept(new EntityDaoInvocation<>(this, "queryForType", new Object[]{query, clazz}, () -> {
            LeftRight<String, Object[]> leftRight = query.sqlValue();
            try {
                return JdbcDao.queryForType(getConnection(JdbcTypeEnum.SELECT), clazz, leftRight.getLeft(), leftRight.getRight());
            } catch (Exception e) {
                logger.error("查询异常", e);
                throw new DbException(e);
            }
        }));
    }

    @Override
    public Map<String, Object> queryForMap(AbstractQuery<?> query, boolean mapUnderscoreToCamelCase) {
        beforeSelect(query);
        return InterceptorUtils.intercept(new EntityDaoInvocation<>(this, "queryForMap", new Object[]{query, mapUnderscoreToCamelCase}, () -> {
            LeftRight<String, Object[]> leftRight = query.sqlValue();
            try {
                return JdbcDao.queryForMap(getConnection(JdbcTypeEnum.SELECT), mapUnderscoreToCamelCase, leftRight.getLeft(), leftRight.getRight());
            } catch (Exception e) {
                logger.error("查询异常", e);
                throw new DbException(e);
            }
        }));
    }

    @Override
    public List<Map<String, Object>> listForMap(AbstractQuery<?> query, boolean mapUnderscoreToCamelCase) {
        beforeSelect(query);
        return InterceptorUtils.intercept(new EntityDaoInvocation<>(this, "listForMap", new Object[]{query, mapUnderscoreToCamelCase}, () -> {
            LeftRight<String, Object[]> leftRight = query.sqlValue();
            try {
                return JdbcDao.listForMap(getConnection(JdbcTypeEnum.SELECT), mapUnderscoreToCamelCase, leftRight.getLeft(), leftRight.getRight());
            } catch (Exception e) {
                logger.error("查询异常", e);
                throw new DbException(e);
            }
        }));
    }

    @Override
    public <E> List<E> queryList(AbstractQuery<?> query, Class<E> clazz) {
        beforeSelect(query);
        return InterceptorUtils.intercept(new EntityDaoInvocation<>(this, "queryList", new Object[]{query, clazz}, () -> {
            LeftRight<String, Object[]> leftRight = query.sqlValue();
            try {
                return JdbcDao.listForType(getConnection(JdbcTypeEnum.SELECT), clazz, leftRight.getLeft(), leftRight.getRight());
            } catch (Exception e) {
                logger.error("查询异常", e);
                throw new DbException(e);
            }
        }));
    }

    @Override
    public int update(AbstractUpdate<?> update) {
        update.check();
        if (update.getEntityClass() == null) {
            update.setEntityClass(getEntityClass());
        }
        beforeSubQuery(update.getFilters());
        return InterceptorUtils.intercept(new EntityDaoInvocation<>(this, "update", new Object[]{update}, () -> {
            LeftRight<String, Object[]> leftRight = update.sqlValue();
            try {
                return JdbcDao.executeUpdate(getConnection(JdbcTypeEnum.UPDATE), leftRight.getLeft(), leftRight.getRight());
            } catch (Exception e) {
                logger.error("更新异常", e);
                throw new DbException(e);
            }
        }));
    }

    @Override
    public int delete(Delete delete) {
        delete.check();
        delete.setEntityClass(getEntityClass());
        beforeSubQuery(delete.getFilters());
        return InterceptorUtils.intercept(new EntityDaoInvocation<>(this, "delete", new Object[]{delete}, () -> {
            LeftRight<String, Object[]> leftRight;
            JdbcTypeEnum jdbcTypeEnum;
            AbstractUpdate<?> convertUpdate = delete.getConvertUpdate();
            if (convertUpdate != null) {
                if (!convertUpdate.hasFilters()) {
                    delete.getFilters().forEach(convertUpdate::addFilter);
                }
                if (convertUpdate.getEntityClass() == null) {
                    convertUpdate.setEntityClass(getEntityClass());
                }
                jdbcTypeEnum = JdbcTypeEnum.UPDATE;
                leftRight = convertUpdate.sqlValue();
            } else {
                jdbcTypeEnum = JdbcTypeEnum.DELETE;
                leftRight = DaoUtils.delete(delete);
            }
            try {
                return JdbcDao.executeUpdate(getConnection(jdbcTypeEnum), leftRight.getLeft(), leftRight.getRight());
            } catch (Exception e) {
                logger.error("删除异常", e);
                throw new DbException(e);
            }
        }));
    }

    private void beforeSelect(AbstractQuery<?> query) {
        if (query.getEntityClass() == null) {
            query.setEntityClass(getEntityClass());
        }
        query.check();
        beforeSubQuery(query.getFilters());
    }

    private void beforeSubQuery(List<Filter> filters) {
        if (filters != null && !filters.isEmpty()) {
            for (Filter filter : filters) {
                if (filter.isSpecial() && filter.getValue() instanceof SubQueryParam) {
                    beforeSelect(((SubQueryParam) filter.getValue()).getQuery());
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<T> getEntityClass() {
        if (entityClass != null) {
            return entityClass;
        }
        Type genericSuperclass = getClass().getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class && JdbcEntityDao.class.isAssignableFrom((Class<?>) rawType)) {
                return (entityClass = (Class<T>) parameterizedType.getActualTypeArguments()[0]);
            }
        }
        throw new RuntimeException("获取实体类型失败，请重写 getEntityClass() 方法");
    }

    private Connection getConnection(JdbcTypeEnum jdbcTypeEnum) throws SQLException {
        if (dataSourceProvider == null) {
            throw new DbException("未获取到 dataSourceProvider");
        }
        return dataSourceProvider.getConnection(dataSourceProvider.getDataSource(this, jdbcTypeEnum));
    }

}
