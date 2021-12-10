package cn.veasion.db.jdbc;

import cn.veasion.db.base.Expression;
import cn.veasion.db.base.Filter;
import cn.veasion.db.query.AbstractQuery;
import cn.veasion.db.query.EntityQuery;
import cn.veasion.db.query.JoinQueryParam;
import cn.veasion.db.query.OrderParam;
import cn.veasion.db.query.SubQuery;
import cn.veasion.db.query.UnionQueryParam;

import java.util.Arrays;
import java.util.HashMap;
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
    private List<String> insertFields;

    private String tableAs;
    private List<JoinQueryParam> joins;

    public QuerySQL(AbstractQuery<?> query) {
        this.query = query;
    }

    public static QuerySQL build(AbstractQuery<?> query) {
        return new QuerySQL(query).build();
    }

    public static QuerySQL build(AbstractQuery<?> query, List<String> insertFields) {
        QuerySQL querySQL = new QuerySQL(query);
        querySQL.insertFields = insertFields;
        return querySQL.build();
    }

    @Override
    public QuerySQL build() {
        this.reset();
        buildQuery();
        return this;
    }

    private Map<String, Class<?>> entityClassMap() {
        Map<String, Class<?>> entityClassMap = new HashMap<>();
        if (query instanceof EntityQuery) {
            joins = ((EntityQuery) query).getJoinAll();
            tableAs = ((EntityQuery) query).getTableAs();
            if (joins != null) {
                joins.forEach(q -> entityClassMap.put(q.getJoinQuery().getTableAs(), q.getJoinQuery().getEntityClass()));
            }
        } else if (query instanceof SubQuery) {
            tableAs = ((SubQuery) query).getTableAs();
        }
        entityClassMap.put(tableAs, query.getEntityClass());
        return entityClassMap;
    }

    private void buildQuery() {
        Map<String, Class<?>> entityClassMap = entityClassMap();
        sql.append("SELECT ");
        if (query.isDistinct()) {
            sql.append("DISTINCT ");
        }
        // select & join select
        appendSelects(entityClassMap, false);
        // expression & join expression
        appendSelects(entityClassMap, true);
        trimEndSql(",");
        // from table
        sql.append(" FROM ");
        if (query instanceof SubQuery) {
            QuerySQL querySQL = build(((SubQuery) query).getSubQuery());
            sql.append("(").append(querySQL.getSQL()).append(")");
            values.addAll(Arrays.asList(querySQL.getValues()));
        } else {
            sql.append(getTableName(query.getEntityClass()));
        }
        if (tableAs != null) {
            sql.append(" ").append(tableAs);
        }
        // join
        appendJoins();
        sql.append(" WHERE");
        // filter & join filter
        appendFilters(entityClassMap);
        trimEndSql("WHERE");
        // group by
        appendGroups(entityClassMap);
        // having
        appendFilter(entityClassMap, query.getHaving());
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

    private void appendFilters(Map<String, Class<?>> entityClassMap) {
        appendFilter(entityClassMap, query.getFilters());
        if (joins == null || joins.isEmpty()) return;
        for (JoinQueryParam join : joins) {
            EntityQuery mainQuery = join.getMainQuery();
            EntityQuery joinQuery = join.getJoinQuery();
            if (joinQuery.hasFilters()) {
                sql.append(" AND");
                appendFilter(new HashMap<String, Class<?>>() {{
                    put(mainQuery.getTableAs(), mainQuery.getEntityClass());
                    put(joinQuery.getTableAs(), joinQuery.getEntityClass());
                }}, joinQuery.getFilters());
            }
        }
    }

    private void appendSelects(Map<String, Class<?>> entityClassMap, boolean isExpression) {
        if (isExpression) {
            appendSelects(entityClassMap, query.getSelectExpression());
            if (joins == null || joins.isEmpty()) return;
            for (JoinQueryParam join : joins) {
                EntityQuery mainQuery = join.getMainQuery();
                EntityQuery joinQuery = join.getJoinQuery();
                if (joinQuery.getSelectExpression() != null) {
                    appendSelects(new HashMap<String, Class<?>>() {{
                        put(mainQuery.getTableAs(), mainQuery.getEntityClass());
                        put(joinQuery.getTableAs(), joinQuery.getEntityClass());
                    }}, joinQuery.getSelectExpression());
                }
            }
        } else {
            appendSelects(entityClassMap, query.getSelects(), query.getAliasMap());
            if (joins == null || joins.isEmpty()) return;
            for (JoinQueryParam join : joins) {
                EntityQuery mainQuery = join.getMainQuery();
                EntityQuery joinQuery = join.getJoinQuery();
                if (!joinQuery.getSelects().isEmpty()) {
                    appendSelects(new HashMap<String, Class<?>>() {{
                        put(mainQuery.getTableAs(), mainQuery.getEntityClass());
                        put(joinQuery.getTableAs(), joinQuery.getEntityClass());
                    }}, joinQuery.getSelects(), joinQuery.getAliasMap());
                }
            }
        }
    }

    private void appendSelects(Map<String, Class<?>> entityClassMap, List<Expression> selectExpression) {
        if (selectExpression == null || selectExpression.isEmpty()) return;
        StringBuilder sb = new StringBuilder();
        for (Expression expression : selectExpression) {
            if (insertFields != null && expression.getAlias() != null) {
                insertFields.add(expression.getAlias());
            }
            sb.append(" ").append(replaceSqlEval(expression.getExpression(), entityClassMap));
            if (expression.getAlias() != null) {
                sb.append(" AS ").append(expression.getAlias());
            }
            sb.append(",");
        }
        sql.append(replaceSqlEval(sb.toString(), entityClassMap));
    }

    private void appendSelects(Map<String, Class<?>> entityClassMap, List<String> selects, Map<String, String> aliasMap) {
        if (selects == null || selects.isEmpty()) return;
        StringBuilder sb = new StringBuilder();
        for (String select : selects) {
            if (insertFields != null) {
                if (aliasMap.containsKey(select)) {
                    insertFields.add(aliasMap.get(select));
                } else {
                    String field;
                    if (aliasMap.containsKey(select)) {
                        field = aliasMap.get(select);
                    } else if (select.contains(".")) {
                        field = select.substring(select.indexOf(".") + 1);
                    } else {
                        field = select;
                    }
                    insertFields.add(field);
                }
            }
            sb.append(" ").append(handleFieldToColumn(select, entityClassMap));
            if (aliasMap.containsKey(select)) {
                sb.append(" AS ").append(aliasMap.get(select));
            }
            sb.append(",");
        }
        sql.append(securityCheck(sb.toString()));
    }

    private void appendJoins() {
        if (joins == null || joins.isEmpty()) return;
        for (JoinQueryParam join : joins) {
            EntityQuery mainQuery = join.getMainQuery();
            EntityQuery joinQuery = join.getJoinQuery();
            sql.append(" ").append(join.getJoinType().getJoin());
            sql.append(" ").append(getTableName(joinQuery.getEntityClass()));
            if (joinQuery.getTableAs() != null) {
                sql.append(" ").append(joinQuery.getTableAs());
            }
            List<Filter> filters = join.getOnFilters();
            if (filters != null && filters.size() > 0) {
                sql.append(" ON");
                appendFilter(new HashMap<String, Class<?>>() {{
                    put(mainQuery.getTableAs(), mainQuery.getEntityClass());
                    put(joinQuery.getTableAs(), joinQuery.getEntityClass());
                }}, filters);
            }
        }
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
            if (!order.isAsc()) {
                sql.append(" DESC");
            }
            sql.append(",");
        }
        trimEndSql(",");
    }

}
