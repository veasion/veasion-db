package cn.veasion.db.jdbc;

import cn.veasion.db.DbException;
import cn.veasion.db.utils.FieldUtils;
import cn.veasion.db.utils.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JdbcDao
 *
 * @author luozhuowei
 * @date 2021/12/3
 */
public class JdbcDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcDao.class);

    /**
     * 执行增删改
     *
     * @return 返回影响条数
     */
    public static int executeUpdate(Connection connection, String sql, Object... params) {
        int count;
        PreparedStatement ps = null;
        try {
            ps = prepareStatement(connection, sql, params);
            count = ps.executeUpdate();
            LOGGER.info("<==    Updates: {}", count);
        } catch (SQLException e) {
            throw new DbException("更新异常", e);
        } finally {
            closeAll(ps, null);
        }
        return count;
    }

    /**
     * 执行新增，返回自增长id
     *
     * @return 返回自增长id
     */
    public static Object[] executeInsert(Connection connection, String sql, Object... params) {
        PreparedStatement ps = null;
        ResultSet result = null;
        List<Object> keys = new ArrayList<>();
        try {
            ps = prepareStatement(connection, Statement.RETURN_GENERATED_KEYS, sql, params);
            int count = ps.executeUpdate();
            if (count > 0) {
                result = ps.getGeneratedKeys();
                while (result != null && result.next()) {
                    keys.add(result.getObject(1));
                }
            }
            LOGGER.info("<==    Updates: {}", count);
        } catch (SQLException e) {
            throw new DbException("新增异常", e);
        } finally {
            closeAll(ps, result);
        }
        return keys.toArray();
    }

    /**
     * 列表查询
     *
     * @return 返回列表数据
     */
    public static List<Map<String, Object>> listForMap(Connection connection, String sql, Object... params) {
        return listForMap(connection, true, sql, params);
    }

    /**
     * 列表查询
     *
     * @return 返回列表数据
     */
    public static List<Map<String, Object>> listForMap(Connection connection, boolean mapUnderscoreToCamelCase, String sql, Object... params) {
        ResultSet rs = null;
        PreparedStatement ps = null;
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            String columnName;
            Map<String, Object> map;
            ps = prepareStatement(connection, sql, params);
            rs = ps.executeQuery();
            ResultSetMetaData data;
            while (rs.next()) {
                map = new HashMap<>();
                data = rs.getMetaData();
                int count = data.getColumnCount();
                for (int i = 1; i <= count; i++) {
                    columnName = data.getColumnLabel(i);
                    if (mapUnderscoreToCamelCase) {
                        columnName = FieldUtils.lineToHump(columnName);
                    }
                    map.put(columnName, rs.getObject(i));
                }
                list.add(map);
            }
            LOGGER.info("<==      Total: {}", list.size());
        } catch (SQLException e) {
            throw new DbException("查询异常", e);
        } finally {
            closeAll(ps, rs);
        }
        return list;
    }

    /**
     * 查询单个
     */
    public static Map<String, Object> queryForMap(Connection connection, String sql, Object... params) {
        return queryForMap(connection, true, sql, params);
    }

    /**
     * 查询单个
     */
    public static Map<String, Object> queryForMap(Connection connection, boolean mapUnderscoreToCamelCase, String sql, Object... params) {
        List<Map<String, Object>> list = listForMap(connection, mapUnderscoreToCamelCase, sql, params);
        if (list.isEmpty()) {
            return null;
        } else if (list.size() > 1) {
            throw new DbException("查询有多个结果：" + sql);
        }
        return list.get(0);
    }

    /**
     * 查询单个
     */
    public static <T> T queryForType(Connection connection, Class<T> clazz, String sql, Object... params) {
        List<T> list = listForType(connection, clazz, sql, params);
        if (list.isEmpty()) {
            return null;
        } else if (list.size() > 1) {
            throw new DbException("查询有多个结果(" + list.size() + ")：" + sql);
        }
        return list.get(0);
    }

    /**
     * 列表查询
     *
     * @return 返回列表数据
     */
    public static <T> List<T> listForType(Connection connection, Class<T> clazz, String sql, Object... params) {
        return listForType(connection, clazz, null, sql, params);
    }

    /**
     * 列表查询
     *
     * @return 返回列表数据
     */
    public static <T> List<T> listForType(Connection connection, Class<T> clazz, FieldAssignmentHandler handler, String sql, Object... params) {
        ResultSet rs = null;
        PreparedStatement ps = null;
        List<T> list = new ArrayList<>();
        Map<String, String> fieldColumnMap = FieldUtils.fieldColumns(clazz);
        Map<String, String> columnFieldMap = fieldColumnMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey, (v1, v2) -> v1));
        try {
            String columnName;
            ps = prepareStatement(connection, sql, params);
            rs = ps.executeQuery();
            ResultSetMetaData data;
            while (rs.next()) {
                data = rs.getMetaData();
                int count = data.getColumnCount();
                if (count == 1) {
                    Object object = rs.getObject(1);
                    if ((object != null && clazz.isAssignableFrom(object.getClass())) || TypeUtils.isSimpleClass(clazz)) {
                        list.add(TypeUtils.convert(object, clazz));
                        continue;
                    }
                }
                T obj = TypeUtils.newInstance(clazz);
                for (int i = 1; i <= count; i++) {
                    columnName = data.getColumnLabel(i);
                    String fieldName = null;
                    if (fieldColumnMap.containsKey(columnName)) {
                        fieldName = columnName;
                    } else if (columnFieldMap.containsKey(columnName)) {
                        fieldName = columnFieldMap.get(columnName);
                    }
                    if (fieldName != null) {
                        if (handler != null) {
                            handler.handle(obj, fieldName, rs.getObject(i));
                        } else {
                            FieldUtils.setValue(obj, fieldName, rs.getObject(i), true);
                        }
                    }
                }
                list.add(obj);
            }
            LOGGER.info("<==      Total: {}", list.size());
        } catch (SQLException e) {
            throw new DbException("查询异常", e);
        } catch (RuntimeException e) {
            LOGGER.error("查询处理数据异常", e);
            throw e;
        } finally {
            closeAll(ps, rs);
        }
        return list;
    }

    /**
     * 获取单个值
     *
     * @return 返回结果
     */
    public static Object queryOnly(Connection connection, String sql, Object... params) {
        Object value = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            ps = prepareStatement(connection, sql, params);
            rs = ps.executeQuery();
            if (rs.next()) {
                value = rs.getObject(1);
            }
            int total = 1;
            while (rs.next()) {
                total++;
            }
            LOGGER.info("<==      Total: {}", total);
        } catch (SQLException e) {
            throw new DbException("查询异常", e);
        } finally {
            closeAll(ps, rs);
        }
        return value;
    }

    private static PreparedStatement prepareStatement(Connection connection, String sql, Object... params) throws SQLException {
        return prepareStatement(connection, Statement.NO_GENERATED_KEYS, sql, params);
    }

    private static PreparedStatement prepareStatement(Connection connection, int autoGeneratedKeys, String sql, Object... params) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sql, autoGeneratedKeys);
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("==>  Preparing: {}", sql);
            LOGGER.info("==> Parameters: {}", paramToString(params));
        }
        return ps;
    }

    private static String paramToString(Object[] params) {
        if (params == null || params.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (Object param : params) {
            sb.append(param);
            if (param != null) {
                sb.append("(").append(param.getClass().getSimpleName()).append(")");
            }
            sb.append(", ");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2);
        }
        return sb.toString();
    }

    private static void closeAll(Statement ps, ResultSet rs) {
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FunctionalInterface
    public interface FieldAssignmentHandler {

        /**
         * 字段赋值处理
         *
         * @param object    对象
         * @param fieldName 字段名
         * @param value     值
         */
        void handle(Object object, String fieldName, Object value);

    }

}
