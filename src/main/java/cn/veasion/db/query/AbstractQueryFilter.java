package cn.veasion.db.query;

import cn.veasion.db.base.AbstractFilter;
import cn.veasion.db.base.Filter;
import cn.veasion.db.utils.FilterUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * AbstractFilter
 *
 * @author luozhuowei
 * @date 2021/12/2
 */
@SuppressWarnings("unchecked")
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
        if (!isSkipNullValueFilter() || isSkipNullValueFilter() && FilterUtils.hasFilter(filter)) {
            having.add(handleFilter(filter));
        }
        return (T) this;
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
