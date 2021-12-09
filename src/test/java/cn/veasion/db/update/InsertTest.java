package cn.veasion.db.update;

import cn.veasion.db.base.Expression;
import cn.veasion.db.base.UserInfoPO;
import cn.veasion.db.dao.UserInfoDao;
import cn.veasion.db.query.EQ;
import cn.veasion.db.query.Q;

import java.util.Arrays;
import java.util.Date;

/**
 * InsertTest
 *
 * @author luozhuowei
 * @date 2021/12/9
 */
public class InsertTest {

    public static void main(String[] args) {
        UserInfoDao userInfoDao = new UserInfoDao();

        // 单个新增
        UserInfoPO userInfo = getUserInfo();
        Long id = userInfoDao.add(userInfo);
        System.out.println(id);
        System.out.println(userInfo.getId());

        // 多个新增
        Long[] ids = userInfoDao.batchAdd(Arrays.asList(getUserInfo(), getUserInfo(), getUserInfo()));
        System.out.println(Arrays.toString(ids));

        // 批量新增 insert select
        ids = userInfoDao.batchAdd(new BatchEntityInsert(new Q("username", "userNike", "age").lte("id", 6)));
        System.out.println(Arrays.toString(ids));

        // 复杂批量新增 insert select
        EQ eq = new EQ(UserInfoPO.class, "u1");
        eq.join(new EQ(UserInfoPO.class, "u2").lte("id", 6)).on("id", "id");
        eq.gt("id", 1);
        eq.selectExpression(Expression.select("ifnull(${u2.age}, ${u1.id})", "age"));
        eq.selects("username", "isDeleted");
        eq.select("u2.username", "userNike");
        ids = userInfoDao.batchAdd(new BatchEntityInsert(eq));
        System.out.println(Arrays.toString(ids));
    }

    private static UserInfoPO getUserInfo() {
        UserInfoPO userInfoPO = new UserInfoPO();
        userInfoPO.setUsername("veasion-" + System.currentTimeMillis());
        userInfoPO.setUserNike("伟神-" + System.currentTimeMillis());
        userInfoPO.setAge(18);
        userInfoPO.setTest("test");
        userInfoPO.setVersion(0);
        userInfoPO.setCreateTime(new Date());
        userInfoPO.setUpdateTime(new Date());
        userInfoPO.setIsDeleted(0L);
        return userInfoPO;
    }

}
