package cn.veasion.db.jdbc;

import cn.veasion.db.DbException;
import cn.veasion.db.base.Expression;
import cn.veasion.db.base.IBaseId;
import cn.veasion.db.base.JdbcTypeEnum;
import cn.veasion.db.base.Page;
import cn.veasion.db.interceptor.EntityDaoInvocation;
import cn.veasion.db.interceptor.InterceptorUtils;
import cn.veasion.db.query.AbstractQuery;
import cn.veasion.db.query.PageParam;
import cn.veasion.db.query.SubQuery;
import cn.veasion.db.update.AbstractUpdate;
import cn.veasion.db.update.BatchEntityInsert;
import cn.veasion.db.update.Delete;
import cn.veasion.db.update.EntityInsert;
import cn.veasion.db.utils.FieldUtils;
import cn.veasion.db.utils.ServiceLoaderUtils;
import cn.veasion.db.utils.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * JdbcEntityDao
 *
 * @author luozhuowei
 * @date 2021/12/5
 */
public abstract class JdbcEntityDao<T, ID> implements EntityDao<T, ID> {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    private Class<T> entityClass;
    protected DataSourceProvider dataSourceProvider = ServiceLoaderUtils.dataSourceProvider();

    @Override
    @SuppressWarnings("unchecked")
    public ID add(EntityInsert entityInsert) {
        entityInsert.check(getEntityClass());
        Object entity = entityInsert.getEntity();
        Field idField = FieldUtils.getIdField(entity.getClass());
        return InterceptorUtils.intercept(new EntityDaoInvocation<>(this, "add", new Object[]{entityInsert}, () -> {
            InsertSQL insertSQL = entityInsert.sqlValue();
            return executeJdbc(JdbcTypeEnum.INSERT, connection -> {
                Object[] objects = JdbcDao.executeInsert(connection, insertSQL.getSQL(), insertSQL.getValues());
                if (objects.length > 0) {
                    ID id = (ID) TypeUtils.convert(objects[0], idField.getType());
                    if (entity instanceof IBaseId) {
                        ((IBaseId<ID>) entity).setId(id);
                    }
                    return id;
                }
                return entity instanceof IBaseId ? ((IBaseId<ID>) entity).getId() : null;
            });
        }));
    }

    @Override
    @SuppressWarnings("unchecked")
    public ID[] batchAdd(BatchEntityInsert batchEntityInsert) {
        batchEntityInsert.check(getEntityClass());
        AbstractQuery<?> insertSelectQuery = batchEntityInsert.getInsertSelectQuery();
        if (insertSelectQuery != null) {
            insertSelectQuery.check(getEntityClass());
        }
        return InterceptorUtils.intercept(new EntityDaoInvocation<>(this, "batchAdd", new Object[]{batchEntityInsert}, () -> {
            InsertSQL insertSQL = batchEntityInsert.sqlValue();
            Field idField = FieldUtils.getIdField(getEntityClass());
            List<?> entityList = batchEntityInsert.getEntityList();
            return executeJdbc(JdbcTypeEnum.INSERT, connection -> {
                Object[] objects = JdbcDao.executeInsert(connection, insertSQL.getSQL(), insertSQL.getValues());
                if (idField == null) {
                    return null;
                }
                ID[] ids = (ID[]) Array.newInstance(idField.getType(), objects.length > 0 ? objects.length : entityList.size());
                if (objects.length > 0) {
                    for (int i = 0; i < objects.length; i++) {
                        ID id = (ID) TypeUtils.convert(objects[i], idField.getType());
                        Array.set(ids, i, id);
                        if (entityList != null && entityList.get(i) instanceof IBaseId) {
                            ((IBaseId<ID>) entityList.get(i)).setId(id);
                        }
                    }
                } else {
                    for (int i = 0; i < entityList.size(); i++) {
                        if (entityList.get(i) instanceof IBaseId) {
                            Array.set(ids, i, ((IBaseId<ID>) entityList.get(i)).getId());
                        }
                    }
                }
                return ids;
            });
        }));
    }

    @Override
    public <E> E queryForType(AbstractQuery<?> query, Class<E> clazz) {
        query.check(getEntityClass());
        return InterceptorUtils.intercept(new EntityDaoInvocation<>(this, "queryForType", new Object[]{query, clazz}, () -> {
            QuerySQL querySQL = query.sqlValue();
            return executeJdbc(JdbcTypeEnum.SELECT, connection ->
                    JdbcDao.queryForType(connection, clazz, querySQL.getSQL(), querySQL.getValues())
            );
        }));
    }

    @Override
    public Map<String, Object> queryForMap(AbstractQuery<?> query, boolean mapUnderscoreToCamelCase) {
        query.check(getEntityClass());
        return InterceptorUtils.intercept(new EntityDaoInvocation<>(this, "queryForMap", new Object[]{query, mapUnderscoreToCamelCase}, () -> {
            QuerySQL querySQL = query.sqlValue();
            return executeJdbc(JdbcTypeEnum.SELECT, connection ->
                    JdbcDao.queryForMap(connection, mapUnderscoreToCamelCase, querySQL.getSQL(), querySQL.getValues())
            );
        }));
    }

    @Override
    public List<Map<String, Object>> listForMap(AbstractQuery<?> query, boolean mapUnderscoreToCamelCase) {
        query.check(getEntityClass());
        return InterceptorUtils.intercept(new EntityDaoInvocation<>(this, "listForMap", new Object[]{query, mapUnderscoreToCamelCase}, () -> {
            QuerySQL querySQL = query.sqlValue();
            return executeJdbc(JdbcTypeEnum.SELECT, connection ->
                    JdbcDao.listForMap(connection, mapUnderscoreToCamelCase, querySQL.getSQL(), querySQL.getValues())
            );
        }));
    }

    @Override
    public <E> List<E> queryList(AbstractQuery<?> query, Class<E> clazz) {
        query.check(getEntityClass());
        return InterceptorUtils.intercept(new EntityDaoInvocation<>(this, "queryList", new Object[]{query, clazz}, () -> {
            QuerySQL querySQL = query.sqlValue();
            return executeJdbc(JdbcTypeEnum.SELECT, connection ->
                    JdbcDao.listForType(connection, clazz, querySQL.getSQL(), querySQL.getValues())
            );
        }));
    }

    @Override
    public <E> Page<E> queryPage(AbstractQuery<?> query, Class<E> clazz) {
        if (query.getPageParam() == null) {
            throw new DbException("分页参数不能为空");
        }
        SubQuery countQuery = new SubQuery(query, "t").selectExpression(Expression.select("count(1)", null));
        countQuery.check(getEntityClass());
        return InterceptorUtils.intercept(new EntityDaoInvocation<>(this, "queryPage", new Object[]{query, clazz}, () -> {
            final PageParam pageParam = query.getPageParam();
            try {
                query.page(null);
                QuerySQL countQuerySQL = countQuery.sqlValue();
                return executeJdbc(JdbcTypeEnum.SELECT, connection -> {
                    Object value = JdbcDao.queryOnly(connection, countQuerySQL.getSQL(), countQuerySQL.getValues());
                    Long count = TypeUtils.convert(value, Long.class);
                    query.page(pageParam);
                    if (count > 0) {
                        QuerySQL listQuerySQL = query.sqlValue();
                        List<E> list = JdbcDao.listForType(connection, clazz, listQuerySQL.getSQL(), listQuerySQL.getValues());
                        return new Page<>(pageParam.getPage(), pageParam.getSize(), count, list);
                    } else {
                        return new Page<>(pageParam.getPage(), pageParam.getSize(), count, new ArrayList<>());
                    }
                });
            } finally {
                query.page(pageParam);
            }
        }));
    }

    @Override
    public int update(AbstractUpdate<?> update) {
        update.check(getEntityClass());
        return InterceptorUtils.intercept(new EntityDaoInvocation<>(this, "update", new Object[]{update}, () -> {
            UpdateSQL updateSQL = update.sqlValue();
            return executeJdbc(JdbcTypeEnum.UPDATE, connection ->
                    JdbcDao.executeUpdate(connection, updateSQL.getSQL(), updateSQL.getValues())
            );
        }));
    }

    @Override
    public int delete(Delete delete) {
        delete.check(getEntityClass());
        return InterceptorUtils.intercept(new EntityDaoInvocation<>(this, "delete", new Object[]{delete}, () -> {
            JdbcTypeEnum jdbcTypeEnum;
            AbstractSQL<?> abstractSQL;
            AbstractUpdate<?> convertUpdate = delete.getConvertUpdate();
            if (convertUpdate != null) {
                if (!convertUpdate.hasFilters()) {
                    delete.getFilters().forEach(convertUpdate::addFilter);
                }
                if (convertUpdate.getEntityClass() == null) {
                    convertUpdate.setEntityClass(getEntityClass());
                }
                jdbcTypeEnum = JdbcTypeEnum.UPDATE;
                abstractSQL = convertUpdate.sqlValue();
            } else {
                jdbcTypeEnum = JdbcTypeEnum.DELETE;
                abstractSQL = DeleteSQL.build(delete);
            }
            return executeJdbc(jdbcTypeEnum, connection ->
                    JdbcDao.executeUpdate(connection, abstractSQL.getSQL(), abstractSQL.getValues())
            );
        }));
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

    private <R> R executeJdbc(JdbcTypeEnum jdbcTypeEnum, Function<Connection, R> function) {
        if (dataSourceProvider == null) {
            logger.info("请通过SPI实现cn.veasion.db.jdbc.DataSourceProvider接口");
            throw new DbException("未获取到 dataSourceProvider");
        }
        DataSource dataSource = dataSourceProvider.getDataSource(this, jdbcTypeEnum);
        Connection connection;
        try {
            connection = dataSourceProvider.getConnection(dataSource);
        } catch (SQLException e) {
            throw new DbException("获取数据库连接失败", e);
        }
        try {
            return function.apply(connection);
        } finally {
            dataSourceProvider.releaseConnection(dataSource, connection);
        }
    }

}
