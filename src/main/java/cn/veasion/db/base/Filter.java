package cn.veasion.db.base;

import cn.veasion.db.FilterException;
import cn.veasion.db.query.SubQueryParam;
import cn.veasion.db.utils.FilterUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * Filter
 *
 * @author luozhuowei
 * @date 2021/12/1
 */
public class Filter {

    public static Filter eq(String field, Object value) {
        return build(field, Operator.EQ, value);
    }

    public static Filter neq(String field, Object value) {
        return build(field, Operator.NEQ, value);
    }

    public static Filter gt(String field, Object value) {
        return build(field, Operator.GT, value);
    }

    public static Filter gte(String field, Object value) {
        return build(field, Operator.GTE, value);
    }

    public static Filter lt(String field, Object value) {
        return build(field, Operator.LT, value);
    }

    public static Filter lte(String field, Object value) {
        return build(field, Operator.LTE, value);
    }

    public static Filter in(String field, Collection<?> value) {
        return inOrNotIn(field, Operator.IN, value);
    }

    public static Filter in(String field, Object[] value) {
        return inOrNotIn(field, Operator.IN, value);
    }

    public static Filter notIn(String field, Collection<?> value) {
        return inOrNotIn(field, Operator.NOT_IN, value);
    }

    public static Filter notIn(String field, Object[] value) {
        return inOrNotIn(field, Operator.NOT_IN, value);
    }

    public static Filter like(String field, Object value) {
        if (value == null) return null;
        return build(field, Operator.LIKE, like(value, true, true));
    }

    public static Filter likeLeft(String field, Object value) {
        if (value == null) return null;
        return build(field, Operator.LIKE, like(value, true, false));
    }

    public static Filter likeRight(String field, Object value) {
        if (value == null) return null;
        return build(field, Operator.LIKE, like(value, false, true));
    }

    public static Filter isNull(String field) {
        return build(field, Operator.NULL);
    }

    public static Filter isNotNull(String field) {
        return build(field, Operator.NOT_NULL);
    }

    public static Filter between(String field, Object value1, Object value2) {
        return build(field, Operator.BETWEEN, new Object[]{value1, value2}, Operator.BETWEEN.getOpt().concat(" ? AND ?"));
    }

    public static Filter subQuery(String field, Operator operator, SubQueryParam subQueryParam) {
        return build(field, operator, Objects.requireNonNull(subQueryParam), null).special();
    }

    /**
     * 表达式过滤
     *
     * @param expression 表达式，其中#{}和${}中间可以使用占位字段，解析时#{}会默认替换成values对应的值，${}替换成字段对应表中的列名 <br><br>
     *                   示例一：NOW() <br>
     *                   示例二：DATE_FORMAT(#{value1},'%Y-%m-%d') <br>
     *                   示例二：${age} + #{value1} + #{age} <br>
     * @param values     占位值，对应 #{value1}, #{value2}, #{value3}, #{value...}，通过占位符拼接参数防SQL注入
     */
    public static Filter expression(String field, Operator operator, String expression, Object... values) {
        return expression(field, operator, Expression.filter(expression, values));
    }

    public static Filter expression(String field, Operator operator, Expression expression) {
        return build(field, operator, expression, null).special();
    }

    /**
     * SQL过滤
     *
     * @param sqlFilterHandler 获取SQL接口，参数支持字段处理
     * @param values           占位符对应值
     */
    public static Filter sqlFilter(SqlFilterHandler sqlFilterHandler, Object... values) {
        return new SqlFilter(sqlFilterHandler, values);
    }

    public static Filter AND = build("AND");
    public static Filter OR = build("OR");
    public static Filter LEFT_BRACKET = build("(");
    public static Filter RIGHT_BRACKET = build(")");

    public static Filter and() {
        return AND;
    }

    public static Filter or() {
        return OR;
    }

    public static Filter leftBracket() {
        return LEFT_BRACKET;
    }

    public static Filter rightBracket() {
        return RIGHT_BRACKET;
    }

    private static Filter inOrNotIn(String field, Operator operator, Object value) {
        int len = value instanceof Collection ? ((Collection<?>) value).size() : ((Object[]) value).length;
        if (len == 0) {
            throw new FilterException(field + " " + operator.opt + " 空集合");
        }
        String[] array = new String[len];
        Arrays.fill(array, "?");
        return build(field, operator, value, operator.getOpt().concat(" (").concat(String.join(",", array)).concat(")"));
    }

    private static Filter build(String sql) {
        Filter filter = new Filter();
        filter.sql = sql;
        return filter;
    }

    private static Filter build(String field, Operator operator) {
        return build(field, operator, null, operator.getOpt());
    }

    private static Filter build(String field, Operator operator, Object value) {
        return build(field, operator, value, operator.getOpt().concat(" ?"));
    }

    private static Filter build(String field, Operator operator, Object value, String sql) {
        Filter filter = new Filter();
        filter.field = field;
        filter.operator = operator;
        filter.value = value;
        filter.sql = sql;
        return filter;
    }

    private static String like(Object v, boolean left, boolean right) {
        String value = v != null ? v.toString() : null;
        if (left && right) return "%" + value + "%";
        if (left) return "%" + value;
        if (right) return value + "%";
        return value;
    }

    private Filter() {
    }

    private String field;
    private Operator operator;
    private String sql;
    private Object value;
    private boolean special;

    public Filter fieldAs(String tableAs) {
        field = FilterUtils.tableAsField(tableAs, field);
        return this;
    }

    public String getField() {
        return field;
    }

    public Operator getOperator() {
        return operator;
    }

    public String getSql() {
        return sql;
    }

    public Object getValue() {
        return value;
    }

    private Filter special() {
        this.special = true;
        return this;
    }

    public boolean isSpecial() {
        return special;
    }

    @Override
    public String toString() {
        if (operator != null && value != null) {
            return field + " " + operator.opt + " " + value;
        } else if (operator != null) {
            return field + " " + operator.opt;
        } else {
            return sql;
        }
    }

    @FunctionalInterface
    public interface SqlFilterHandler {
        String getSQL(ColumnFieldHandler columnFieldHandler);
    }

    @FunctionalInterface
    public interface ColumnFieldHandler {
        String asField(String columnField);
    }

    public static class SqlFilter extends Filter {

        private String tableAs;
        private SqlFilterHandler sqlFilterHandler;

        private SqlFilter(SqlFilterHandler sqlFilterHandler, Object... values) {
            this.sqlFilterHandler = sqlFilterHandler;
            if (values != null && values.length > 0) {
                super.value = Arrays.asList(values);
            }
        }

        @Override
        public String getSql() {
            if (tableAs != null) {
                return sqlFilterHandler.getSQL(field -> FilterUtils.tableAsField(tableAs, field));
            } else {
                return sqlFilterHandler.getSQL(field -> field);
            }
        }

        @Override
        public SqlFilter fieldAs(String tableAs) {
            this.tableAs = tableAs;
            return this;
        }

        @Override
        public boolean isSpecial() {
            return true;
        }

        @Override
        public String toString() {
            return getSql();
        }
    }

}
