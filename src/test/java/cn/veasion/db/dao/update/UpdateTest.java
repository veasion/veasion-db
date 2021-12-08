package cn.veasion.db.dao.update;

import cn.veasion.db.base.UserInfoPO;
import cn.veasion.db.dao.UserInfoDao;
import cn.veasion.db.update.EU;
import cn.veasion.db.update.EntityUpdate;
import cn.veasion.db.update.Update;

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
        System.out.println(userInfoDao.update(eu));;
    }

}
