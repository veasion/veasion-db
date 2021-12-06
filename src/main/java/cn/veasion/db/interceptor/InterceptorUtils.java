package cn.veasion.db.interceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.Supplier;

/**
 * InterceptorUtils
 *
 * @author luozhuowei
 * @date 2021/12/6
 */
public class InterceptorUtils {

    static final List<EntityDaoInterceptor> interceptors = new ArrayList<>();

    static {
        ServiceLoader<EntityDaoInterceptor> serviceLoader = ServiceLoader.load(EntityDaoInterceptor.class);
        for (EntityDaoInterceptor entityDaoInterceptor : serviceLoader) {
            interceptors.add(entityDaoInterceptor);
        }
        sortInterceptor();
    }

    public static void addInterceptor(EntityDaoInterceptor interceptor) {
        interceptors.add(Objects.requireNonNull(interceptor));
        sortInterceptor();
    }

    private static void sortInterceptor() {
        interceptors.sort(Comparator.comparingInt(EntityDaoInterceptor::sortIndex));
    }

    public static List<EntityDaoInterceptor> getInterceptors() {
        return Collections.unmodifiableList(interceptors);
    }

    public static <R> R intercept(EntityDaoInvocation<R> invocation) {
        if (interceptors.isEmpty()) {
            return DefaultInterceptor.getInstance().intercept(invocation);
        }
        if (interceptors.size() == 1) {
            return interceptors.get(0).intercept(invocation);
        }
        Supplier<R> supplier = null;
        for (int i = 0; i < interceptors.size() - 1; i++) {
            EntityDaoInterceptor interceptor = interceptors.get(i);
            Supplier<R> pre = supplier != null ? supplier : invocation::proceed;
            supplier = () -> interceptor.intercept(EntityDaoInvocation.build(invocation, pre));
        }
        return interceptors.get(interceptors.size() - 1).intercept(EntityDaoInvocation.build(invocation, supplier));
    }

}
