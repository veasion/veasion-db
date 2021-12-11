package cn.veasion.db.query;

import cn.veasion.db.base.Filter;
import cn.veasion.db.base.UserInfoPO;
import cn.veasion.db.dao.UserInfoDao;

import java.util.List;

/**
 * SubQueryTest
 *
 * @author luozhuowei
 * @date 2021/12/10
 */
public class SubQueryTest {

    public static void main(String[] args) {
        UserInfoDao userInfoDao = new UserInfoDao();

        // 子查询
        List<UserInfoPO> list = userInfoDao.queryList(new SubQuery(new Q().gt("id", 6), "t")
                .selectAll().select("userNike", "nike").like("userNike", "伟神"));
        System.out.println(list);

        // 子查询关联子查询
        SubQuery sub1 = new SubQuery(new Q("id", "userNike", "test").gt("id", 0), "t1");
        SubQuery sub2 = new SubQuery(new Q("id", "username", "test").lt("id", 10), "t2");
        sub1.join(sub2).on("userNike", "username");
        sub1.like("t1.test", "test");
        list = userInfoDao.queryList(sub1);
        System.out.println(list);

        // 模拟 oracle 分页
        int page = 1, size = 10;
        EQ query = new EQ(UserInfoPO.class);
        list = userInfoDao.queryList(
                new SubQuery(
                        new SubQuery(query, "t")
                                .selectAll()
                                .realSelect("ROWNUM", "row")
                                .realFilter(Filter.lte("ROWNUM", page * size)),
                        "t"
                ).gt("row", page * size - size));
        System.out.println(list);

    }

}
