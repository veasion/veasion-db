package cn.veasion.db.jdbc;

import cn.veasion.db.AbstractFilter;
import cn.veasion.db.DbException;
import cn.veasion.db.FilterException;
import cn.veasion.db.base.Expression;
import cn.veasion.db.base.Filter;
import cn.veasion.db.base.Operator;
import cn.veasion.db.query.SubQueryParam;
import cn.veasion.db.utils.FieldUtils;
import cn.veasion.db.utils.LeftRight;
import cn.veasion.db.utils.ServiceLoaderUtils;
import cn.veasion.db.utils.TypeUtils;

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
    protected DynamicTableExt dynamicTableExt = ServiceLoaderUtils.dynamicTableExt();

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

    protected String getTableName(Class<?> entityClazz, AbstractFilter<?> filter, Object source) {
        String tableName = TypeUtils.getTableName(entityClazz);
        if (dynamicTableExt != null) {
            String changeTableName = dynamicTableExt.getTableName(tableName, entityClazz, filter, source);
            tableName = changeTableName != null ? changeTableName : tableName;
        }
        return tableName;
    }

    protected void appendFilter(Map<String, Class<?>> entityClassMap, List<Filter> filters) {
        if (filters == null || filters.isEmpty()) return;
        for (Filter filter : filters) {
            sql.append(" ");
            if (filter.isSpecial()) {
                if (filter.getValue() instanceof SubQueryParam) {
                    // 子查询
                    // exists、not exists 字段为空
                    if (!(filter.getField() == null && (Operator.EXISTS.equals(filter.getOperator()) || Operator.NOT_EXISTS.equals(filter.getOperator())))) {
                        sql.append(handleFieldToColumn(filter.getField(), entityClassMap)).append(" ");
                    }
                    sql.append(filter.getOperator().getOpt()).append(" (");
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
                    throw new FilterException("不支持过滤器：" + filter);
                }
            } else if (filter.getField() == null || !filter.getSql().contains("?")) {
                if (filter.getField() != null) {
                    sql.append(handleFieldToColumn(filter.getField(), entityClassMap)).append(" ");
                }
                sql.append(filter.getSql());
            } else {
                sql.append(handleFieldToColumn(filter.getField(), entityClassMap));
                sql.append(" ");
                sql.append(filter.getSql());
                Object value = filter.getValue();
                boolean multiple = filter.getSql().indexOf("?") != filter.getSql().lastIndexOf("?");
                if (value instanceof Collection) {
                    if (multiple || ((Collection<?>) value).size() == 1) {
                        values.addAll((Collection<?>) value);
                    } else {
                        throw new FilterException("异常SQL类型：" + filter.getSql());
                    }
                } else if (value instanceof Object[]) {
                    if (multiple || ((Object[]) value).length == 1) {
                        values.addAll(Arrays.asList((Object[]) value));
                    } else {
                        throw new FilterException("异常SQL类型：" + filter.getSql());
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
            eval = FieldUtils.replaceSqlPlaceholder(eval, null, (tableAs, field) -> {
                LeftRight<Boolean, Object> value = expressionValue(tableAs, field);
                if (value != null && Boolean.TRUE.equals(value.getLeft())) {
                    values.add(value.getRight());
                } else {
                    if (tableAs != null && !"".equals(tableAs) || !field.startsWith("value")) {
                        throw new DbException("占位符格式错误：" + field);
                    }
                    try {
                        int index = Integer.parseInt(field.substring(5)) - 1;
                        values.add(vs[index]);
                    } catch (Exception e) {
                        throw new DbException("表达式错误：" + expression.getExpression(), e);
                    }
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
                if (field.endsWith("`") && field.endsWith("`")) {
                    return field;
                } else {
                    return FieldUtils.humpToLine(field);
                }
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

    protected LeftRight<Boolean, Object> expressionValue(String tableAs, String field) {
        return null;
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
        if (endsWith(end)) {
            sql.setLength(sql.length() - end.length());
        }
    }

    protected boolean endsWith(String end) {
        int len1 = end.length();
        int len2 = sql.length() - len1;
        for (int i = 0; i < len1; i++) {
            if (end.charAt(i) != sql.charAt(len2 + i)) {
                return false;
            }
        }
        return true;
    }

    protected String securityCheck(String sql) {
        // TODO 检查SQL注入（特殊字符处理）
        return sql;
    }

}
