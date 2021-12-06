package cn.veasion.db.dao;

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
    }

}
