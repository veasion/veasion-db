package cn.veasion.db.update;

import cn.veasion.db.dao.UserInfoDao;

/**
 * DeleteTest
 *
 * @author luozhuowei
 * @date 2021/12/8
 */
public class DeleteTest {

    public static void main(String[] args) {
        UserInfoDao userInfoDao = new UserInfoDao();

        // 逻辑删除
        Delete delete = new Delete().eq("id", 1).convertUpdate(new U("isDeleted", 1));
        System.out.println(userInfoDao.delete(delete));

        // 物理删除
        System.out.println(userInfoDao.delete(new Delete().eq("id", 1)));
    }

}
