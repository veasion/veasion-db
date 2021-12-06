package cn.veasion.db.interceptor;

/**
 * EntityDaoInterceptor
 *
 * @author luozhuowei
 * @date 2021/12/6
 */
@FunctionalInterface
public interface EntityDaoInterceptor {

    <R> R intercept(EntityDaoInvocation<R> invocation);

    default int sortIndex() {
        return 0;
    }

}
