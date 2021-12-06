package cn.veasion.db;

import cn.veasion.db.base.Expression;
import cn.veasion.db.base.Filter;
import cn.veasion.db.utils.FilterUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * AbstractFilter
 *
 * @author luozhuowei
 * @date 2021/12/2
 */
@SuppressWarnings("unchecked")
public abstract class AbstractFilter<T> {

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

    public T filterExpression(String field, Filter.Operator operator, Expression expression) {
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
        if (!isSkipNullValueFilter() || isSkipNullValueFilter() && FilterUtils.hasFilter(filter)) {
            filters.add(handleFilter(filter));
        }
        return (T) this;
    }

    public Filter removeFilter(String field) {
        if (filters != null && filters.size() > 0) {
            for (Filter f : filters) {
                if (Objects.equals(f.getField(), field)) {
                    filters.remove(f);
                    return f;
                }
            }
        }
        return null;
    }

    public T addFilters(Filter... filters) {
        for (Filter filter : filters) {
            addFilter(filter);
        }
        return (T) this;
    }

    public boolean hasFilters() {
        return filters != null && !filters.isEmpty() && filters.stream().anyMatch(f ->
                !Filter.AND.equals(f) && !Filter.OR.equals(f) &&
                        !Filter.LEFT_BRACKET.equals(f) && !Filter.RIGHT_BRACKET.equals(f)
        );
    }

    public void check() {
        checkFilter(filters, skipNullValueFilter);
    }

    public synchronized static void checkFilter(List<Filter> filters, boolean ignoreNullValueFilter) {
        if (filters == null || filters.isEmpty()) return;
        boolean preIsJoin = true;
        for (int i = 0; i < filters.size(); i++) {
            Filter filter = FilterUtils.checkFilter(filters.get(i));
            if (ignoreNullValueFilter && filter.getValue() == null) {
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

    protected abstract Filter handleFilter(Filter filter);

}
