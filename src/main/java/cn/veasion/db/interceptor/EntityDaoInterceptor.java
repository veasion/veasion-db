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

    /**
     * 拦截器执行排序，sortIndex值越大越先执行
     */
    default int sortIndex() {
        return 0;
    }

}
