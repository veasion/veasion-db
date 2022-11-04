package cn.veasion.db.jdbc;

import cn.veasion.db.base.Expression;
import cn.veasion.db.base.Filter;
import cn.veasion.db.query.AbstractJoinQuery;
import cn.veasion.db.query.AbstractQuery;
import cn.veasion.db.query.JoinQueryParam;
import cn.veasion.db.query.OrderParam;
import cn.veasion.db.query.SubQuery;
import cn.veasion.db.query.SubQueryParam;
import cn.veasion.db.query.UnionQueryParam;
import cn.veasion.db.query.Window;
import cn.veasion.db.utils.FilterUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * QuerySQL
 *
 * @author luozhuowei
 * @date 2021/12/10
 */
public class QuerySQL extends AbstractSQL<QuerySQL> {

    private AbstractQuery<?> query;

    private String tableAs;
    private List<JoinQueryParam> joins;
    private Map<String, String> selectFieldColumnMap;

    private SubQuery subQuery;
    private QuerySQL subQuerySQL;
    private Map<String, QuerySQL> joinSubQuerySQLMap;

    public QuerySQL(AbstractQuery<?> query) {
        this.query = query;
    }

    public static QuerySQL build(AbstractQuery<?> query) {
        return new QuerySQL(query).build();
    }

    public static QuerySQL build(AbstractQuery<?> query, Map<String, String> selectFieldColumnMap) {
        QuerySQL querySQL = new QuerySQL(query);
        querySQL.selectFieldColumnMap = selectFieldColumnMap;
        return querySQL.build();
    }

    @Override
    public QuerySQL build() {
        this.reset();
        buildQuery();
        return this;
    }

    private void buildQuery() {
        if (query instanceof SubQuery) {
            subQuery = (SubQuery) query;
            subQuerySQL = build(subQuery.getSubQuery(), new HashMap<>());
        }
        Map<String, Class<?>> entityClassMap = entityClassMap();
        sql.append("SELECT");
        if (query.isDistinct()) {
            sql.append(" DISTINCT");
        }
        // select & join select
        appendSelects(entityClassMap, false);
        // expression & join expression
        appendSelects(entityClassMap, true);
        // subQuery & join subQuery
        appendSelects(query.getSelectSubQueryList(), true);
        trimEndSql(",");
        // from table
        sql.append(" FROM ");
        if (subQuerySQL != null) {
            sql.append("(").append(subQuerySQL.getSQL()).append(")");
            values.addAll(Arrays.asList(subQuerySQL.getValues()));
        } else {
            sql.append(getTableName(query.getEntityClass(), query, query));
        }
        if (tableAs != null) {
            sql.append(" ").append(tableAs);
        }
        // join
        appendJoins(entityClassMap);
        // window
        if (query.getWindow() != null && query.getWindow().isWhereBefore()) {
            appendWindow(query.getWindow(), entityClassMap);
        }
        sql.append(" WHERE");
        // filter & join filter
        appendFilters(entityClassMap);
        trimEndSql("WHERE");
        // group by
        appendGroups(entityClassMap);
        // having
        if (query.getHaving() != null) {
            sql.append(" HAVING");
            appendFilter(entityClassMap, query.getHaving());
            trimEndSql("HAVING");
        }
        // window
        if (query.getWindow() != null && !query.getWindow().isWhereBefore()) {
            appendWindow(query.getWindow(), entityClassMap);
        }
        // union all
        List<UnionQueryParam> unions = query.getUnions();
        if (unions != null) {
            for (UnionQueryParam union : unions) {
                QuerySQL querySQL = build(union.getUnion());
                sql.append(union.isUnionAll() ? " UNION ALL " : " UNION ").append(querySQL.getSQL()).append(" ");
                values.addAll(Arrays.asList(querySQL.getValues()));
            }
        }
        // order by
        appendOrders(entityClassMap);
        // page
        if (query.getPageParam() != null) {
            query.getPageParam().handleSqlValue(sql, values);
        }
    }

    private Map<String, Class<?>> entityClassMap() {
        Map<String, Class<?>> entityClassMap = new HashMap<>();
        if (query instanceof AbstractJoinQuery) {
            joins = ((AbstractJoinQuery<?>) query).getJoinAll();
            tableAs = ((AbstractJoinQuery<?>) query).getTableAs();
            if (joins != null) {
                for (JoinQueryParam join : joins) {
                    AbstractJoinQuery<?> joinQuery = join.getJoinQuery();
                    String tableAs = joinQuery.getTableAs();
                    if (joinQuery instanceof SubQuery) {
                        QuerySQL joinSubQuerySQL = build(((SubQuery) joinQuery).getSubQuery(), new HashMap<>());
                        if (joinSubQuerySQLMap == null) {
                            joinSubQuerySQLMap = new HashMap<>();
                        }
                        joinSubQuerySQLMap.put(tableAs, joinSubQuerySQL);
                    }
                    entityClassMap.put(tableAs, joinQuery.getEntityClass());
                }
            }
        }
        entityClassMap.put(tableAs, query.getEntityClass());
        return entityClassMap;
    }

    private void appendSelects(Map<String, Class<?>> entityClassMap, boolean isExpression) {
        if (isExpression) {
            appendSelects(entityClassMap, query.getSelectExpression());
            if (joins == null || joins.isEmpty()) return;
            for (JoinQueryParam join : joins) {
                AbstractJoinQuery<?> joinQuery = join.getJoinQuery();
                if (joinQuery.getSelectExpression() != null) {
                    appendSelects(entityClassMap, joinQuery.getSelectExpression());
                }
            }
        } else {
            appendSelects(entityClassMap, query.getSelects(), query.getAliasMap());
            if (joins == null || joins.isEmpty()) return;
            for (JoinQueryParam join : joins) {
                AbstractJoinQuery<?> joinQuery = join.getJoinQuery();
                if (!joinQuery.getSelects().isEmpty()) {
                    appendSelects(entityClassMap, joinQuery.getSelects(), joinQuery.getAliasMap());
                }
            }
        }
    }

    private void appendSelects(List<SubQueryParam> list, boolean main) {
        if (list != null && !list.isEmpty()) {
            for (SubQueryParam sub : list) {
                QuerySQL querySQL = build(sub.getQuery(), new LinkedHashMap<>());
                sql.append(" (").append(querySQL.getSQL()).append(")");
                if (!querySQL.selectFieldColumnMap.isEmpty()) {
                    sql.append(" AS ").append(querySQL.selectFieldColumnMap.keySet().iterator().next());
                }
                sql.append(",");
                values.addAll(querySQL.values);
            }
        }
        if (main && joins != null && !joins.isEmpty()) {
            for (JoinQueryParam join : joins) {
                List<SubQueryParam> selectSubQueryList = join.getJoinQuery().getSelectSubQueryList();
                if (selectSubQueryList != null) {
                    appendSelects(selectSubQueryList, false);
                }
            }
        }
    }

    private void appendSelects(Map<String, Class<?>> entityClassMap, List<Expression> selectExpression) {
        if (selectExpression == null || selectExpression.isEmpty()) return;
        for (Expression expression : selectExpression) {
            String alias = expression.getAlias();
            if (selectFieldColumnMap != null && alias != null) {
                selectFieldColumnMap.put(alias, alias);
            }
            sql.append(" ");
            appendExpressionValue(entityClassMap, expression);
            if (alias != null) {
                sql.append(" AS ").append(alias);
            }
            sql.append(",");
        }
    }

    private void appendSelects(Map<String, Class<?>> entityClassMap, List<String> selects, Map<String, String> aliasMap) {
        if (selects == null || selects.isEmpty()) return;
        StringBuilder sb = new StringBuilder();
        for (String select : selects) {
            String alias = aliasMap.get(select);
            String column = handleFieldToColumn(select, entityClassMap);
            if (selectFieldColumnMap != null && alias != null) {
                selectFieldColumnMap.put(alias, alias);
            } else if (selectFieldColumnMap != null) {
                selectFieldColumnMap.put(FilterUtils.tableAsField("-", select), FilterUtils.tableAsField("-", column));
            }
            sb.append(" ").append(column);
            if (alias != null) {
                sb.append(" AS ").append(alias);
            }
            sb.append(",");
        }
        sql.append(securityCheck(sb.toString()));
    }

    private void appendJoins(Map<String, Class<?>> entityClassMap) {
        if (joins == null || joins.isEmpty()) return;
        for (JoinQueryParam join : joins) {
            AbstractJoinQuery<?> joinQuery = join.getJoinQuery();
            sql.append(" ").append(join.getJoinType().getJoin());
            if (joinQuery instanceof SubQuery) {
                QuerySQL joinSubQuerySQL = joinSubQuerySQLMap.get(joinQuery.getTableAs());
                sql.append(" (").append(joinSubQuerySQL.getSQL()).append(")");
                values.addAll(Arrays.asList(joinSubQuerySQL.getValues()));
            } else {
                sql.append(" ").append(getTableName(joinQuery.getEntityClass(), joinQuery, join));
            }
            if (joinQuery.getTableAs() != null) {
                sql.append(" ").append(joinQuery.getTableAs());
            }
            List<Filter> filters = join.getOnFilters();
            if (filters != null && filters.size() > 0) {
                sql.append(" ON");
                appendFilter(entityClassMap, filters);
            }
        }
    }

    private void appendFilters(Map<String, Class<?>> entityClassMap) {
        appendFilter(entityClassMap, query.getFilters());
        if (joins == null || joins.isEmpty()) return;
        for (JoinQueryParam join : joins) {
            AbstractJoinQuery<?> joinQuery = join.getJoinQuery();
            if (joinQuery.hasFilters()) {
                if (!endsWith(" WHERE")) {
                    sql.append(" AND");
                }
                appendFilter(entityClassMap, joinQuery.getFilters());
            }
        }
    }

    private void appendWindow(Window window, Map<String, Class<?>> entityClassMap) {
        sql.append(" WINDOW ").append(window.getAlias());
        sql.append(" AS (");
        appendExpressionValue(entityClassMap, window.getExpression());
        sql.append(")");
    }

    private void appendGroups(Map<String, Class<?>> entityClassMap) {
        List<String> groupBys = query.getGroupBys();
        if (groupBys == null || groupBys.isEmpty()) return;
        sql.append(" GROUP BY");
        for (String field : groupBys) {
            sql.append(" ").append(handleFieldToColumn(field, entityClassMap)).append(",");
        }
        trimEndSql(",");
    }

    private void appendOrders(Map<String, Class<?>> entityClassMap) {
        List<OrderParam> orders = query.getOrders();
        if (orders == null || orders.isEmpty()) return;
        sql.append(" ORDER BY");
        for (OrderParam order : orders) {
            sql.append(" ").append(handleFieldToColumn(order.getField(), entityClassMap));
            if (order.isDesc()) {
                sql.append(" DESC");
            }
            sql.append(",");
        }
        trimEndSql(",");
    }

    @Override
    protected String toColumn(String tableAs, String field) {
        if (subQuery != null && subQuerySQL != null) {
            String column = subQuerySQL.selectFieldColumnMap.get(field);
            if (column != null) {
                return column;
            }
        } else if (joinSubQuerySQLMap != null && tableAs != null) {
            QuerySQL joinSubQuerySQL = joinSubQuerySQLMap.get(tableAs);
            if (joinSubQuerySQL != null) {
                String column = joinSubQuerySQL.selectFieldColumnMap.get(field);
                if (column != null) {
                    return column;
                }
            }
        }
        return super.toColumn(tableAs, field);
    }

}
