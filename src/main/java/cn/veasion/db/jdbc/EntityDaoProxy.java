package cn.veasion.db.jdbc;

import cn.veasion.db.DbException;
import cn.veasion.db.interceptor.EntityDaoInvocation;
import cn.veasion.db.interceptor.InterceptorUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * EntityDaoProxy
 *
 * @author luozhuowei
 * @date 2021/12/6
 */
@Deprecated
public class EntityDaoProxy implements InvocationHandler {

    private EntityDao<?, ?> entityDao;

    public EntityDaoProxy(EntityDao<?, ?> entityDao) {
        this.entityDao = entityDao;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return InterceptorUtils.intercept(new EntityDaoInvocation<>(entityDao, method.getName(), args, () -> {
            try {
                return method.invoke(entityDao, args);
            } catch (Exception e) {
                throw new DbException(e);
            }
        }));
    }

    public static EntityDao<?, ?> getProxyInstance(EntityDao<?, ?> entityDao) {
        return (EntityDao<?, ?>) Proxy.newProxyInstance(entityDao.getClass().getClassLoader(),
                entityDao.getClass().getInterfaces(), new EntityDaoProxy(entityDao));
    }

}
