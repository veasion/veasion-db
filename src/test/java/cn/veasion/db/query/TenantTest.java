package cn.veasion.db.query;

import cn.veasion.db.BaseTest;
import cn.veasion.db.interceptor.TenantInterceptor;

/**
 * TenantTest
 *
 * @author luozhuowei
 * @date 2022/12/1
 */
public class TenantTest extends BaseTest {

    public static void main(String[] args) {
        // 租户拦截器测试
        // 默认情况下应该是只能查询当前登录用户对应的租户数据，这里只是简单模拟演示

        // 默认 -1 租户用户数据
        println(saasUserDao.queryList(new Q()));

        println(TenantInterceptor.withTenantId(1000L, () -> {
            // 1000 租户用户数据
            return saasUserDao.queryList(new Q());
        }));

        println(TenantInterceptor.withSkip(() -> {
            // 全部租户用户数据
            return saasUserDao.queryList(new Q());
        }));
    }

}
