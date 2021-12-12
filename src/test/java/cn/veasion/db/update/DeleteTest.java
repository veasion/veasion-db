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
        // 因为 BaseTest 父类的 static 方法中指定了逻辑删除拦截器，所以这里的删除都是逻辑删除
        // 可以通过 LogicDeleteInterceptor.skip 方法来跳过不走逻辑删除拦截器逻辑

        // 逻辑删除
        println(studentDao.deleteById(1L));
        println(studentDao.getById(1L));

        // 恢复
        LogicDeleteInterceptor.skip(true);
        println(studentDao.update(new U().update("isDeleted", 0).eq("id", 1)));
        println(studentDao.getById(1L));

        // 物理删除
        LogicDeleteInterceptor.skip(true);
        println(studentDao.deleteById(1L));
        println(studentDao.getById(1L));
    }

}
