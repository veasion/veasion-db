package cn.veasion.db.base;

import cn.veasion.db.DbException;

/**
 * Expression
 *
 * @author luozhuowei
 * @date 2021/12/2
 */
public class Expression {

    private String alias;
    private String expression;
    private Object[] values;
    private Object[] sqlValues;

    private Expression() {
    }

    /**
     * 表达式查询（带函数或SQL类不安全查询）
     *
     * @param expression 表达式，其中#{}和${}中间可以使用占位字段，解析时#{}会默认替换成values对应的值，${}替换成字段对应表中的列名 <br><br>
     *                   示例一：IFNULL(${userName}, 'xxx') <br>
     *                   示例二：IF(${u.type} = 1, AVG(${s.score}), SUM(${s.score})) <br>
     * @param alias      别名，如 userName
     * @param values     占位值，对应 #{value1}, #{value2}, #{value3}, #{value...}，通过占位符拼接参数防SQL注入
     */
    public static Expression select(String expression, String alias, Object... values) {
        Expression o = new Expression();
        o.expression = expression;
        o.alias = alias;
        o.values = values;
        return o;
    }

    /**
     * 表达式过滤（带函数或SQL类不安全过滤条件）
     *
     * @param expression 表达式，其中#{}和${}中间可以使用占位字段，解析时#{}会默认替换成values对应的值，${}替换成字段对应表中的列名 <br><br>
     *                   示例一：NOW() <br>
     *                   示例二：DATE_FORMAT(#{value1},'%Y-%m-%d') <br>
     *                   示例二：${age} + #{value1} + #{age} <br>
     * @param values     占位值，对应 #{value1}, #{value2}, #{value3}, #{value...}，通过占位符拼接参数防SQL注入
     */
    public static Expression filter(String expression, Object... values) {
        Expression o = new Expression();
        o.expression = expression;
        o.values = values;
        return o;
    }

    /**
     * 表达式更新（带函数或SQL类不安全更新数据）
     *
     * @param expression 表达式，其中#{}和${}中间可以使用占位字段，解析时#{}会默认替换成对象字段和values对应的值，${}替换成字段对应表中的列名 <br><br>
     *                   示例一：${version} + 1 <br>
     *                   示例二：${totalAmount} + #{totalAmount} <br>
     *                   示例三：${id} + #{value1} + #{value2} <br>
     * @param values     占位值，对应 #{value1}, #{value2}, #{value3}, #{value...}，通过占位符拼接参数防SQL注入
     */
    public static Expression update(String expression, Object... values) {
        Expression o = new Expression();
        o.expression = expression;
        o.values = values;
        return o;
    }

    /**
     * SQL表达式
     *
     * @param sql       示例：xxx = ?
     * @param sqlValues SQL中 ? 对应的占位符
     */
    public static Expression sql(String sql, Object... sqlValues) {
        Expression o = new Expression();
        o.expression = sql;
        o.sqlValues = sqlValues;
        return o;
    }

    public Expression tableAs(String tableAs) {
        if (tableAs != null && expression != null && expression.contains("${")) {
            int startIdx = 0, idx;
            StringBuilder sb = new StringBuilder();
            while ((idx = expression.indexOf("${", startIdx)) > -1) {
                int endIdx = expression.indexOf("}", idx);
                if (endIdx == -1) {
                    throw new DbException("错误表达式：" + expression);
                }
                sb.append(expression.substring(startIdx, idx + 2));
                String field = expression.substring(idx + 2, endIdx + 1);
                sb.append(field.contains(".") ? field : (tableAs + "." + field));
                startIdx = endIdx + 1;
            }
            sb.append(expression.substring(startIdx));
            expression = sb.toString();
        }
        return this;
    }

    public Expression setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public String getExpression() {
        return expression;
    }

    public Object[] getValues() {
        return values;
    }

    public Object[] getSqlValues() {
        return sqlValues;
    }

}
