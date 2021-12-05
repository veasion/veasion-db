package cn.veasion.db.dao;

import cn.veasion.db.TestUtils;
import cn.veasion.db.base.UserInfoPO;
import cn.veasion.db.jdbc.JdbcDao;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * CommonJdbcTest
 *
 * @author luozhuowei
 * @date 2021/12/3
 */
public class CommonJdbcTest {

    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://10.10.0.44:3306/log?useUnicode=true&characterEncoding=utf-8&autoReconnect=true";
        DataSource dataSource = TestUtils.getDataSource(url, "kaifa_admin", "msh#gd69Rp");
        JdbcDao jdbcDao = new JdbcDao(dataSource);
        Object[] ids = jdbcDao.executeInsert("insert into t_user_info(`username`, `user_nike`, `age`, `test_column`, `is_deleted`, `create_time`) values " +
                        "(?, ?, ?, ?, ? , ?),(?, ?, ?, ?, ? , ?)",
                "test1", "测试1", 18, "xxx", 0, new Date(),
                "test2", "测试2", 20, "sss", 0, new Date());
        System.out.println(Arrays.toString(ids));

        String sql = "select id as isDeleted, username, user_nike from t_user_info where id > ? limit 10";
        List<Map<String, Object>> list = jdbcDao.listForMap(sql, 0);
        System.out.println(list);
        System.out.println();
        sql = "select user_nike from t_user_info where id > ? limit 10";
        List<UserInfoPO> userList = jdbcDao.listForEntity(UserInfoPO.class, sql, 0);
        System.out.println(userList);
        System.out.println();
        List<String> result = jdbcDao.listForEntity(String.class, sql, 0);
        System.out.println(result);
        System.out.println();
        dataSource.getConnection().close();
    }
}
