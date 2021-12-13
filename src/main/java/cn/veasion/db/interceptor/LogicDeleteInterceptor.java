package cn.veasion.db.interceptor;

import cn.veasion.db.AbstractFilter;
import cn.veasion.db.base.Expression;
import cn.veasion.db.base.Filter;
import cn.veasion.db.query.AbstractJoinQuery;
import cn.veasion.db.query.AbstractQuery;
import cn.veasion.db.query.JoinQueryParam;
import cn.veasion.db.query.SubQuery;
import cn.veasion.db.query.SubQueryParam;
import cn.veasion.db.query.UnionQueryParam;
import cn.veasion.db.update.AbstractUpdate;
import cn.veasion.db.update.Delete;
import cn.veasion.db.update.EntityUpdate;
import cn.veasion.db.update.JoinUpdateParam;
import cn.veasion.db.update.Update;
import cn.veasion.db.utils.FilterUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 逻辑删除拦截器
 *
 * @author luozhuowei
 * @date 2021/12/6
 */
public class LogicDeleteInterceptor implements EntityDaoInterceptor {

    private static ThreadLocal<Boolean> skipLogicDeleteFilter = new ThreadLocal<>();
    private static ThreadLocal<Set<Class<?>>> skipClassLogicDeleteFilter = new ThreadLocal<>();

    private String logicDeleteField;
    private Object availableValue;
    private Object deletedValue;

    /**
     * 逻辑删除处理拦截器
     *
     * @param logicDeleteField 逻辑删除字段，标识是否删除的字段，如：isDeleted <br><br>
     * @param availableValue   表示可用未删除的值，如： 0，当查询条件没有逻辑删除字段条件时默认加上该条件值 <br><br>
     * @param deletedValue     字段值表示删除，当调用删除时默认会转update更新逻辑删除字段，如：1，<br>
     *                         如需要更新特殊为特殊值，如 id 或者 userId 时可以传 Expression 对象，如 Expression.update("${id}")
     */
    public LogicDeleteInterceptor(String logicDeleteField, Object availableValue, Object deletedValue) {
        this.logicDeleteField = Objects.requireNonNull(logicDeleteField);
        this.availableValue = Objects.requireNonNull(availableValue);
        this.deletedValue = Objects.requireNonNull(deletedValue);
    }

    /**
     * 跳过逻辑删除过滤
     *
     * @param skip 是否跳过
     */
    public static void skip(boolean skip) {
        skipLogicDeleteFilter.set(skip);
    }

    /**
     * 指定类跳过逻辑删除过滤
     *
     * @param classes 指定类跳过逻辑删除过滤
     */
    public static void skip(Class<?>... classes) {
        skipClassLogicDeleteFilter.set(new HashSet<>(Arrays.asList(classes)));
    }

    public static void clearSkip() {
        skipLogicDeleteFilter.remove();
        skipClassLogicDeleteFilter.remove();
    }

    @Override
    public <R> R intercept(EntityDaoInvocation<R> invocation) {
        try {
            Object[] args = invocation.getArgs();
            if (args != null && !Boolean.TRUE.equals(skipLogicDeleteFilter.get())) {
                for (Object arg : args) {
                    if (arg instanceof AbstractQuery) {
                        handleQuery((AbstractQuery<?>) arg);
                    } else if (arg instanceof AbstractUpdate) {
                        handleUpdate((AbstractUpdate<?>) arg);
                    } else if (arg instanceof Delete) {
                        handleDelete((Delete) arg);
                    } else if (arg instanceof AbstractFilter) {
                        handleFilter((AbstractFilter<?>) arg);
                    }
                }
            }
        } finally {
            clearSkip();
        }
        return invocation.proceed();
    }

    private boolean containSkipClass(AbstractFilter<?> filter) {
        return skipClassLogicDeleteFilter.get() != null &&
                skipClassLogicDeleteFilter.get().contains(filter.getEntityClass());
    }

    private void handleQuery(AbstractQuery<?> query) {
        if (query instanceof SubQuery) {
            handleQuery(((SubQuery) query).getSubQuery());
        }
        handleSelectSubQuery(query.getSelectSubQueryList());
        handleFilter(query);
        if (query instanceof AbstractJoinQuery) {
            List<JoinQueryParam> joinList = ((AbstractJoinQuery<?>) query).getJoinAll();
            if (joinList != null) {
                for (JoinQueryParam joinQueryParam : joinList) {
                    AbstractJoinQuery<?> joinQuery = joinQueryParam.getJoinQuery();
                    if (joinQuery instanceof SubQuery) {
                        handleQuery(((SubQuery) joinQuery).getSubQuery());
                    } else {
                        if (!containSkipClass(joinQuery)) {
                            handleSelectSubQuery(joinQuery.getSelectSubQueryList());
                            handleOnFilter(joinQueryParam::getOnFilters, joinQueryParam::on, joinQuery.getTableAs());
                        }
                        handleFilterSubQuery(joinQuery.getFilters());
                    }
                }
            }
        }
        List<UnionQueryParam> unions = query.getUnions();
        if (unions != null) {
            unions.stream().map(UnionQueryParam::getUnion).forEach(this::handleQuery);
        }
    }

    private void handleUpdate(AbstractUpdate<?> update) {
        handleFilter(update);
        if (update instanceof EntityUpdate) {
            List<JoinUpdateParam> joinList = ((EntityUpdate) update).getJoinAll();
            if (joinList == null || joinList.isEmpty()) return;
            for (JoinUpdateParam joinUpdateParam : joinList) {
                EntityUpdate joinUpdate = joinUpdateParam.getJoinUpdate();
                if (!containSkipClass(joinUpdate)) {
                    handleOnFilter(joinUpdateParam::getOnFilters, joinUpdateParam::on, joinUpdate.getTableAs());
                }
                handleFilterSubQuery(joinUpdate.getFilters());
            }
        }
    }

    private void handleDelete(Delete delete) {
        if (containSkipClass(delete)) {
            return;
        }
        AbstractUpdate<?> convertUpdate = delete.getConvertUpdate();
        if (convertUpdate == null) {
            convertUpdate = new Update();
        }
        if (deletedValue instanceof Expression) {
            convertUpdate.updateExpression(logicDeleteField, (Expression) deletedValue);
        } else {
            convertUpdate.update(logicDeleteField, deletedValue);
        }
        delete.convertUpdate(convertUpdate);
    }

    private void handleOnFilter(Supplier<List<Filter>> onFilters, Consumer<Filter> on, String tableAs) {
        List<Filter> filters = onFilters.get();
        if (filters != null && !filters.isEmpty()) {
            for (Filter filter : filters) {
                String field = filter.getField();
                if (field != null && FilterUtils.tableAsField("-", field).equals(logicDeleteField)) {
                    return;
                }
            }
        }
        on.accept(Filter.AND);
        on.accept(Filter.eq(logicDeleteField, availableValue).fieldAs(tableAs));
    }

    private void handleFilter(AbstractFilter<?> abstractFilter) {
        if (containSkipClass(abstractFilter)) {
            return;
        }
        if (abstractFilter != null) {
            if (!abstractFilter.hasFilter(logicDeleteField)) {
                abstractFilter.eq(logicDeleteField, availableValue);
            }
            handleFilterSubQuery(abstractFilter.getFilters());
        }
    }

    private void handleSelectSubQuery(List<SubQueryParam> list) {
        if (list == null || list.isEmpty()) return;
        for (SubQueryParam sub : list) {
            handleQuery(sub.getQuery());
        }
    }

    private void handleFilterSubQuery(List<Filter> filters) {
        if (filters == null || filters.isEmpty()) return;
        for (Filter filter : filters) {
            if (filter.isSpecial() && filter.getValue() instanceof SubQueryParam) {
                handleQuery(((SubQueryParam) filter.getValue()).getQuery());
            }
        }
    }

}
