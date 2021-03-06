package cn.veasion.db.interceptor;

import cn.veasion.db.AbstractFilter;
import cn.veasion.db.base.Expression;
import cn.veasion.db.base.Filter;
import cn.veasion.db.update.AbstractUpdate;
import cn.veasion.db.update.Delete;
import cn.veasion.db.update.Update;
import cn.veasion.db.utils.FilterUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 逻辑删除拦截器
 *
 * @author luozhuowei
 * @date 2021/12/6
 */
public class LogicDeleteInterceptor extends AbstractInterceptor {

    private static ThreadLocal<Boolean> skipLogicDeleteFilter = new ThreadLocal<>();
    private static ThreadLocal<Set<Class<?>>> skipClassLogicDeleteFilter = new ThreadLocal<>();

    protected String logicDeleteField;
    protected Object availableValue;
    protected Object deletedValue;

    /**
     * 逻辑删除处理拦截器
     *
     * @param logicDeleteField 逻辑删除字段，标识是否删除的字段，如：isDeleted <br><br>
     * @param availableValue   表示可用未删除的值，如： 0，当查询条件没有逻辑删除字段条件时默认加上该条件值 <br><br>
     * @param deletedValue     字段值表示删除，当调用删除时默认会转update更新逻辑删除字段，如：1，<br>
     *                         如需要更新特殊为特殊值，如 id 或者 userId 时可以传 Expression 对象，如 Expression.update("${id}")
     */
    public LogicDeleteInterceptor(String logicDeleteField, Object availableValue, Object deletedValue) {
        super(true, true, true, true, false);
        this.logicDeleteField = Objects.requireNonNull(logicDeleteField);
        this.availableValue = availableValue;
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

    /**
     * 清空跳过
     */
    public static void clearSkip() {
        skipLogicDeleteFilter.remove();
        skipClassLogicDeleteFilter.remove();
    }

    @Override
    protected boolean skip() {
        return Boolean.TRUE.equals(skipLogicDeleteFilter.get());
    }

    @Override
    protected boolean containSkipClass(Class<?> clazz) {
        return skipClassLogicDeleteFilter.get() != null &&
                skipClassLogicDeleteFilter.get().contains(clazz);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void handleDelete(Delete delete) {
        AbstractUpdate<?> convertUpdate = delete.getConvertUpdate();
        if (convertUpdate == null) {
            convertUpdate = new Update();
        }
        Object deletedValue = this.deletedValue;
        if (deletedValue instanceof Function) {
            deletedValue = ((Function<Delete, Object>) deletedValue).apply(delete);
        }
        if (deletedValue instanceof Expression) {
            convertUpdate.updateExpression(logicDeleteField, (Expression) deletedValue);
        } else {
            convertUpdate.update(logicDeleteField, deletedValue);
        }
        delete.convertUpdate(convertUpdate);
    }

    @Override
    protected void handleFilter(AbstractFilter<?> abstractFilter) {
        if (!abstractFilter.hasFilter(logicDeleteField)) {
            abstractFilter.addFilter(getAvailableFilter());
        }
    }

    @Override
    protected void handleOnFilter(Object joinParam, Supplier<List<Filter>> onFilters, Consumer<Filter> onMethod, String tableAs) {
        List<Filter> filters = onFilters.get();
        if (filters != null && !filters.isEmpty()) {
            for (Filter filter : filters) {
                String field = filter.getField();
                if (field != null && FilterUtils.tableAsField("-", field).equals(logicDeleteField)) {
                    return;
                }
            }
        }
        onMethod.accept(Filter.AND);
        onMethod.accept(getAvailableFilter().fieldAs(tableAs));
    }

    protected Filter getAvailableFilter() {
        return availableValue == null ? Filter.isNull(logicDeleteField) : Filter.eq(logicDeleteField, availableValue);
    }

    @Override
    protected void handleInsert(Class<?> entityClass, List<?> entityList, List<Map<String, Object>> fieldValueMapList) {
    }

    @Override
    public int sortIndex() {
        return -1;
    }

}
