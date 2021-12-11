package cn.veasion.db.jdbc;

import cn.veasion.db.DbException;
import cn.veasion.db.base.Expression;
import cn.veasion.db.base.Filter;
import cn.veasion.db.base.Table;
import cn.veasion.db.query.SubQueryParam;
import cn.veasion.db.utils.FieldUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * AbstractSQL
 *
 * @author luozhuowei
 * @date 2021/12/10
 */
public abstract class AbstractSQL<T> {

    protected StringBuilder sql = new StringBuilder();
    protected List<Object> values = new ArrayList<>();

    public String getSQL() {
        return sql.toString();
    }

    public Object[] getValues() {
        return values.toArray();
    }

    public void reset() {
        sql.setLength(0);
        values.clear();
    }

    public abstract T build();

    protected String getTableName(Class<?> entityClazz) {
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

    protected void appendFilter(Map<String, Class<?>> entityClassMap, List<Filter> filters) {
        if (filters == null || filters.isEmpty()) return;
        for (Filter filter : filters) {
            sql.append(" ");
            if (filter.isSpecial()) {
                if (filter.getValue() instanceof SubQueryParam) {
                    // 子查询
                    sql.append(handleFieldToColumn(filter.getField(), entityClassMap));
                    sql.append(" ").append(filter.getOperator().getOpt()).append(" (");
                    SubQueryParam subQueryParam = (SubQueryParam) filter.getValue();
                    QuerySQL querySQL = QuerySQL.build(subQueryParam.getQuery());
                    sql.append(querySQL.sql);
                    values.addAll(querySQL.values);
                    sql.append(")");
                } else if (filter.getValue() instanceof Expression) {
                    // 表达式
                    sql.append(handleFieldToColumn(filter.getField(), entityClassMap));
                    sql.append(" ").append(filter.getOperator().getOpt()).append(" ");
                    Expression expression = (Expression) filter.getValue();
                    appendExpressionValue(entityClassMap, expression);
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
        trimEndSql("AND");
    }

    protected void appendExpressionValue(Map<String, Class<?>> entityClassMap, Expression expression) {
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

    protected String replaceSqlEval(String eval, Map<String, Class<?>> entityClassMap) {
        if (!eval.contains("${")) {
            return eval;
        }
        return FieldUtils.replaceSqlPlaceholder(eval, null, (tableAs, field) -> {
            String toColumn = toColumn(tableAs, field);
            if (toColumn != null) {
                return toColumn;
            }
            Class<?> clazz = entityClassMap.get(tableAs);
            if (clazz == null && tableAs == null && entityClassMap.size() == 1) {
                clazz = entityClassMap.values().iterator().next();
            }
            if (clazz == null) {
                return field;
            }
            return FieldUtils.entityFieldColumns(clazz).getOrDefault(field, field);
        });
    }

    protected String handleFieldToColumn(final String field, Map<String, Class<?>> entityClassMap) {
        int idx = field.indexOf(".");
        if (idx > 0) {
            String tableAs = field.substring(0, idx);
            String _field = field.substring(idx + 1).trim();
            String toColumn = toColumn(tableAs, _field);
            if (toColumn != null) {
                return tableAs + "." + toColumn;
            }
            Class<?> clazz = entityClassMap.get(tableAs);
            if (clazz == null) {
                return field;
            }
            return tableAs + "." + FieldUtils.entityFieldColumns(clazz).getOrDefault(_field, _field);
        } else {
            String toColumn = toColumn(null, field);
            if (toColumn != null) {
                return toColumn;
            }
            Class<?> clazz = entityClassMap.size() == 1 ? entityClassMap.values().iterator().next() : entityClassMap.get(null);
            if (clazz == null) {
                return field;
            }
            return FieldUtils.entityFieldColumns(clazz).getOrDefault(field, field);
        }
    }

    protected String toColumn(String tableAs, String field) {
        return null;
    }

    public static String sqlPlaceholder(int len) {
        String[] array = new String[len];
        Arrays.fill(array, "?");
        return String.join(",", array);
    }

    protected void trimEndSql(String end) {
        int idx = sql.lastIndexOf(end);
        int len = sql.length() - end.length();
        if (idx >= len) {
            sql.setLength(len);
        }
    }

    protected String securityCheck(String sql) {
        // TODO 检查SQL注入（特殊字符处理）
        return sql;
    }

}
