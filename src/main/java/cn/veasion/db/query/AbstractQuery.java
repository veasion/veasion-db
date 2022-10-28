package cn.veasion.db.query;

import cn.veasion.db.AbstractFilter;
import cn.veasion.db.DbException;
import cn.veasion.db.base.Expression;
import cn.veasion.db.base.Filter;
import cn.veasion.db.jdbc.QuerySQL;
import cn.veasion.db.utils.FieldUtils;
import cn.veasion.db.utils.FilterUtils;
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
public abstract class AbstractQuery<T extends AbstractQuery<?>> extends AbstractFilter<T> {

    private boolean distinct;
    protected boolean selectAll;
    private boolean asterisk = true;
    protected List<String> selects = new ArrayList<>();
    protected Map<String, String> aliasMap = new HashMap<>();
    private Set<String> excludeSelects;
    private List<Expression> selectExpression;
    private List<SubQueryParam> selectSubQueryList;
    private List<String> groupBys;
    private List<OrderParam> orders;
    private List<Filter> having;
    private List<UnionQueryParam> unions;
    private PageParam pageParam;

    public T distinct() {
        this.distinct = true;
        return getSelf();
    }

    public T selectAll() {
        this.selectAll = true;
        return getSelf();
    }

    public T selectAllWithNoAsterisk() {
        this.selectAll = true;
        this.asterisk = false;
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

    public T selectSubQuery(SubQueryParam subQueryParam) {
        Objects.requireNonNull(subQueryParam, "子查询参数不能为空");
        if (selectSubQueryList == null) selectSubQueryList = new ArrayList<>();
        selectSubQueryList.add(subQueryParam);
        return getSelf();
    }

    /**
     * 表达式查询
     *
     * @param expression 表达式，其中#{}和${}中间可以使用占位字段，解析时#{}会默认替换成values对应的值，${}替换成字段对应表中的列名 <br><br>
     *                   示例一：IFNULL(${userName}, #{value1}) <br>
     *                   示例二：IF(${u.type} = 1, AVG(${s.score}), SUM(${s.score})) <br>
     * @param alias      别名，如 userName
     * @param values     占位值，对应 #{value1}, #{value2}, #{value3}, #{value...}，通过占位符拼接参数防SQL注入
     */
    public T selectExpression(String expression, String alias, Object... values) {
        return selectExpression(Expression.select(expression, alias, values));
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
        return order(new OrderParam(field, false));
    }

    public T desc(String field) {
        return order(new OrderParam(field, true));
    }

    public T order(OrderParam orderParam) {
        if (orders == null) orders = new ArrayList<>();
        orderParam.setField(handleField(orderParam.getField()));
        orders.add(orderParam);
        return getSelf();
    }

    public T having(Filter filter) {
        if (having == null) having = new ArrayList<>();
        Objects.requireNonNull(filter, "过滤器不能为空");
        if (!isSkipNullValueFilter() || (isSkipNullValueFilter() && FilterUtils.hasFilter(filter))) {
            if (selectExpression != null && selectExpression.stream().map(Expression::getAlias).anyMatch(s -> s != null && s.equals(filter.getField()))) {
                having.add(filter);
            } else {
                having.add(handleFilter(filter));
            }
        }
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
        if (pageParam != null && (pageParam.getPage() <= 0 || pageParam.getSize() <= 0)) {
            throw new DbException("分页参数page、size必须大于0");
        }
        return getSelf();
    }

    public T page(int page, int size) {
        PageParam pageParam = ServiceLoaderUtils.pageParam();
        if (pageParam != null) {
            pageParam.setPage(page);
            pageParam.setSize(size);
        } else {
            pageParam = new MysqlPage(page, size);
        }
        return this.page(pageParam);
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

    public List<SubQueryParam> getSelectSubQueryList() {
        return selectSubQueryList;
    }

    public List<String> getGroupBys() {
        return groupBys;
    }

    public T setGroupBys(List<String> groupBys) {
        this.groupBys = groupBys;
        return getSelf();
    }

    public List<OrderParam> getOrders() {
        return orders;
    }

    public List<Filter> getHaving() {
        return having;
    }

    public List<UnionQueryParam> getUnions() {
        return unions;
    }

    public PageParam getPageParam() {
        return pageParam;
    }

    @Override
    public void check(Class<?> mainEntityClass) {
        if (selectAll && !checked) {
            if (excludeSelects != null && excludeSelects.size() > 0) {
                Map<String, String> fieldColumns = FieldUtils.entityFieldColumns(getEntityClass() != null ? getEntityClass() : mainEntityClass);
                fieldColumns.keySet().stream().map(this::handleField).filter(k -> !excludeSelects.contains(k)).forEach(this::select);
            } else if (asterisk) {
                this.select(this.handleField("*"));
            } else {
                Map<String, String> fieldColumns = FieldUtils.entityFieldColumns(getEntityClass() != null ? getEntityClass() : mainEntityClass);
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
        super.check(mainEntityClass);
        if (selectSubQueryList != null) {
            for (SubQueryParam sub : selectSubQueryList) {
                sub.getQuery().check(mainEntityClass);
            }
        }
        checkFilter(mainEntityClass, having, false);
        if (unions != null) {
            for (UnionQueryParam union : unions) {
                union.getUnion().check(mainEntityClass);
            }
        }
    }

    public QuerySQL sqlValue() {
        return QuerySQL.build(this);
    }

}
