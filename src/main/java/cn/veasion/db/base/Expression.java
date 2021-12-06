package cn.veasion.db.base;

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

    private Expression() {
    }

    /**
     * 表达式查询（带函数或SQL类不安全查询）
     *
     * @param expression 表达式，其中${}中间可以使用对象字段名，解析时会默认替换成表对应的列名<br><br>
     *                   示例一：IFNULL(${userName}, 'xxx') <br>
     *                   示例二：IF(${u.type} = 1, AVG(${s.score}), SUM(${s.score})) <br>
     * @param alias      别名，如 userName
     */
    public static Expression select(String expression, String alias) {
        Expression o = new Expression();
        o.expression = expression;
        o.alias = alias;
        return o;
    }

    /**
     * 表达式过滤（带函数或SQL类不安全过滤条件）
     *
     * @param expression 表达式，其中#{}和${}中间可以使用占位字段，解析时#{}会默认替换成对象字段对应的值或values对应的值，${}替换成字段对应表中的列名 <br><br>
     *                   示例一：NOW() <br>
     *                   示例二：DATE_FORMAT(#{value1},'%Y-%m-%d') <br>
     *                   示例二：${age} + #{value1} + #{age} <br>
     * @param values     占位值，对应 #{value1}, #{value2}, #{value3}, #{value...}
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
     * @param expression 表达式，其中#{}和${}中间可以使用占位字段，解析时#{}会默认替换成对象字段对应的值，${}替换成字段对应表中的列名 <br><br>
     *                   示例一：${version} + 1 <br>
     *                   示例二：-${id} <br>
     *                   示例三：${id} + #{id} <br>
     */
    public static Expression update(String expression) {
        Expression o = new Expression();
        o.expression = expression;
        return o;
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
}
