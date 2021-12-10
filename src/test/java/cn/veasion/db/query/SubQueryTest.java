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
        List<UserInfoPO> list = userInfoDao.queryList(new SubQuery(new Q().gt("id", 6))
                .selectAll().select("userNike", "nike").like("userNike", "伟神"));
        System.out.println(list);

        // 模拟 oracle 分页
        int page = 1, size = 10;
        EQ query = new EQ(UserInfoPO.class);
        list = userInfoDao.queryList(
                new SubQuery(
                        new SubQuery(query)
                                .selectAll()
                                .realSelect("ROWNUM", "row")
                                .realFilter(Filter.lte("ROWNUM", page * size))
                ).gt("row", page * size - size));
        System.out.println(list);

    }

}
