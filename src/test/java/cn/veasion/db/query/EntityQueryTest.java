package cn.veasion.db.query;

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

        // join 查询
        EQ eq = new EQ(UserInfoPO.class, "u");
        eq.join(new EQ(UserInfoPO.class, "u2").select("id", "uId").eq("age", 18)).on("id", "id");
        eq.selectAll();
        eq.select("u2.id", "u2Id");
        eq.gt("id", 0);
        list = userInfoDao.queryList(eq);
        System.out.println(list);
    }

}