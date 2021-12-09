package cn.veasion.db.jdbc;

import cn.veasion.db.DbException;
import cn.veasion.db.base.Expression;
import cn.veasion.db.base.Filter;
import cn.veasion.db.base.Table;
import cn.veasion.db.query.*;
import cn.veasion.db.update.*;
import cn.veasion.db.utils.FieldUtils;
import cn.veasion.db.utils.LeftRight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * SqlDaoUtils
 *
 * @author luozhuowei
 * @date 2021/12/6
 */
public class SqlDaoUtils {

    private static Logger logger = LoggerFactory.getLogger(SqlDaoUtils.class);

    private static DataSourceProvider dataSourceProvider;

    public synchronized static DataSourceProvider dataSourceProvider() {
        if (dataSourceProvider != null) {
            return dataSourceProvider;
        }
        ServiceLoader<DataSourceProvider> serviceLoader = ServiceLoader.load(DataSourceProvider.class);
        Iterator<DataSourceProvider> iterator = serviceLoader.iterator();
        if (iterator.hasNext()) {
            dataSourceProvider = iterator.next();
        }
        if (iterator.hasNext()) {
            logger.warn("发现多个dataSourceProvider");
        }
        if (dataSourceProvider == null) {
            logger.warn("dataSourceProvider未获取到实例");
        }
        return dataSourceProvider;
    }

    public static Field getIdField(Class<?> entityClazz) {
        Table annotation = entityClazz.getAnnotation(Table.class);
        if (annotation != null) {
            String field = annotation.idField();
            if (!"".equals(field)) {
                return FieldUtils.getField(entityClazz, field);
            }
            if (annotation.entityClass() != Void.class) {
                return getIdField(annotation.entityClass());
            }
        }
        return FieldUtils.getField(entityClazz, "id");
    }

    public static String getTableName(Class<?> entityClazz) {
        Table annotation = entityClazz.getAnnotation(Table.class);
        if (annotation != null) {
            if (!"".equals(annotation.value())) {
                return annotation.value();
            }
            if (annotation.entityClass() != Void.class) {
                return getTableName(annotation.entityClass());
            }
        }
        return FieldUtils.humpToLine(entityClazz.getName());
    }

    public static String sqlPlaceholder(int len) {
        String[] array = new String[len];
        Arrays.fill(array, "?");
        return String.join(",", array);
    }

    public static LeftRight<String, Object[]> insert(Class<?> entityClazz, List<Map<String, Object>> fieldValueMapList) {
        if (fieldValueMapList == null || fieldValueMapList.isEmpty()) {
            throw new DbException("fieldValueMapList is empty");
        }
        Map<String, String> fieldColumns = FieldUtils.entityFieldColumns(entityClazz);
        Set<String> fields = fieldValueMapList.get(0).keySet();

        Object[] values = new Object[fields.size() * fieldValueMapList.size()];
        StringBuilder sql = new StringBuilder("insert into ");
        sql.append(getTableName(entityClazz)).append(" (");
        for (String field : fields) {
            sql.append("`").append(fieldColumns.get(field)).append("`").append(",");
        }
        trimEndSql(sql, ",");
        sql.append(") values");

        int index = 0;
        for (Map<String, Object> map : fieldValueMapList) {
            sql.append(" (").append(sqlPlaceholder(map.size())).append(")").append(",");
            for (String field : fields) {
                values[index++] = map.get(field);
            }
        }
        sql.setLength(sql.length() - 1);

        return LeftRight.build(sql.toString(), values);
    }

    public static LeftRight<String, Object[]> insertSelect(Class<?> entityClass, AbstractQuery<?> query) {
        List<JoinQueryParam> joins = null;
        if (query instanceof EntityQuery) {
            joins = ((EntityQuery) query).getJoinAll();
        }

        List<String> insertFields = new ArrayList<>();
        // select & join select
        appendSelects(query, joins, null, null, insertFields, false);
        // expression & join expression
        appendSelects(query, joins, null, null, insertFields, true);

        Map<String, String> fieldColumns = FieldUtils.entityFieldColumns(entityClass);

        StringBuilder sql = new StringBuilder("insert into ");
        sql.append(getTableName(entityClass)).append(" (");
        for (String field : insertFields) {
            sql.append("`").append(fieldColumns.getOrDefault(field, field)).append("`").append(",");
        }
        trimEndSql(sql, ",");
        sql.append(") ");
        LeftRight<String, Object[]> leftRight = select(query);
        sql.append(leftRight.getLeft());
        leftRight.setLeft(sql.toString());
        return leftRight;
    }

    public static LeftRight<String, Object[]> select(AbstractQuery<?> query) {
        String tableAs = null;
        List<JoinQueryParam> joins = null;
        Map<String, Class<?>> entityClassMap = new HashMap<>();
        if (query instanceof EntityQuery) {
            joins = ((EntityQuery) query).getJoinAll();
            tableAs = ((EntityQuery) query).getTableAs();
            entityClassMap.put(tableAs, query.getEntityClass());
            if (joins != null) {
                joins.forEach(q -> entityClassMap.put(q.getJoinEntityQuery().getTableAs(), q.getJoinEntityQuery().getEntityClass()));
            }
        } else {
            entityClassMap.put(null, query.getEntityClass());
        }
        Class<?> entityClass = query.getEntityClass();
        StringBuilder sql = new StringBuilder();
        List<Object> values = new ArrayList<>();
        sql.append("select ");
        if (query.isDistinct()) {
            sql.append("distinct ");
        }

        // select & join select
        appendSelects(query, joins, entityClassMap, sql, null, false);
        // expression & join expression
        appendSelects(query, joins, entityClassMap, sql, null, true);
        trimEndSql(sql, ",");

        sql.append(" from ").append(getTableName(entityClass));
        if (tableAs != null) {
            sql.append(" ").append(tableAs);
        }
        if (joins != null) {
            // join
            appendJoins(joins, sql, values);
        }
        sql.append(" where");
        // filter & join filter
        appendFilter(entityClassMap, query.getFilters(), sql, values);
        if (joins != null) {
            for (JoinQueryParam join : joins) {
                EntityQuery mainEntityQuery = join.getMainEntityQuery();
                EntityQuery joinEntityQuery = join.getJoinEntityQuery();
                if (joinEntityQuery.hasFilters()) {
                    sql.append(" and");
                    appendFilter(new HashMap<String, Class<?>>() {{
                        put(mainEntityQuery.getTableAs(), mainEntityQuery.getEntityClass());
                        put(joinEntityQuery.getTableAs(), joinEntityQuery.getEntityClass());
                    }}, joinEntityQuery.getFilters(), sql, values);
                }
            }
        }
        trimEndSql(sql, "where");
        // group by
        appendGroups(entityClassMap, query.getGroupBys(), sql);
        // having
        appendFilter(entityClassMap, query.getHaving(), sql, values);
        // union all
        List<UnionQueryParam> unions = query.getUnions();
        if (unions != null) {
            for (UnionQueryParam union : unions) {
                LeftRight<String, Object[]> leftRight = select(union.getUnion());
                sql.append(union.isUnionAll() ? " union all " : " union ").append(leftRight.getLeft()).append(" ");
                values.addAll(Arrays.asList(leftRight.getRight()));
            }
        }
        // order by
        appendOrders(entityClassMap, query.getOrders(), sql);
        // page
        if (query.getPageParam() != null) {
            query.getPageParam().handleSqlValue(sql, values);
        }
        return LeftRight.build(sql.toString(), values.toArray());
    }

    private static void appendSelects(AbstractQuery<?> query, List<JoinQueryParam> joins, Map<String, Class<?>> entityClassMap, StringBuilder sql, List<String> insertFields, boolean isExpression) {
        if (isExpression) {
            appendSelects(entityClassMap, query.getSelectExpression(), sql, insertFields);
            if (joins != null) {
                for (JoinQueryParam join : joins) {
                    EntityQuery mainEntityQuery = join.getMainEntityQuery();
                    EntityQuery joinEntityQuery = join.getJoinEntityQuery();
                    if (joinEntityQuery.getSelectExpression() != null) {
                        appendSelects(new HashMap<String, Class<?>>() {{
                            put(mainEntityQuery.getTableAs(), mainEntityQuery.getEntityClass());
                            put(joinEntityQuery.getTableAs(), joinEntityQuery.getEntityClass());
                        }}, joinEntityQuery.getSelectExpression(), sql, insertFields);
                    }
                }
            }
        } else {
            appendSelects(entityClassMap, query.getSelects(), query.getAliasMap(), sql, insertFields);
            if (joins != null) {
                for (JoinQueryParam join : joins) {
                    EntityQuery mainEntityQuery = join.getMainEntityQuery();
                    EntityQuery joinEntityQuery = join.getJoinEntityQuery();
                    if (!joinEntityQuery.getSelects().isEmpty()) {
                        appendSelects(new HashMap<String, Class<?>>() {{
                            put(mainEntityQuery.getTableAs(), mainEntityQuery.getEntityClass());
                            put(joinEntityQuery.getTableAs(), joinEntityQuery.getEntityClass());
                        }}, joinEntityQuery.getSelects(), joinEntityQuery.getAliasMap(), sql, insertFields);
                    }
                }
            }
        }
    }

    public static LeftRight<String, Object[]> update(AbstractUpdate<?> update) {
        Map<String, Class<?>> entityClassMap = new HashMap<>();
        StringBuilder sql = new StringBuilder();
        List<Object> values = new ArrayList<>();
        sql.append("update ");
        sql.append(getTableName(update.getEntityClass()));
        List<JoinUpdateParam> joins = null;
        if (update instanceof EntityUpdate) {
            joins = ((EntityUpdate) update).getJoinAll();
            String tableAs = ((EntityUpdate) update).getTableAs();
            if (tableAs != null) {
                sql.append(" ").append(tableAs);
            }
            entityClassMap.put(tableAs, update.getEntityClass());
            if (joins != null) {
                joins.forEach(q -> entityClassMap.put(q.getJoinEntityUpdate().getTableAs(), q.getJoinEntityUpdate().getEntityClass()));
            }
        } else {
            entityClassMap.put(null, update.getEntityClass());
        }
        if (joins != null) {
            // join on
            for (JoinUpdateParam join : joins) {
                EntityUpdate mainEntityUpdate = join.getMainEntityUpdate();
                EntityUpdate joinEntityUpdate = join.getJoinEntityUpdate();
                sql.append(" ").append(join.getJoinType().getJoin());
                sql.append(" ").append(getTableName(joinEntityUpdate.getEntityClass()));
                if (joinEntityUpdate.getTableAs() != null) {
                    sql.append(" ").append(joinEntityUpdate.getTableAs());
                }
                List<Filter> onFilters = join.getOnFilters();
                if (onFilters != null && onFilters.size() > 0) {
                    sql.append(" on");
                    appendFilter(new HashMap<String, Class<?>>() {{
                        put(mainEntityUpdate.getTableAs(), mainEntityUpdate.getEntityClass());
                        put(joinEntityUpdate.getTableAs(), joinEntityUpdate.getEntityClass());
                    }}, onFilters, sql, values);
                }
            }
        }
        sql.append(" set");
        appendUpdates(entityClassMap, update.getUpdates(), sql, values);
        if (joins != null) {
            for (JoinUpdateParam join : joins) {
                EntityUpdate mainEntityUpdate = join.getMainEntityUpdate();
                EntityUpdate joinEntityUpdate = join.getJoinEntityUpdate();
                if (joinEntityUpdate.getUpdates() != null) {
                    sql.append(",");
                    appendUpdates(new HashMap<String, Class<?>>() {{
                        put(mainEntityUpdate.getTableAs(), mainEntityUpdate.getEntityClass());
                        put(joinEntityUpdate.getTableAs(), joinEntityUpdate.getEntityClass());
                    }}, joinEntityUpdate.getUpdates(), sql, values);
                }
            }
        }
        sql.append(" where");
        appendFilter(entityClassMap, update.getFilters(), sql, values);
        if (joins != null) {
            for (JoinUpdateParam join : joins) {
                EntityUpdate mainEntityUpdate = join.getMainEntityUpdate();
                EntityUpdate joinEntityUpdate = join.getJoinEntityUpdate();
                if (joinEntityUpdate.hasFilters()) {
                    sql.append(" and");
                    appendFilter(new HashMap<String, Class<?>>() {{
                        put(mainEntityUpdate.getTableAs(), mainEntityUpdate.getEntityClass());
                        put(joinEntityUpdate.getTableAs(), joinEntityUpdate.getEntityClass());
                    }}, joinEntityUpdate.getFilters(), sql, values);
                }
            }
        }
        trimEndSql(sql, "where");
        return LeftRight.build(sql.toString(), values.toArray());
    }

    public static LeftRight<String, Object[]> delete(Delete delete) {
        StringBuilder sql = new StringBuilder();
        List<Object> values = new ArrayList<>();
        sql.append("delete from ").append(getTableName(delete.getEntityClass())).append(" where");
        appendFilter(new HashMap<String, Class<?>>() {{
            put(null, delete.getEntityClass());
        }}, delete.getFilters(), sql, values);
        return LeftRight.build(sql.toString(), values.toArray());
    }

    public static void appendSelects(Map<String, Class<?>> entityClassMap, List<Expression> selectExpression, StringBuilder sql, List<String> insertFields) {
        if (selectExpression == null || selectExpression.isEmpty()) return;
        StringBuilder sb = new StringBuilder();
        for (Expression expression : selectExpression) {
            if (insertFields != null) {
                if (expression.getAlias() == null) {
                    throw new DbException(expression.getExpression() + "必须有别名对应新增字段");
                }
                insertFields.add(expression.getAlias());
                continue;
            }
            sb.append(" ").append(replaceSqlEval(expression.getExpression(), entityClassMap));
            if (expression.getAlias() != null) {
                sb.append(" as ").append(expression.getAlias());
            }
            sb.append(",");
        }
        if (sql != null) {
            sql.append(replaceSqlEval(sb.toString(), entityClassMap));
        }
    }

    public static void appendSelects(Map<String, Class<?>> entityClassMap, List<String> selects, Map<String, String> aliasMap, StringBuilder sql, List<String> insertFields) {
        if (selects == null || selects.isEmpty()) return;
        StringBuilder sb = new StringBuilder();
        for (String select : selects) {
            if (insertFields != null) {
                String field;
                if (aliasMap.containsKey(select)) {
                    field = aliasMap.get(select);
                } else if (select.contains(".")) {
                    field = select.substring(select.indexOf(".") + 1);
                } else {
                    field = select;
                }
                insertFields.add(field);
                continue;
            }
            sb.append(" ").append(handleFieldToColumn(select, entityClassMap));
            if (aliasMap.containsKey(select)) {
                sb.append(" as ").append(aliasMap.get(select));
            }
            sb.append(",");
        }
        if (sql != null) {
            sql.append(securityCheck(sb.toString()));
        }
    }

    public static void appendJoins(List<JoinQueryParam> joins, StringBuilder sql, List<Object> values) {
        if (joins == null || joins.isEmpty()) return;
        for (JoinQueryParam join : joins) {
            EntityQuery mainEntityQuery = join.getMainEntityQuery();
            EntityQuery joinEntityQuery = join.getJoinEntityQuery();
            sql.append(" ").append(join.getJoinType().getJoin());
            sql.append(" ").append(getTableName(joinEntityQuery.getEntityClass()));
            if (joinEntityQuery.getTableAs() != null) {
                sql.append(" ").append(joinEntityQuery.getTableAs());
            }
            List<Filter> filters = join.getOnFilters();
            if (filters != null && filters.size() > 0) {
                sql.append(" on");
                appendFilter(new HashMap<String, Class<?>>() {{
                    put(mainEntityQuery.getTableAs(), mainEntityQuery.getEntityClass());
                    put(joinEntityQuery.getTableAs(), joinEntityQuery.getEntityClass());
                }}, filters, sql, values);
            }
        }
    }

    public static void appendFilter(Map<String, Class<?>> entityClassMap, List<Filter> filters, StringBuilder sql, List<Object> values) {
        if (filters == null || filters.isEmpty()) return;
        for (Filter filter : filters) {
            sql.append(" ");
            if (filter.isSpecial()) {
                if (filter.getValue() instanceof SubQueryParam) {
                    // 子查询
                    sql.append(handleFieldToColumn(filter.getField(), entityClassMap));
                    sql.append(" ").append(filter.getOperator().getOpt()).append(" (");
                    SubQueryParam subQueryParam = (SubQueryParam) filter.getValue();
                    LeftRight<String, Object[]> leftRight = select(subQueryParam.getQuery());
                    sql.append(leftRight.getLeft());
                    values.addAll(Arrays.asList(leftRight.getRight()));
                    sql.append(")");
                } else if (filter.getValue() instanceof Expression) {
                    // 表达式
                    sql.append(handleFieldToColumn(filter.getField(), entityClassMap));
                    sql.append(" ").append(filter.getOperator().getOpt()).append(" ");
                    Expression expression = (Expression) filter.getValue();
                    appendExpressionValue(entityClassMap, expression, sql, values);
                } else {
                    throw new DbException("不支持过滤器：" + filter);
                }
            } else if (filter.getField() == null || !filter.getSql().contains("?")) {
                sql.append(filter.getSql());
            } else {
                sql.append(handleFieldToColumn(filter.getField(), entityClassMap));
                sql.append(" ");
                sql.append(filter.getSql());
                Object value = filter.getValue();
                if (filter.getSql().indexOf("?") != filter.getSql().lastIndexOf("?")) {
                    if (value instanceof Collection) {
                        values.addAll((Collection<?>) value);
                    } else if (value instanceof Object[]) {
                        values.addAll(Arrays.asList((Object[]) value));
                    } else {
                        throw new DbException("异常SQL类型：" + filter.getSql());
                    }
                } else {
                    values.add(value);
                }
            }
        }
        trimEndSql(sql, "and");
    }

    public static void appendGroups(Map<String, Class<?>> entityClassMap, List<String> groupBys, StringBuilder sql) {
        if (groupBys == null || groupBys.isEmpty()) return;
        sql.append(" group by");
        for (String field : groupBys) {
            sql.append(" ").append(handleFieldToColumn(field, entityClassMap)).append(",");
        }
        trimEndSql(sql, ",");
    }

    public static void appendOrders(Map<String, Class<?>> entityClassMap, List<OrderParam> orders, StringBuilder sql) {
        if (orders == null || orders.isEmpty()) return;
        sql.append(" order by");
        for (OrderParam order : orders) {
            sql.append(" ").append(handleFieldToColumn(order.getField(), entityClassMap));
            if (!order.isAsc()) {
                sql.append(" desc");
            }
            sql.append(",");
        }
        trimEndSql(sql, ",");
    }

    public static void appendUpdates(Map<String, Class<?>> entityClassMap, Map<String, Object> updates, StringBuilder sql, List<Object> values) {
        if (updates == null || updates.isEmpty()) return;
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            sql.append(" ").append(handleFieldToColumn(key, entityClassMap)).append(" = ");
            if (value instanceof Expression) {
                Expression expression = (Expression) value;
                appendExpressionValue(entityClassMap, expression, sql, values);
            } else {
                sql.append("?");
                values.add(value);
            }
            sql.append(",");
        }
        trimEndSql(sql, ",");
    }

    private static void appendExpressionValue(Map<String, Class<?>> entityClassMap, Expression expression, StringBuilder sql, List<Object> values) {
        String eval = replaceSqlEval(expression.getExpression(), entityClassMap);
        if (eval.contains("#{")) {
            Object[] vs = expression.getValues();
            eval = FieldUtils.replaceSqlPlaceholder(eval, null, (as, field) -> {
                if (as != null && !"".equals(as) || !field.startsWith("value")) {
                    throw new DbException("占位符格式错误：" + field);
                }
                try {
                    int index = Integer.parseInt(field.substring(5)) - 1;
                    values.add(vs[index]);
                } catch (Exception e) {
                    throw new DbException("表达式错误：" + expression.getExpression(), e);
                }
                return "?";
            }, "#{", "}");
        }
        sql.append(eval);
    }

    private static String replaceSqlEval(String eval, Map<String, Class<?>> entityClassMap) {
        if (!eval.contains("${")) {
            return eval;
        }
        return FieldUtils.replaceSqlPlaceholder(eval, null, (as, field) -> {
            Class<?> clazz = entityClassMap.get(as);
            if (clazz == null && as == null && entityClassMap.size() == 1) {
                clazz = entityClassMap.values().iterator().next();
            }
            if (clazz == null) {
                return field;
            }
            return FieldUtils.entityFieldColumns(clazz).getOrDefault(field, field);
        });
    }

    private static String handleFieldToColumn(final String field, Map<String, Class<?>> entityClassMap) {
        int idx = field.indexOf(".");
        if (idx > 0) {
            String as = field.substring(0, idx);
            String _field = field.substring(idx + 1).trim();
            Class<?> clazz = entityClassMap.get(as);
            if (clazz == null) {
                return field;
            }
            return as + "." + FieldUtils.entityFieldColumns(clazz).getOrDefault(_field, _field);
        } else {
            Class<?> clazz = entityClassMap.size() == 1 ? entityClassMap.values().iterator().next() : entityClassMap.get(null);
            if (clazz == null) {
                return field;
            }
            return FieldUtils.entityFieldColumns(clazz).getOrDefault(field, field);
        }
    }

    private static void trimEndSql(StringBuilder sql, String end) {
        int idx = sql.lastIndexOf(end);
        int len = sql.length() - end.length();
        if (idx >= len) {
            sql.setLength(len);
        }
    }

    private static String securityCheck(String sql) {
        // TODO 检查SQL注入（特殊字符处理）
        return sql;
    }

}
