package cn.veasion.db.update;

import cn.veasion.db.BaseTest;
import cn.veasion.db.interceptor.LogicDeleteInterceptor;

/**
 * DeleteTest
 *
 * @author luozhuowei
 * @date 2021/12/12
 */
public class DeleteTest extends BaseTest {

    public static void main(String[] args) {

        // 逻辑删除
        println(studentDao.deleteById(1L));
        println(studentDao.getById(1L));

        // 恢复
        println(studentDao.update(new U().update("isDeleted", 0).eq("id", 1).neq("isDeleted", 0)));
        println(studentDao.getById(1L));

        try {
            // 物理删除
            LogicDeleteInterceptor.skip(true);
            println(studentDao.deleteById(1L));
            println(studentDao.getById(1L));
        } finally {
            LogicDeleteInterceptor.clearSkip();
        }
    }

}
