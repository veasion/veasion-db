package cn.veasion.db.dao;

import cn.veasion.db.base.UserInfoPO;

import java.util.Date;

/**
 * JdbcEntityDaoTest
 *
 * @author luozhuowei
 * @date 2021/12/5
 */
public class JdbcEntityDaoTest {

    public static void main(String[] args) {
        UserInfoDao userInfoDao = new UserInfoDao();
        System.out.println(userInfoDao.getIdField());
        System.out.println(userInfoDao.getEntityClass());
        UserInfoPO userInfo = getUserInfo();
        Long id = userInfoDao.add(userInfo);
        System.out.println(id);
        System.out.println(userInfo.getId());
    }

    private static UserInfoPO getUserInfo() {
        UserInfoPO userInfoPO = new UserInfoPO();
        userInfoPO.setUsername("veasion-" + System.currentTimeMillis());
        userInfoPO.setUserNike("伟神-"+System.currentTimeMillis());
        userInfoPO.setAge(18);
        userInfoPO.setTest("test");
        userInfoPO.setVersion(0);
        userInfoPO.setCreateTime(new Date());
        userInfoPO.setUpdateTime(new Date());
        userInfoPO.setIsDeleted(0L);
        return userInfoPO;
    }

}
