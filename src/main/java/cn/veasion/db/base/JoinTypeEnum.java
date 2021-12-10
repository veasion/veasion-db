package cn.veasion.db.base;

/**
 * JoinTypeEnum
 *
 * @author luozhuowei
 * @date 2021/12/3
 */
public enum JoinTypeEnum implements JoinType {

    JOIN("JOIN"),
    INNER_JOIN("INNER JOIN"),
    OUTER_JOIN("OUTER JOIN"),
    LEFT_JOIN("LEFT JOIN"),
    RIGHT_JOIN("RIGHT JOIN"),
    FULL_JOIN("FULL JOIN");

    String join;

    JoinTypeEnum(String join) {
        this.join = join;
    }

    public String getJoin() {
        return join;
    }

}
