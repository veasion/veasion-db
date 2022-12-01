package cn.veasion.db.interceptor;

import cn.veasion.db.AbstractFilter;
import cn.veasion.db.base.Filter;
import cn.veasion.db.update.Delete;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * SAAS数据隔离拦截器
 *
 * @author luozhuowei
 * @date 2022/12/01
 */
public class TenantInterceptor extends AbstractInterceptor {

    private static final String TENANT_ID = "tenantId";
    private static final Long DEFAULT_TENANT_ID = -1L;

    private static ThreadLocal<Boolean> skipThreadLocal = new ThreadLocal<>();
    private static ThreadLocal<Long> tenantThreadLocal = new ThreadLocal<>();

    public TenantInterceptor() {
        super(true, true, true, true, true);
    }

    public static boolean isSkip() {
        return Boolean.TRUE.equals(skipThreadLocal.get());
    }

    public static void skip(boolean skip) {
        skipThreadLocal.set(skip);
    }

    public static void clearSkip() {
        skipThreadLocal.remove();
    }

    public static <R> R withSkip(Supplier<R> supplier) {
        Boolean flag = skipThreadLocal.get();
        try {
            skipThreadLocal.set(true);
            return supplier.get();
        } finally {
            if (flag != null) {
                skipThreadLocal.set(flag);
            } else {
                skipThreadLocal.remove();
            }
        }
    }

    public static <R> R withTenantId(Long tenantId, Supplier<R> supplier) {
        Long oldTenantId = tenantThreadLocal.get();
        try {
            tenantThreadLocal.set(tenantId);
            return supplier.get();
        } finally {
            if (oldTenantId != null) {
                tenantThreadLocal.set(oldTenantId);
            } else {
                tenantThreadLocal.remove();
            }
        }
    }

    public static Long setTenantId(Long tenantId) {
        Long oldTenantId = tenantThreadLocal.get();
        tenantThreadLocal.set(tenantId);
        return oldTenantId;
    }

    public static void clearTenantId() {
        tenantThreadLocal.remove();
    }

    @Override
    protected boolean skip() {
        return Boolean.TRUE.equals(skipThreadLocal.get());
    }

    @Override
    protected boolean containSkipClass(Class<?> clazz) {
        if (!ITenantId.class.isAssignableFrom(clazz)) {
            return true;
        }
        return super.containSkipClass(clazz);
    }

    @Override
    protected void handleDelete(Delete delete) {
        if (!delete.hasFilter(TENANT_ID)) {
            delete.in(TENANT_ID, authTenantIds());
        }
    }

    @Override
    protected void handleOnFilter(Object joinParam, Supplier<List<Filter>> onFilters, Consumer<Filter> onMethod, String tableAs) {
        onMethod.accept(Filter.AND);
        onMethod.accept(Filter.in(TENANT_ID, authTenantIds()).fieldAs(tableAs));
    }

    @Override
    protected void handleFilter(AbstractFilter<?> abstractFilter) {
        if (!abstractFilter.hasFilter(TENANT_ID)) {
            abstractFilter.in(TENANT_ID, authTenantIds());
        }
    }

    @Override
    protected void handleInsert(Class<?> entityClass, List<?> entityList, List<Map<String, Object>> fieldValueMapList) {
        if (entityList == null) {
            return;
        }
        for (Object entity : entityList) {
            if (entity instanceof ITenantId) {
                ITenantId tenant = (ITenantId) entity;
                if (tenant.getTenantId() != null) {
                    continue;
                }
                tenant.setTenantId(userTenantId());
            }
        }
    }

    protected Long userTenantId() {
        Long tenantId = tenantThreadLocal.get();
        if (tenantId != null) {
            return tenantId;
        }
        // 这里可以扩展从当前登录用户的 tenantId
        // return SessionHelper.getUserTenantId();
        return DEFAULT_TENANT_ID;
    }

    protected Long[] authTenantIds() {
        Long tenantId = tenantThreadLocal.get();
        if (tenantId != null) {
            return new Long[]{DEFAULT_TENANT_ID, tenantId};
        } else {
            // 这里可以扩展从当前登录用户权限的 tenantId 集合
            // return SessionHelper.getUserAuthTenantIds();
            return new Long[]{DEFAULT_TENANT_ID};
        }
    }

    public static Long getThreadLocalTenantId() {
        return tenantThreadLocal.get();
    }

}
