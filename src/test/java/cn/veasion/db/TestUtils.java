package cn.veasion.db;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * TestUtils
 *
 * @author luozhuowei
 * @date 2021/12/3
 */
public class TestUtils {

    public static DataSource getDataSource() {
        try {
            String url = "jdbc:mysql://10.10.0.44:3306/log?useUnicode=true&characterEncoding=utf-8&autoReconnect=true";
            return getDataSource(url, "kaifa_admin", "msh#gd69Rp");
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    public static DataSource getDataSource(String url, String user, String password) throws SQLException {
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
