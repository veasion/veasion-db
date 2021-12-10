package cn.veasion.db.base;

/**
 * JoinTypeEnum
 *
 * @author luozhuowei
 * @date 2021/12/3
 */
public enum JoinTypeEnum implements JoinType {

    JOIN("inner join"),
    LEFT_JOIN("left join"),
    RIGHT_JOIN("right join"),
    FULL_JOIN("full join");

    String join;

    JoinTypeEnum(String join) {
        this.join = join;
    }

    public String getJoin() {
        return join;
    }

}
