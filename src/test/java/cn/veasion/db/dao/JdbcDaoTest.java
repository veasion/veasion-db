package cn.veasion.db.dao;

import cn.veasion.db.DataSourceUtils;
import cn.veasion.db.base.UserInfoPO;
import cn.veasion.db.jdbc.JdbcDao;

import javax.sql.DataSource;
import java.sql.Connection;
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
public class JdbcDaoTest {

    public static void main(String[] args) throws Exception {
        DataSource dataSource = DataSourceUtils.getDataSource();
        Connection connection = dataSource.getConnection();

        // 批量新增
        Object[] ids = JdbcDao.executeInsert(connection, "insert into t_user_info(`username`, `user_nike`, `age`, `test_column`, `is_deleted`, `create_time`) values " +
                        "(?, ?, ?, ?, ? , ?),(?, ?, ?, ?, ? , ?)",
                "test1", "测试1", 18, "xxx", 0, new Date(),
                "test2", "测试2", 20, "sss", 0, new Date());
        System.out.println(Arrays.toString(ids));

        // 查询map
        String sql = "select id as isDeleted, username, user_nike from t_user_info where id > ? limit 10";
        List<Map<String, Object>> list = JdbcDao.listForMap(connection, sql, 0);
        System.out.println(list);

        // 查询list
        sql = "select user_nike from t_user_info where id > ? limit 10";
        List<UserInfoPO> userList = JdbcDao.listForType(connection, UserInfoPO.class, sql, 0);
        System.out.println(userList);

        // 查询list
        List<String> result = JdbcDao.listForType(connection, String.class, sql, 0);
        System.out.println(result);

        connection.close();
    }
}
