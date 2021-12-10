package cn.veasion.db.query;

import cn.veasion.db.base.Expression;
import cn.veasion.db.jdbc.QuerySQL;
import cn.veasion.db.utils.FieldUtils;
import cn.veasion.db.utils.ServiceLoaderUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * AbstractQuery
 *
 * @author luozhuowei
 * @date 2021/12/2
 */
public abstract class AbstractQuery<T> extends AbstractQueryFilter<T> {

    private boolean distinct;
    protected boolean selectAll;
    protected List<String> selects = new ArrayList<>();
    protected Map<String, String> aliasMap = new HashMap<>();
    private Set<String> excludeSelects;
    private List<Expression> selectExpression;
    private List<String> groupBys;
    private List<OrderParam> orders;
    private List<UnionQueryParam> unions;
    private PageParam pageParam;

    private Class<?> entityClass;

    public T distinct() {
        this.distinct = true;
        return getSelf();
    }

    public T selectAll() {
        this.selectAll = true;
        return getSelf();
    }

    public T select(String field) {
        return select(field, null);
    }

    public T select(String field, String alias) {
        field = handleField(field);
        selects.add(field);
        if (alias != null) {
            aliasMap.put(field, alias);
        }
        return getSelf();
    }

    public T selects(String... fields) {
        for (String field : fields) {
            select(field);
        }
        return getSelf();
    }

    public T selectExpression(Expression expression) {
        if (selectExpression == null) selectExpression = new ArrayList<>();
        selectExpression.add(Objects.requireNonNull(expression));
        return getSelf();
    }

    public T alias(String field, String alias) {
        aliasMap.put(handleField(field), Objects.requireNonNull(alias));
        return getSelf();
    }

    public T excludeFields(String... fields) {
        if (excludeSelects == null) excludeSelects = new HashSet<>();
        for (String field : fields) {
            excludeSelects.add(handleField(field));
        }
        return getSelf();
    }

    public T groupBy(String... fields) {
        if (groupBys == null) groupBys = new ArrayList<>();
        for (String field : fields) {
            groupBys.add(handleField(field));
        }
        return getSelf();
    }

    public T asc(String field) {
        return order(new OrderParam(field, true));
    }

    public T desc(String field) {
        return order(new OrderParam(field, false));
    }

    public T order(OrderParam orderParam) {
        if (orders == null) orders = new ArrayList<>();
        orderParam.setField(handleField(orderParam.getField()));
        orders.add(orderParam);
        return getSelf();
    }

    public T union(AbstractQuery<T> unionQuery) {
        if (unions == null) unions = new ArrayList<>();
        unions.add(new UnionQueryParam(unionQuery, false));
        return getSelf();
    }

    public T unionAll(AbstractQuery<T> unionQuery) {
        if (unions == null) unions = new ArrayList<>();
        unions.add(new UnionQueryParam(unionQuery, true));
        return getSelf();
    }

    public T page(PageParam pageParam) {
        this.pageParam = pageParam;
        return getSelf();
    }

    public T page(int page, int size) {
        PageParam pageParam = ServiceLoaderUtils.loadOne(PageParam.class);
        if (pageParam != null) {
            this.pageParam = pageParam;
            this.pageParam.setPage(page);
            this.pageParam.setSize(size);
        } else {
            this.pageParam = new MysqlPage(page, size);
        }
        return getSelf();
    }

    protected abstract String handleField(String field);

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

    public PageParam getPageParam() {
        return pageParam;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public void check() {
        if (selectAll && !checked) {
            Map<String, String> fieldColumns = FieldUtils.entityFieldColumns(entityClass);
            if (excludeSelects != null && excludeSelects.size() > 0) {
                fieldColumns.keySet().stream().map(this::handleField).filter(k -> !excludeSelects.contains(k)).forEach(this::select);
            } else {
                fieldColumns.keySet().stream().map(this::handleField).forEach(this::select);
            }
        }
        if (excludeSelects != null) {
            for (String excludeSelect : excludeSelects) {
                for (int i = 0; i < selects.size(); i++) {
                    if (Objects.equals(excludeSelect, selects.get(i))) {
                        selects.remove(i);
                        break;
                    }
                }
            }
        }
        super.check();
        if (unions != null) {
            for (UnionQueryParam union : unions) {
                union.getUnion().check();
            }
        }
    }

    public QuerySQL sqlValue() {
        return QuerySQL.build(this);
    }

}
