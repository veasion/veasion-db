package cn.veasion.db.jdbc;

import cn.veasion.db.base.JdbcTypeEnum;
import cn.veasion.db.utils.ISort;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * ConnectionProvider
 *
 * @author luozhuowei
 * @date 2021/12/5
 */
public interface DataSourceProvider extends ISort {

    /**
     * 获取数据源
     *
     * @param entityDao    当前dao对象
     * @param jdbcTypeEnum 当前方法操作类型
     * @return 数据源
     */
    DataSource getDataSource(EntityDao<?, ?> entityDao, JdbcTypeEnum jdbcTypeEnum);

    /**
     * 根据数据源获取连接
     *
     * @param dataSource 数据源
     * @return sql连接
     */
    Connection getConnection(DataSource dataSource) throws SQLException;

    /**
     * 释放连接
     *
     * @param dataSource 数据源
     * @param connection 连接
     */
    void releaseConnection(DataSource dataSource, Connection connection);

}
