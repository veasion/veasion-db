package cn.veasion.db.update;

import cn.veasion.db.base.Expression;
import cn.veasion.db.base.UserInfoPO;
import cn.veasion.db.dao.UserInfoDao;

import java.util.Date;

/**
 * UpdateTest
 *
 * @author luozhuowei
 * @date 2021/12/8
 */
public class UpdateTest {

    public static void main(String[] args) {
        UserInfoDao userInfoDao = new UserInfoDao();

        // 更新全部
        System.out.println(userInfoDao.update(new Update("age", 20)));

        // 更新有值字段
        UserInfoPO userInfoPO = new UserInfoPO();
        userInfoPO.setId(5L);
        userInfoPO.setUserNike("测试~");
        userInfoPO.setIsDeleted(0L);
        userInfoPO.setUpdateTime(new Date());
        System.out.println(userInfoDao.updateById(userInfoPO));

        // 指定字段更新
        userInfoDao.update(new EntityUpdate(userInfoPO).updateFields("userNike", "id", "isDeleted").eq("id").excludeUpdateFilterFields());

        // 关联更新
        EU eu = new EU(userInfoPO, "u");
        eu.updateFields("userNike", "isDeleted");
        eu.join(new EU(userInfoPO, "u2").update("age", 18).eq("id")).on("id", "id");
        eu.gt("id", 0);
        System.out.println(userInfoDao.update(eu));

        // 复杂关联更新（u1关联u2, u2关联u3, u3关联u4, u1关联u5）
        EU u1 = new EU(userInfoPO, "u1");
        EU u2 = new EU(UserInfoPO.class, "u2");
        EU u3 = new EU(UserInfoPO.class, "u3");
        EU u4 = new EU(UserInfoPO.class, "u4");

        u1.join(u2.update("age", 20).eq("id", 2)).on("id", "id");
        u2.join(u3.gt("id", 3)).on("id", "id");
        u3.join(u4.gt("id", 4).update("age", 40)).on("id", "id");

        u1.join(new EU(UserInfoPO.class, "u5").update("age", 50).gt("id", 5)).on("id", "id");

        u1.updateFields("userNike", "isDeleted").update("u2.isDeleted", 1).update("u4.userNike", "user4Nike").gt("id", 1);
        u3.update("username", "user3name").update("u2.userNike", "user2Nike");
        u4.updateExpression("age", Expression.update("ifnull(${age}, #{value1})", 20));
        System.out.println(userInfoDao.update(u1));
    }

}
