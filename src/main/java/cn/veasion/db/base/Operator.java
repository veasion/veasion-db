package cn.veasion.db.base;

/**
 * Operator
 *
 * @author luozhuowei
 * @date 2021/12/13
 */
public enum Operator {

    EQ("="),
    NEQ("<>"),
    GT(">"),
    GTE(">="),
    LT("<"),
    LTE("<="),
    IN("IN"),
    NOT_IN("NOT IN"),
    LIKE("LIKE"),
    BETWEEN("BETWEEN"),
    EXISTS("EXISTS"),
    NOT_EXISTS("NOT EXISTS"),
    NULL("IS NULL"),
    NOT_NULL("IS NOT NULL");

    String opt;

    Operator(String opt) {
        this.opt = opt;
    }

    public String getOpt() {
        return opt;
    }
}
