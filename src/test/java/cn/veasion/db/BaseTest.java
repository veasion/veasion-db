package cn.veasion.db;

import cn.veasion.db.base.Expression;
import cn.veasion.db.dao.AreaDao;
import cn.veasion.db.dao.ClassesDao;
import cn.veasion.db.dao.CourseDao;
import cn.veasion.db.dao.ScoreDao;
import cn.veasion.db.dao.StudentDao;
import cn.veasion.db.dao.TeacherDao;
import cn.veasion.db.interceptor.InterceptorUtils;
import cn.veasion.db.interceptor.LogicDeleteInterceptor;
import cn.veasion.db.utils.FieldUtils;
import cn.veasion.db.utils.TypeUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * BaseTest
 *
 * @author luozhuowei
 * @date 2021/12/12
 */
public class BaseTest {

    static final String jdbcUrl = "jdbc:mysql://127.0.0.1:3306/veasion_db?useUnicode=true&characterEncoding=utf-8&autoReconnect=true";
    static final String user = "root";
    static final String password = "123456";

    static {
        // 添加一个拦截器，默认查询没有逻辑删除的数据（也可以通过 spi 添加）
        InterceptorUtils.addInterceptor(
                new LogicDeleteInterceptor("isDeleted", 0, Expression.update("${id}"))
        );
    }

    protected static final StudentDao studentDao = new StudentDao();
    protected static final TeacherDao teacherDao = new TeacherDao();
    protected static final ClassesDao classesDao = new ClassesDao();
    protected static final CourseDao courseDao = new CourseDao();
    protected static final ScoreDao scoreDao = new ScoreDao();
    protected static final AreaDao areaDao = new AreaDao();

    private static Set<String> printSkipField = new HashSet<String>() {{
        add("version");
        add("isDeleted");
        add("createTime");
        add("updateTime");
    }};

    protected static void println(Object object) {
        StringBuilder sb = new StringBuilder();
        append(sb, object);
        System.out.println(sb);
    }

    @SuppressWarnings("unchecked")
    protected static void append(StringBuilder sb, Object object) {
        if (object == null) {
            sb.append("null");
            return;
        }
        if (object instanceof Collection || object instanceof Object[]) {
            sb.append("[");
            int len = sb.length();
            if (object instanceof Collection) {
                for (Object o : ((Collection<?>) object)) {
                    sb.append("\r\n  ");
                    append(sb, o);
                    sb.append(", ");
                }
            } else {
                for (Object o : (Object[]) object) {
                    sb.append("\r\n ");
                    append(sb, o);
                    sb.append(", ");
                }
            }
            if (sb.length() > len) {
                sb.setLength(sb.length() - 2);
                sb.append("\r\n");
            }
            sb.append("]");
        } else if (object instanceof Map) {
            sb.append("{");
            int len = sb.length();
            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) object).entrySet()) {
                if (printSkipField.contains(String.valueOf(entry.getKey()))) {
                    continue;
                }
                sb.append("\"").append(entry.getKey()).append("\": ");
                append(sb, entry.getValue());
                sb.append(", ");
            }
            if (sb.length() > len) {
                sb.setLength(sb.length() - 2);
            }
            sb.append("}");
        } else if (TypeUtils.isSimpleClass(object.getClass()) || object instanceof Number) {
            if (Number.class.isAssignableFrom(object.getClass())) {
                sb.append(object);
            } else {
                sb.append("\"").append(object).append("\"");
            }
        } else {
            Map<String, Field> fields = FieldUtils.fields(object.getClass());
            sb.append("{");
            int len = sb.length();
            for (String field : fields.keySet()) {
                if (printSkipField.contains(field)) {
                    continue;
                }
                sb.append("\"").append(field).append("\": ");
                append(sb, FieldUtils.getValue(object, field, false));
                sb.append(", ");
            }
            if (sb.length() > len) {
                sb.setLength(sb.length() - 2);
            }
            sb.append("}");
        }
    }

}
