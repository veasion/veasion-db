package cn.veasion.db.dao;

import cn.veasion.db.TestUtils;
import cn.veasion.db.jdbc.JdbcDao;
import cn.veasion.db.jdbc.JdbcDaoProvider;

/**
 * JdbcDaoProviderTest
 *
 * @author luozhuowei
 * @date 2021/12/5
 */
public class JdbcDaoProviderTest implements JdbcDaoProvider {

    JdbcDao jdbcDao = new JdbcDao(TestUtils.getDataSource());

    @Override
    public JdbcDao getJdbcDao() {
        return jdbcDao;
    }

}
