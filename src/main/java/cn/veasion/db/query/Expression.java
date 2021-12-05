package cn.veasion.db.query;

/**
 * Expression
 *
 * @author luozhuowei
 * @date 2021/12/2
 */
public class Expression {

    private String expression;
    private String alias;

    public Expression(String expression) {
        this.expression = expression;
    }

    public Expression(String expression, String alias) {
        this.expression = expression;
        this.alias = alias;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
