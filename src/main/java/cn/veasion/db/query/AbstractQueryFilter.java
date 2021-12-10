package cn.veasion.db.query;

import cn.veasion.db.AbstractFilter;
import cn.veasion.db.base.Filter;
import cn.veasion.db.utils.FilterUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * AbstractFilter
 *
 * @author luozhuowei
 * @date 2021/12/2
 */
public abstract class AbstractQueryFilter<T> extends AbstractFilter<T> {

    private List<Filter> having;

    public T exists(String field, SubQueryParam subQueryParam) {
        return addFilter(Filter.subQuery(field, Filter.Operator.EXISTS, subQueryParam));
    }

    public T notExists(String field, SubQueryParam subQueryParam) {
        return addFilter(Filter.subQuery(field, Filter.Operator.NOT_EXISTS, subQueryParam));
    }

    public T having(Filter filter) {
        if (having == null) having = new ArrayList<>();
        Objects.requireNonNull(filter, "过滤器不能为空");
        if (!isSkipNullValueFilter() || (isSkipNullValueFilter() && FilterUtils.hasFilter(filter))) {
            having.add(handleFilter(filter));
        }
        return getSelf();
    }

    public List<Filter> getHaving() {
        return having;
    }

    @Override
    public void check() {
        super.check();
        checkFilter(having, false);
    }

}
