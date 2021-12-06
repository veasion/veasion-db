package cn.veasion.db.interceptor;

/**
 * InterceptorTest
 *
 * @author luozhuowei
 * @date 2021/12/6
 */
public class InterceptorTest {

    public static void main(String[] args) {
        for (int i = 0; i < 3; i++) {
            int no = i + 1;
            InterceptorUtils.addInterceptor(new EntityDaoInterceptor() {
                @Override
                public <R> R intercept(EntityDaoInvocation<R> invocation) {
                    System.out.println("拦截器" + no + "开始");
                    R proceed = invocation.proceed();
                    System.out.println("拦截器" + no + "结束");
                    return proceed;
                }
            });
        }

        InterceptorUtils.addInterceptor(new EntityDaoInterceptor() {
            @Override
            public <R> R intercept(EntityDaoInvocation<R> invocation) {
                System.out.println("拦截器999开始");
                R proceed = invocation.proceed();
                System.out.println("拦截器999结束");
                return proceed;
            }

            @Override
            public int sortIndex() {
                return 999;
            }
        });

        InterceptorUtils.addInterceptor(new EntityDaoInterceptor() {
            @Override
            public <R> R intercept(EntityDaoInvocation<R> invocation) {
                System.out.println("拦截器666开始");
                R proceed = invocation.proceed();
                System.out.println("拦截器666结束");
                return proceed;
            }

            @Override
            public int sortIndex() {
                return 666;
            }
        });

        InterceptorUtils.addInterceptor(new EntityDaoInterceptor() {
            @Override
            public <R> R intercept(EntityDaoInvocation<R> invocation) {
                System.out.println("拦截器-1开始");
                R proceed = invocation.proceed();
                System.out.println("拦截器-1结束");
                return proceed;
            }

            @Override
            public int sortIndex() {
                return -1;
            }
        });

        EntityDaoInvocation<Boolean> invocation = new EntityDaoInvocation<>(null, "test", null, () -> {
            System.out.println("执行test方法");
            return true;
        });

        Object result = InterceptorUtils.intercept(invocation);

        System.out.println(result);
    }

}
