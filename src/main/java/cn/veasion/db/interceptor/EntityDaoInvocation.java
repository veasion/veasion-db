package cn.veasion.db.interceptor;

import java.util.function.Supplier;

/**
 * EntityDaoInvocation
 *
 * @author luozhuowei
 * @date 2021/12/6
 */
public class EntityDaoInvocation<R> {

    private final Object target;
    private final String methodName;
    private final Object[] args;
    private Supplier<R> supplier;

    public EntityDaoInvocation(Object target, String methodName, Object[] args, Supplier<R> supplier) {
        this.target = target;
        this.methodName = methodName;
        this.args = args;
        this.supplier = supplier;
    }

    public static <R> EntityDaoInvocation<R> build(EntityDaoInvocation<R> invocation, Supplier<R> supplier) {
        return new EntityDaoInvocation<>(invocation.getTarget(), invocation.getMethodName(), invocation.getArgs(), supplier);
    }

    public Object getTarget() {
        return this.target;
    }

    public String getMethodName() {
        return methodName;
    }

    public Object[] getArgs() {
        return this.args;
    }

    public R proceed() {
        return supplier.get();
    }

}
