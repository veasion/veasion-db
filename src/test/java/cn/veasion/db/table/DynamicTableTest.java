package cn.veasion.db.table;

import cn.veasion.db.AbstractFilter;
import cn.veasion.db.BaseTest;
import cn.veasion.db.DbException;
import cn.veasion.db.base.Filter;
import cn.veasion.db.base.Operator;
import cn.veasion.db.jdbc.DataSourceProvider;
import cn.veasion.db.jdbc.DefaultDynamicTableExt;
import cn.veasion.db.jdbc.JdbcDao;
import cn.veasion.db.model.po.ClassesPO;
import cn.veasion.db.model.po.StudentPO;
import cn.veasion.db.model.vo.StudentVO;
import cn.veasion.db.query.EQ;
import cn.veasion.db.query.Q;
import cn.veasion.db.update.BatchEntityInsert;
import cn.veasion.db.update.EntityInsert;
import cn.veasion.db.update.U;
import cn.veasion.db.utils.ServiceLoaderUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DynamicTableTest
 *
 * @author luozhuowei
 * @date 2022/1/30
 */
public class DynamicTableTest extends BaseTest {

    public static void main(String[] args) throws Exception {
        // 测试根据 班级ID % 2 动态分表
        // 注意：分表后设计到当前学生表的所有增删改查语句都必须带有classId，不支持 in classId，如果 in 需要动态用 union all 写路由规则
        // 批量新增不支持，需要按classId维度group成集合在进行批量操作

        // 动态创建表，历史数据迁移
        createAndSplitTable();

        // 路由规则
        DefaultDynamicTableExt.addGlobalDynamicTableExt(StudentPO.class, new DefaultDynamicTableExt.AbstractDynamicTableExt() {
            @Override
            protected String handleAdd(String tableName, Class<?> entityClazz, EntityInsert entityInsert) {
                Number classId = (Number) entityInsert.getFieldValueMap().get("classId");
                if (classId == null) {
                    throw new DbException("classId不能为空");
                }
                return tableName + "_" + (classId.longValue() % 2);
            }

            @Override
            protected String handleBatchAdd(String tableName, Class<?> entityClazz, BatchEntityInsert batchEntityInsert, boolean insertSelect) {
                if (!insertSelect) {
                    List<Object> classIds = batchEntityInsert.getFieldValueMapList().stream().map(map -> map.get("classId")).distinct().collect(Collectors.toList());
                    if (classIds.size() > 1) {
                        throw new DbException("批量新增包含多个classId，不支持分表，请拆分");
                    }
                    if (classIds.get(0) == null) {
                        throw new DbException("classId不能为空");
                    }
                    Number classId = (Number) classIds.get(0);
                    return tableName + "_" + (classId.longValue() % 2);
                } else {
                    throw new DbException("insert select不支持分表");
                }
            }

            @Override
            protected String handleFilter(String tableName, Class<?> entityClazz, AbstractFilter<?> filter) {
                Filter eqClassId = filter.getFilters("classId").stream().filter(f -> Operator.EQ.equals(f.getOperator()) && f.getValue() != null).findFirst().orElse(null);
                if (eqClassId == null) {
                    throw new DbException("条件 classId (eq)不能为空");
                }
                Number classId = (Number) eqClassId.getValue();
                return tableName + "_" + (classId.longValue() % 2);
            }
        });

        // 新增学生
        StudentPO student1 = getStudent();
        student1.setClassId(1L);
        studentDao.add(student1);
        StudentPO student2 = getStudent();
        student2.setClassId(2L);
        studentDao.add(student2);

        // 查询学生及所在班级
        // select s.*, c.class_name from t_student s join t_classes c on s.class_id = c.id where s.class_id = 1
        EQ student = new EQ(StudentPO.class, "s");
        student.join(new EQ(ClassesPO.class, "c").select("className")).on("classId", "id");
        student.selectAll();
        student.eq("classId", 1);
        println(studentDao.queryList(student, StudentVO.class));

        // 更新学生年龄
        // update t_student set age = 20 where id = 2 and classId = 1
        println(studentDao.update(new U("age", 20).eq("id", 2).eq("classId", 1)));

    }

    private static void createAndSplitTable() throws Exception {
        String createSQL = "create table if not exists t_student_%d like t_student";
        String insertSQL = "insert into t_student_%d select * from t_student where class_id = ?";

        List<Long> classIds = studentDao.queryList(new Q("classId").isNotNull("classId").groupBy("classId"), Long.class);
        DataSourceProvider dataSourceProvider = ServiceLoaderUtils.dataSourceProvider();
        DataSource dataSource = dataSourceProvider.getDataSource(null, null);
        Connection connection = dataSource.getConnection();
        try {
            for (Long classId : classIds) {
                JdbcDao.executeUpdate(connection, String.format(createSQL, classId % 2));
                Long count = DefaultDynamicTableExt.withDynamicTableExt((tableName, clazz, filter, source) -> tableName + "_" + classId % 2,
                        () -> studentDao.queryForType(new Q().selectExpression("count(1)", "count"), Long.class));
                if (count == 0) {
                    JdbcDao.executeInsert(connection, String.format(insertSQL, classId % 2), classId);
                }
                // String sql = "create table if not exists t_student_%d as select * from t_student where class_id = ?";
                // JdbcDao.executeInsert(connection, String.format(sql, classId % 2), classId);
            }
        } finally {
            dataSourceProvider.releaseConnection(dataSource, connection);
        }
    }

    private static StudentPO getStudent() {
        long s = System.currentTimeMillis();
        StudentPO studentPO = new StudentPO();
        studentPO.setName("学生_" + s);
        studentPO.setSno("s" + s);
        studentPO.setAge(18);
        studentPO.setSex(1);
        studentPO.setClassId(1L);
        studentPO.setIsDeleted(0L);
        studentPO.setVersion(0);
        studentPO.setCreateTime(new Date());
        studentPO.setUpdateTime(new Date());
        return studentPO;
    }

}
