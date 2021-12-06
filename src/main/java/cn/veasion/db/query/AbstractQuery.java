package cn.veasion.db.query;

import cn.veasion.db.base.Expression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * AbstractQuery
 *
 * @author luozhuowei
 * @date 2021/12/2
 */
@SuppressWarnings("unchecked")
public abstract class AbstractQuery<T> extends AbstractQueryFilter<T> {

    private boolean distinct;
    private boolean selectAll;
    private List<String> selects = new ArrayList<>();
    private Map<String, String> aliasMap = new HashMap<>();
    private List<String> excludeSelects;
    private List<Expression> selectExpression;
    private List<String> groupBys;
    private List<OrderParam> orders;
    private List<UnionQueryParam> unions;

    public T distinct() {
        this.distinct = true;
        return (T) this;
    }

    public T selectAll() {
        this.selectAll = true;
        return (T) this;
    }

    public T select(String field) {
        return select(field, null);
    }

    public T select(String field, String alias) {
        field = handleSelectField(field);
        selects.add(field);
        aliasMap.put(field, alias);
        return (T) this;
    }

    public T selects(String... fields) {
        for (String field : fields) {
            select(field);
        }
        return (T) this;
    }

    public T selectExpression(Expression expression) {
        selectExpression.add(Objects.requireNonNull(expression));
        return (T) this;
    }

    public T alias(String field, String alias) {
        aliasMap.put(handleSelectField(field), alias);
        return (T) this;
    }

    public T excludeFields(String... fields) {
        if (excludeSelects == null) excludeSelects = new ArrayList<>();
        for (String field : fields) {
            excludeSelects.add(handleSelectField(field));
        }
        return (T) this;
    }

    public T groupBy(String... fields) {
        if (groupBys == null) groupBys = new ArrayList<>();
        for (String field : fields) {
            groupBys.add(handleSelectField(field));
        }
        return (T) this;
    }

    public T asc(String field) {
        return order(new OrderParam(field, true));
    }

    public T desc(String field) {
        return order(new OrderParam(field, false));
    }

    public T order(OrderParam orderParam) {
        if (orders == null) orders = new ArrayList<>();
        orderParam.setField(handleSelectField(orderParam.getField()));
        orders.add(orderParam);
        return (T) this;
    }

    public T union(AbstractQuery<T> unionQuery) {
        if (unions == null) unions = new ArrayList<>();
        unions.add(new UnionQueryParam(unionQuery, false));
        return (T) this;
    }

    public T unionAll(AbstractQuery<T> unionQuery) {
        if (unions == null) unions = new ArrayList<>();
        unions.add(new UnionQueryParam(unionQuery, true));
        return (T) this;
    }

    protected abstract String handleSelectField(String field);

    public boolean isDistinct() {
        return distinct;
    }

    public boolean isSelectAll() {
        return selectAll;
    }

    public List<String> getSelects() {
        return selects;
    }

    public Map<String, String> getAliasMap() {
        return aliasMap;
    }

    public List<Expression> getSelectExpression() {
        return selectExpression;
    }

    public List<String> getExcludeSelects() {
        return excludeSelects;
    }

    public List<String> getGroupBys() {
        return groupBys;
    }

    public void setGroupBys(List<String> groupBys) {
        this.groupBys = groupBys;
    }

    public List<OrderParam> getOrders() {
        return orders;
    }

    public List<UnionQueryParam> getUnions() {
        return unions;
    }
}
