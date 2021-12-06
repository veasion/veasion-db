package cn.veasion.db.interceptor;

/**
 * DefaultInterceptor
 *
 * @author luozhuowei
 * @date 2021/12/6
 */
public class DefaultInterceptor implements EntityDaoInterceptor {

    private static DefaultInterceptor defaultInterceptor = new DefaultInterceptor();

    private DefaultInterceptor() {
    }

    public static DefaultInterceptor getInstance() {
        return defaultInterceptor;
    }

    @Override
    public <R> R intercept(EntityDaoInvocation<R> invocation) {
        return invocation.proceed();
    }

}
