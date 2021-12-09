package cn.veasion.db;

import cn.veasion.db.base.JdbcTypeEnum;
import cn.veasion.db.jdbc.EntityDao;
import cn.veasion.db.jdbc.DataSourceProvider;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * JdbcDaoProviderTest
 *
 * @author luozhuowei
 * @date 2021/12/5
 */
public class DataSourceProviderTest implements DataSourceProvider {

    ThreadLocal<Map<DataSource, Connection>> threadLocal = ThreadLocal.withInitial(HashMap::new);

    @Override
    public DataSource getDataSource(EntityDao<?, ?> entityDao, JdbcTypeEnum jdbcTypeEnum) {
        return DataSourceUtils.getDataSource();
    }

    @Override
    public Connection getConnection(DataSource dataSource) throws SQLException {
        Connection connection = threadLocal.get().get(dataSource);
        if (connection == null) {
            connection = dataSource.getConnection();
            threadLocal.get().put(dataSource, connection);
        }
        return connection;
    }

}
