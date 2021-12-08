package cn.veasion.db.dao.query;

import cn.veasion.db.base.Expression;
import cn.veasion.db.base.Filter;
import cn.veasion.db.base.UserInfoPO;
import cn.veasion.db.dao.UserInfoDao;
import cn.veasion.db.query.Q;
import cn.veasion.db.query.Query;
import cn.veasion.db.query.SubQueryParam;

import java.util.Date;
import java.util.List;

/**
 * QueryTest
 *
 * @author luozhuowei
 * @date 2021/12/8
 */
public class QueryTest {

    public static void main(String[] args) {
        UserInfoDao userInfoDao = new UserInfoDao();
        // 查询全部
        List<UserInfoPO> list = userInfoDao.queryList(new Query());
        System.out.println(list);
        // 根据ID查询
        System.out.println(userInfoDao.getById(1L));
        // ID条件
        list = userInfoDao.queryList(new Query().gt("id", 1));
        System.out.println(list);
        // like 查询
        list = userInfoDao.queryList(new Q().like("userNike", "伟神").gt("id", 5));
        System.out.println(list);
        // 分页查一个
        System.out.println(userInfoDao.queryForType(new Q("createTime")
                .like("userNike", "伟神")
                .gt("id", 5).page(1, 1), Date.class));
        // count 查询
        System.out.println(userInfoDao.queryForType(new Q()
                .selectExpression(Expression.select("count(${id})", "count"))
                .filterExpression("id", Filter.Operator.GTE, Expression.filter("#{value1}+#{value2}", 1, 2)), Integer.class));
        // group 查询
        System.out.println(userInfoDao.listForMap(new Q().select("age")
                .selectExpression(Expression.select("count(1)", "count"))
                .in("id", new Object[]{1, 3, 5, 7}).desc("id").asc("age").groupBy("id", "age")));
        // 子查询
        list = userInfoDao.queryList(new Q()
                .filterSubQuery("id", Filter.Operator.IN,
                        SubQueryParam.build(new Query("id").gte("id", 6))));
        System.out.println(list);
    }

}
