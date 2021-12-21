package cn.veasion.db;

import cn.veasion.db.base.JdbcTypeEnum;
import cn.veasion.db.jdbc.EntityDao;
import cn.veasion.db.jdbc.DataSourceProvider;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * DefaultDataSourceProvider
 *
 * @author luozhuowei
 * @date 2021/12/5
 */
public class DefaultDataSourceProvider implements DataSourceProvider {

    @Override
    public DataSource getDataSource(EntityDao<?, ?> entityDao, JdbcTypeEnum jdbcTypeEnum) {
        // 根据 jdbcTypeEnum 判断读写数据库获取数据源
        // 集成 spring 可用通过 SpringUtils.getBean(DataSource.class) 获取数据源
        try {
            return getDataSource(BaseTest.jdbcUrl, BaseTest.user, BaseTest.password);
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    @Override
    public Connection getConnection(DataSource dataSource) throws SQLException {
        // 集成 spring 可用通过 org.springframework.jdbc.datasource.DataSourceUtils.getConnection(dataSource) 获取连接
        return dataSource.getConnection();
    }

    @Override
    public void releaseConnection(DataSource dataSource, Connection connection) {
        // 集成 spring 可用通过 org.springframework.jdbc.datasource.DataSourceUtils.releaseConnection(connection, dataSource) 释放连接
        try {
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static DataSource getDataSource(String url, String user, String password) throws SQLException {
        return new DataSource() {
            private Connection connection;

            @Override
            public Connection getConnection() throws SQLException {
                if (connection != null) {
                    return connection;
                }
                return connection = DriverManager.getConnection(url, user, password);
            }

            @Override
            public Connection getConnection(String username, String password) throws SQLException {
                if (connection != null) {
                    return connection;
                }
                return connection = DriverManager.getConnection(url, username, password);
            }

            @Override
            public <T> T unwrap(Class<T> iface) throws SQLException {
                return null;
            }

            @Override
            public boolean isWrapperFor(Class<?> iface) throws SQLException {
                return false;
            }

            @Override
            public PrintWriter getLogWriter() throws SQLException {
                return null;
            }

            @Override
            public void setLogWriter(PrintWriter out) throws SQLException {

            }

            @Override
            public void setLoginTimeout(int seconds) throws SQLException {

            }

            @Override
            public int getLoginTimeout() throws SQLException {
                return 0;
            }

            @Override
            public Logger getParentLogger() throws SQLFeatureNotSupportedException {
                return null;
            }
        };
    }
}
