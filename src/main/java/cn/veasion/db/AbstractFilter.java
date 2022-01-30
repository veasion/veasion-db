package cn.veasion.db;

import cn.veasion.db.base.Expression;
import cn.veasion.db.base.Filter;
import cn.veasion.db.base.Operator;
import cn.veasion.db.query.SubQueryParam;
import cn.veasion.db.utils.FilterUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * AbstractFilter
 *
 * @author luozhuowei
 * @date 2021/12/2
 */
public abstract class AbstractFilter<T> {

    protected boolean checked;
    private Class<?> entityClass;
    private List<Filter> filters;
    private boolean skipNullValueFilter;

    public T eq(String field, Object value) {
        return addFilter(Filter.eq(field, value));
    }

    public T neq(String field, Object value) {
        return addFilter(Filter.neq(field, value));
    }

    public T gt(String field, Object value) {
        return addFilter(Filter.gt(field, value));
    }

    public T gte(String field, Object value) {
        return addFilter(Filter.gte(field, value));
    }

    public T lt(String field, Object value) {
        return addFilter(Filter.lt(field, value));
    }

    public T lte(String field, Object value) {
        return addFilter(Filter.lte(field, value));
    }

    public T in(String field, Collection<?> value) {
        return addFilter(Filter.in(field, value));
    }

    public T in(String field, Object[] value) {
        return addFilter(Filter.in(field, value));
    }

    public T notIn(String field, Collection<?> value) {
        return addFilter(Filter.notIn(field, value));
    }

    public T notIn(String field, Object[] value) {
        return addFilter(Filter.notIn(field, value));
    }

    public T like(String field, Object value) {
        return addFilter(Filter.like(field, value));
    }

    public T likeLeft(String field, Object value) {
        return addFilter(Filter.likeLeft(field, value));
    }

    public T likeRight(String field, Object value) {
        return addFilter(Filter.likeRight(field, value));
    }

    public T isNull(String field) {
        return addFilter(Filter.isNull(field));
    }

    public T isNotNull(String field) {
        return addFilter(Filter.isNotNull(field));
    }

    public T between(String field, Object value1, Object value2) {
        return addFilter(Filter.between(field, value1, value2));
    }

    public T andBracket(Filter... filters) {
        addFilter(Filter.leftBracket());
        addFilters(filters);
        return addFilter(Filter.rightBracket());
    }

    public T exists(SubQueryParam subQueryParam) {
        return addFilter(Filter.subQuery(null, Operator.EXISTS, subQueryParam));
    }

    public T notExists(SubQueryParam subQueryParam) {
        return addFilter(Filter.subQuery(null, Operator.NOT_EXISTS, subQueryParam));
    }

    public T filterSubQuery(String field, Operator operator, SubQueryParam subQueryParam) {
        return addFilter(Filter.subQuery(field, operator, subQueryParam));
    }

    /**
     * 表达式过滤
     *
     * @param expression 表达式，其中#{}和${}中间可以使用占位字段，解析时#{}会默认替换成values对应的值，${}替换成字段对应表中的列名 <br><br>
     *                   示例一：NOW() <br>
     *                   示例二：DATE_FORMAT(#{value1},'%Y-%m-%d') <br>
     *                   示例二：${age} + #{value1} + #{age} <br>
     * @param values     占位值，对应 #{value1}, #{value2}, #{value3}, #{value...}，通过占位符拼接参数防SQL注入
     */
    public T filterExpression(String field, Operator operator, String expression, Object... values) {
        return filterExpression(field, operator, Expression.filter(expression, values));
    }

    public T filterExpression(String field, Operator operator, Expression expression) {
        return addFilters(Filter.expression(field, operator, expression));
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public boolean isSkipNullValueFilter() {
        return skipNullValueFilter;
    }

    public void setSkipNullValueFilter(boolean skipNullValueFilter) {
        this.skipNullValueFilter = skipNullValueFilter;
    }

    public T addFilter(Filter filter) {
        if (filters == null) filters = new ArrayList<>();
        Objects.requireNonNull(filter, "过滤器不能为空");
        if (!isSkipNullValueFilter() || (isSkipNullValueFilter() && FilterUtils.hasFilter(filter))) {
            filters.add(handleFilter(filter));
            checkFilter();
        }
        return getSelf();
    }

    public boolean hasFilter(String field) {
        if (!hasFilters()) return false;
        field = handleFilter(Filter.eq(field, null)).getField();
        for (Filter filter : filters) {
            if (Objects.equals(filter.getField(), field)) {
                return true;
            }
        }
        return false;
    }

    public List<Filter> getFilters(String field) {
        if (!hasFilters()) return Collections.emptyList();
        String _field = handleFilter(Filter.eq(field, null)).getField();
        return filters.stream().filter(filter -> Objects.equals(filter.getField(), _field)).collect(Collectors.toList());
    }

    public Filter removeFilter(String field) {
        field = handleFilter(Filter.eq(field, null)).getField();
        if (filters != null && filters.size() > 0) {
            for (Filter filter : filters) {
                if (Objects.equals(filter.getField(), field)) {
                    filters.remove(filter);
                    checkFilter();
                    return filter;
                }
            }
        }
        return null;
    }

    public T addFilters(Filter... filters) {
        for (Filter filter : filters) {
            addFilter(filter);
        }
        return getSelf();
    }

    public boolean hasFilters() {
        return filters != null && !filters.isEmpty();
    }

    public void check(Class<?> mainEntityClass) {
        if (entityClass == null) {
            setEntityClass(mainEntityClass);
        }
        checked = true;
        checkFilter(mainEntityClass, filters, skipNullValueFilter);
    }

    private void checkFilter() {
        if (checked) {
            checkFilter(entityClass, filters, skipNullValueFilter);
        }
    }

    public synchronized static void checkFilter(Class<?> mainEntityClass, List<Filter> filters, boolean ignoreNullValueFilter) {
        if (filters == null || filters.isEmpty()) return;
        boolean preIsJoin = true;
        for (int i = 0; i < filters.size(); i++) {
            Filter filter = FilterUtils.checkFilter(filters.get(i));
            if (filter != null && filter.isSpecial() && filter.getValue() instanceof SubQueryParam) {
                ((SubQueryParam) filter.getValue()).getQuery().check(mainEntityClass);
            }
            if (ignoreNullValueFilter && !FilterUtils.hasFilter(filter)) {
                filters.remove(i--);
                continue;
            }
            if (Filter.AND.equals(filter) || Filter.OR.equals(filter)) {
                if (preIsJoin) {
                    filters.remove(i--);
                }
                preIsJoin = true;
                continue;
            } else if (!preIsJoin) {
                if (Filter.RIGHT_BRACKET.equals(filter)) {
                    continue;
                }
                filters.add(i++, Filter.AND);
            } else {
                preIsJoin = false;
            }
            if (Filter.LEFT_BRACKET.equals(filter)) {
                preIsJoin = true;
            }
        }
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    protected abstract Filter handleFilter(Filter filter);

    protected abstract T getSelf();

}
