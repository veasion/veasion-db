package cn.veasion.db.base;

import java.io.Serializable;

/**
 * Operator
 *
 * @author luozhuowei
 * @date 2021/12/13
 */
public enum Operator implements Serializable {

    EQ("="),
    NEQ("<>"),
    GT(">"),
    GTE(">="),
    LT("<"),
    LTE("<="),
    IN("IN"),
    NOT_IN("NOT IN"),
    LIKE("LIKE"),
    NOT_LIKE("NOT LIKE"),
    BETWEEN("BETWEEN"),
    NOT_BETWEEN("NOT BETWEEN"),
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

    public static Operator of(String opt) {
        if (opt == null || opt.length() == 0) {
            return null;
        }
        for (Operator value : values()) {
            if (value.opt.equalsIgnoreCase(opt) || value.name().equalsIgnoreCase(opt)) {
                return value;
            }
        }
        return null;
    }

}
