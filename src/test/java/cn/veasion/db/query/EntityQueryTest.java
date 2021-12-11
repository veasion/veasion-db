package cn.veasion.db.query;

import cn.veasion.db.base.Expression;
import cn.veasion.db.base.UserInfoPO;
import cn.veasion.db.dao.UserInfoDao;

import java.util.List;

/**
 * EntityQueryTest
 *
 * @author luozhuowei
 * @date 2021/12/8
 */
public class EntityQueryTest {

    public static void main(String[] args) {
        UserInfoDao userInfoDao = new UserInfoDao();

        // 查询全部
        List<UserInfoPO> list = userInfoDao.queryList(new EntityQuery(UserInfoPO.class).likeRight("userNike", "伟神"));
        System.out.println(list);

        // join 子查询
        EQ t1 = new EQ(UserInfoPO.class, "t1");
        t1.join(new SubQuery(new Q(), "t2").select("userNike", "t2UserNike").gt("id", 0)).on("userNike", "userNike");
        t1.selectAll().like("userNike", "伟神");
        list = userInfoDao.queryList(t1);
        System.out.println(list);

        // join 查询（u1关联u2, u2关联u3, u3关联u4, u1关联u5）
        EQ u1 = new EQ(UserInfoPO.class, "u1");
        EQ u2 = new EQ(UserInfoPO.class, "u2");
        EQ u3 = new EQ(UserInfoPO.class, "u3");
        EQ u4 = new EQ(UserInfoPO.class, "u4");

        u1.join(u2.select("id", "uId").eq("id", 2)).on("id", "id");
        u2.join(u3.gt("id", 3)).on("id", "id");
        u3.join(u4.gt("id", 4).select("id", "u4Id")).on("id", "id");

        u1.join(new EQ(UserInfoPO.class, "u5").select("id", "u5Id").gt("id", 5)).on("id", "id");

        u1.selectAll().select("u2.id", "u2Id").gt("id", 1).select("u4.userNike");
        u3.select("id", "u3Id").select("u2.userNike", "user2Nike");
        u4.selectExpression(Expression.select("ifnull(${age}, 20)", "u4age"));

        list = userInfoDao.queryList(u1);
        System.out.println(list);
    }

}
