package cn.veasion.db.query;

import cn.veasion.db.base.Expression;

import java.io.Serializable;
import java.util.Objects;

/**
 * Window
 *
 * @author luozhuowei
 * @date 2022/11/4
 */
public class Window implements Serializable {

    private String alias;
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

    public String getAlias() {
        return alias;
    }

    public Expression getExpression() {
        return expression;
    }

}
