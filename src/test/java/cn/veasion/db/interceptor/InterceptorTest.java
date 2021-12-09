package cn.veasion.db.interceptor;

import cn.veasion.db.base.Expression;
import cn.veasion.db.base.Filter;
import cn.veasion.db.base.UserInfoPO;
import cn.veasion.db.dao.UserInfoDao;
import cn.veasion.db.query.EQ;
import cn.veasion.db.query.Q;
import cn.veasion.db.query.Query;
import cn.veasion.db.query.SubQueryParam;

import java.util.List;

/**
 * InterceptorTest
 *
 * @author luozhuowei
 * @date 2021/12/9
 */
public class InterceptorTest {

    public static void main(String[] args) {
        // 添加一个拦截器，默认查询没有逻辑删除的数据
        InterceptorUtils.addInterceptor(new LogicDeleteInterceptor("isDeleted", 0, Expression.update("${id}")));

        UserInfoDao userInfoDao = new UserInfoDao();
        // 普通查询
        System.out.println(userInfoDao.getById(1L));

        // 关联查询
        EQ eq = new EQ(UserInfoPO.class, "u1");
        eq.join(new EQ(UserInfoPO.class, "u2").lt("id", 10)).on("id", "id");
        eq.selects("id", "username", "userNike");
        List<UserInfoPO> list = userInfoDao.queryList(eq.gt("id", 1));
        System.out.println(list);

        // 子查询
        list = userInfoDao.queryList(new Q()
                .filterSubQuery("id", Filter.Operator.IN,
                        SubQueryParam.build(new Query("id").gte("id", 6).lte("id", 10))));
        System.out.println(list);

        // 逻辑删除
        userInfoDao.deleteById(1L);

    }

}
