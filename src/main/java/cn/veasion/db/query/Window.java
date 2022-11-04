package cn.veasion.db.query;

import cn.veasion.db.base.Expression;

import java.util.Objects;

/**
 * Window
 *
 * @author luozhuowei
 * @date 2022/11/4
 */
public class Window {

    private String alias;
    private boolean whereBefore;
    private Expression expression;

    /**
     * 开窗
     *
     * @param alias      别名
     * @param expression SQL表达式
     */
    public Window(String alias, Expression expression) {
        this.alias = alias;
        Objects.requireNonNull(expression, "window主体不能为空");
        this.expression = expression;
        if (alias == null && expression.getAlias() != null) {
            this.alias = expression.getAlias();
        }
        Objects.requireNonNull(alias, "window必须有别名");
    }

    /**
     * 开窗
     *
     * @param alias       别名
     * @param expression  SQL表达式
     * @param whereBefore mysql语法中window在where后，应设置为 false，clickhouse语法中则是在where前，应设置为 true
     */
    public Window(String alias, Expression expression, boolean whereBefore) {
        this(alias, expression);
        this.whereBefore = whereBefore;
    }

    public String getAlias() {
        return alias;
    }

    public Expression getExpression() {
        return expression;
    }

    public boolean isWhereBefore() {
        return whereBefore;
    }

}
