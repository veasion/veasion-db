# veasion-db

veasion-db 是一个轻量级持久层db框架，除slf4j-api外不依赖任何第三方jar，该框架提供丰富灵活的数据库操作，
单元测试 query/update 目录下有大量示例及demo。

框架基本支持sql能实现的任意查询或更新，如关联查询、子查询、关联更新、insert select、不同数据库分页扩展等。

框架支持自定义拦截器，内置逻辑删除拦截器，可通过SPI或调用InterceptorUtils.addInterceptor方法加入扩展。
## maven 依赖
添加 veasion-db 依赖
```xml
<dependency>
    <groupId>cn.veasion</groupId>
    <artifactId>veasion-db</artifactId>
    <version>1.1.7</version>
</dependency>
```
支持sql解析生成veasion-db代码
```
String sql = "select * from t_student where id = 1";
String code = SQLParseUtils.parseSQLConvert(sql);
// 直接把SQL转换成对应的代码，示例参考单元测试 SqlDbConvertTest

// 该功能为扩展功能需要加入第三方依赖
<dependency>
    <groupId>com.github.jsqlparser</groupId>
    <artifactId>jsqlparser</artifactId>
    <version>1.2</version>
</dependency>

```
## 使用方式介绍
这里以 student 表举例
```java
// 自定义 dao 继承 JdbcEntityDao<T, ID> 类
public class StudentDao extends JdbcEntityDao<StudentPO, Long> {

}

// 数据源通过SPI提供，实现 cn.veasion.db.jdbc.DataSourceProvider 接口即可

// 使用
// StudentDao studentDao = new StudentDao();

// StudentPO student = studentDao.getById(1L);
```
### 简单查询示例
```java
public class SimpleQueryTest extends BaseTest {

    public static void main(String[] args) {
        // 查询全部学生
        // select * from t_student
        println(studentDao.queryList(new Q()));

        // 根据id查询学生
        // select * from t_student where id = 1
        println(studentDao.getById(1L));

        // 查询学号为s001的学生名称
        // select name from t_student where sno = 's001'
        println(studentDao.queryForType(new Q("name").eq("sno", "s001"), String.class));

        // 查询所有班级名称
        // select class_name from t_classes
        println(classesDao.queryList(new Q("className"), String.class));

        // 查询年龄满18的学生
        // select * from t_student where age >= 18
        println(studentDao.queryList(new Q().gte("age", 18)));

        // 查询年龄在16-18之间的男学生
        // select * from t_student where sex = 1 and age between 16 and 18
        println(studentDao.queryList(new Q().eq("sex", 1).between("age", 16, 18)));

        // 查询熊姓学生
        // select * from t_student where name like '熊%'
        println(studentDao.queryList(new Q().likeRight("name", "熊")));

        // 查询特殊备注的学生
        // select sno, name, `desc` from t_student where `desc` is not null
        println(studentDao.queryList(new Q("sno", "name", "desc").isNotNull("desc")));

        // 查询年龄最大学生
        // select * from t_student order by age desc limit 1
        println(studentDao.query(new Q().desc("age").page(1, 1)));

        // 查询学生人数和平均年龄
        // select count(1) as count, avg(age) as avgAge from t_student
        println(studentDao.queryForMap(new Q()
                .selectExpression("count(1)", "count")
                .selectExpression("avg(${age})", "avgAge")
        ));

        // 统计学生性别人数小于5的性别及人数
        // select sex, count(id) as count from t_student group by sex having count < 5
        println(studentDao.queryForMap(new Q()
                .select("sex")
                .selectExpression("count(id)", "count")
                .groupBy("sex")
                .having(Filter.lt("count", 5))
        ));

        // 查询小于平均年龄的女学生
        // select * from t_student where sex = 2 and age < (select avg(age) from t_student)
        println(studentDao.queryList(new Q()
                .eq("sex", 2)
                .filterSubQuery("age", Operator.LT, SubQueryParam.build(
                        new Q().selectExpression("avg(age)", null)
                ))
        ));

        // 查询姓名里存在“熊”或者“小”的男学生
        // select * from t_student where sex = 1 and (name like '%熊%' or name like '%小%')
        println(studentDao.queryList(new Q()
                .eq("sex", 1)
                .andBracket(Filter.like("name", "熊"), Filter.or(), Filter.like("name", "小"))
        ));

        // 分页查询学生（第二页）
        // select * from t_student limit 10, 10
        println(studentDao.queryPage(new Q().page(2, 10)));

        // 查询空表
        // select 1 from dual
        println(studentDao.queryForType(new EQ(Dual.class).select("1"), Integer.class));
    }

}
```
### 关联查询示例
```java
public class JoinQueryTest extends BaseTest {

    public static void main(String[] args) {
        // 查询学生及所在班级
        // select s.*, c.class_name from t_student s join t_classes c on s.class_id = c.id
        EQ student = new EQ(StudentPO.class, "s");
        student.join(new EQ(ClassesPO.class, "c").select("className")).on("classId", "id");
        student.selectAll();
        println(studentDao.queryList(student, StudentVO.class));

        // 查询平均分及格的所有课程
        // select c.*, avg(s.score) as avgScore from t_course c join t_score s on c.id = s.course_id having avgScore >= 60
        EQ c = new EQ(CoursePO.class, "c");
        c.join(new EQ(ScorePO.class, "s")).on("id", "courseId");
        c.selectAll().selectExpression("avg(s.score)", "avgScore");
        c.having(Filter.gte("avgScore", 60));
        println(courseDao.queryList(c, CourseScoreVO.class));

        // 查询 “初一一班” 班主任及所有任课老师
        // select t.* from t_classes c
        // inner join t_teacher t on c.master_tno = t.tno
        // where c.class_name = '初一一班'
        // union
        // select distinct t.* from t_classes c
        // inner join t_course course on c.id = course.class_id
        // inner join t_teacher t on course.tno = t.tno
        // where c.class_name = '初一一班'
        EQ classes1 = new EQ(ClassesPO.class, "c");
        EQ teacher1 = new EQ(TeacherPO.class, "t");
        classes1.join(teacher1).on("masterTno", "tno");
        classes1.eq("className", "初一一班");
        teacher1.selectAll();

        EQ classes2 = new EQ(ClassesPO.class, "c");
        EQ course = new EQ(CoursePO.class, "course");
        EQ teacher2 = new EQ(TeacherPO.class, "t");
        classes2.join(course).on("id", "classId");
        course.join(teacher2).on("tno", "tno");
        classes2.eq("className", "初一一班");
        teacher2.selectAll();

        classes1.union(classes2.distinct());
        println(classesDao.queryList(classes1, TeacherPO.class));

        // 查询学生信息及每门课程对应的分数和所在班级、课程对应的任课老师，学生分数打上标签：及格/不及格
        // select s.sno, s.name, c.class_name, course.course_name, t.name as courseTeacher, score.score, if(score.score>=60, '及格', '不及格') as scoreLabel
        // from t_student s
        // join t_classes c on s.class_id = c.id
        // join t_score score on s.sno = score.sno
        // join t_course course on score.course_id = course.id
        // join t_teacher t on course.tno = t.tno
        EQ _student = new EQ(StudentPO.class, "s");
        _student.join(new EQ(ClassesPO.class, "c")).on("classId", "id");
        EQ _score = new EQ(ScorePO.class, "score");
        _student.join(_score).on("sno", "sno");
        EQ _course = new EQ(CoursePO.class, "course");
        _score.join(_course).on("courseId", "id");
        _course.join(new EQ(TeacherPO.class, "t")).on("tno", "tno");

        _student.selects("sno", "name", "c.className", "course.courseName", "score.score");
        _student.select("t.name", "courseTeacher");
        _student.selectExpression("if(score.score>=60, '及格', '不及格')", "scoreLabel");

        println(studentDao.queryList(_student, StudentCourseScoreVO.class));
    }

}
```
### 子查询示例
```java
public class SubQueryTest extends BaseTest {

    public static void main(String[] args) {
        // 简单子查询
        // select count(id) from (select * from t_student) t
        println(studentDao.queryForType(new SubQuery(new Q(), "t").selectExpression("count(id)", "count"), Integer.class));

        // 通过子查询关联查询小于平均年龄的男同学
        // select s.* from t_student s join (select avg(age) as age from t_student) t on s.age < t.age where s.sex = 2
        EQ student = new EQ(StudentPO.class, "s");
        student.join(
                new SubQuery(new Q().selectExpression("avg(age)", "age"), "t")
        ).on(Filter.expression("s.age", Operator.LT, "t.age"));
        student.selectAll().eq("sex", 2);
        println(studentDao.queryList(student));

        // 通过子查询查询 “初一一班” 班主任及所有任课老师
        // select * from t_teacher where
        // tno in (select master_tno from t_classes where class_name = '初一一班')
        // or
        // tno in (select course.tno from t_classes c inner join t_course course on c.id = course.class_id where c.class_name = '初一一班')
        EntityQuery subQuery1 = new EQ(ClassesPO.class).select("masterTno").eq("className", "初一一班");
        EntityQuery subQuery2 = new EQ(ClassesPO.class, "c").eq("className", "初一一班");
        subQuery2.join(new EQ(CoursePO.class, "course").select("tno")).on("id", "classId");
        println(teacherDao.queryList(new Q()
                .filterSubQuery("tno", Operator.IN, SubQueryParam.build(subQuery1))
                .addFilters(Filter.or())
                .filterSubQuery("tno", Operator.IN, SubQueryParam.build(subQuery2))
        ));

        // 通过子查询来查询学生班级名称
        // select s.*, (select class_name from t_classes where id = s.class_id) as className from t_student
        println(studentDao.queryList(new EQ(StudentPO.class, "s")
                .selectAll()
                .selectSubQuery(SubQueryParam.build(
                        new EQ(ClassesPO.class)
                                .select("className")
                                .filterExpression("id", Operator.EQ, "${s.classId}")
                )), StudentVO.class
        ));

        /*
        // 模拟 oracle 分页
        // 等价于 studentDao.queryList(new Q().page(new OraclePage(1, 10)))
        // select t.* from (select t.*, ROWNUM as row from (select * from t_student) t where ROWNUM <= 10) t where t.row > 0
        int page = 1, size = 10;
        println(studentDao.queryList(
                new SubQuery(
                        new SubQuery(new Q(), "t")
                                .selectAll()
                                .realSelect("ROWNUM", "row")
                                .realFilter(Filter.lte("ROWNUM", page * size)),
                        "t"
                ).gt("row", page * size - size)
        ));
        */
    }

}
```

### 新增示例
```java
public class InsertTest extends BaseTest {

    public static void main(String[] args) {

        // 新增一个学生
        // insert into t_student(...) values (...)
        println(studentDao.add(getStudent()));

        // 批量新增学生
        // insert into t_student(...) values (...), (...), (...)
        println(studentDao.batchAdd(Arrays.asList(getStudent(), getStudent(), getStudent())));

        // insert select 新增学生
        // insert into t_student(...) select ... from student order by id desc limit 1
        Long[] ids = studentDao.batchAdd(new BatchEntityInsert(
                new EQ(StudentPO.class)
                        .selects("age", "sex", "version", "isDeleted", "createTime")
                        .selectExpression("concat('copy_', name)", "name")
                        .selectExpression("concat('copy_', sno)", "sno")
                        .desc("id").page(1, 1)
        ));
        println(ids);
        println(studentDao.queryList(new Q().in("id", ids)));

        // 防学号重复新增学生
        // insert into t_student(...) select concat('copy_', sno), ... from t_student
        // where sno = 's001' and not exists (select 1 from t_student where sno = 'copy_s001')
        println(studentDao.batchAdd(new BatchEntityInsert(
                new EQ(StudentPO.class)
                        .selects("age", "sex", "version", "isDeleted", "createTime")
                        .selectExpression("concat('copy_', name)", "name")
                        .selectExpression("concat('copy_', sno)", "sno")
                        .eq("sno", "s001")
                        .notExists(SubQueryParam.build(new Q("1").eq("sno", "copy_s001")))
        )));
    }

    private static StudentPO getStudent() {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
```

### 动态查询机制
支持动态查询机制，可通过配置字段注解提前定义查询方式和动态关联、静态关联表。
非常灵活的实现前端传参后端动态查询，具体参考单元测试 QueryCriteriaTest

### spring 项目接入 veasion-db
SPI 实现 cn.veasion.db.jdbc.DataSourceProvider 接口
```java
public class DefaultDataSourceProvider implements DataSourceProvider {

    @Override
    public DataSource getDataSource(EntityDao<?, ?> entityDao, JdbcTypeEnum jdbcTypeEnum) {
        // 可已定义根据 jdbcTypeEnum 判断读写类型，获取不同数据源
        // SpringUtils 是获取 bean 的工具类（自写）
        return SpringUtils.getBean(DataSource.class);
    }

    @Override
    public Connection getConnection(DataSource dataSource) throws SQLException {
        return org.springframework.jdbc.datasource.DataSourceUtils.getConnection(dataSource);
    }

    @Override
    public void releaseConnection(DataSource dataSource, Connection connection) {
        return org.springframework.jdbc.datasource.DataSourceUtils.releaseConnection(connection, dataSource);
    }
}
```

### 适配 spring-mybatis
在 springboot / spring 中适配 veasion-db 和 mybatis 共存，见项目 [veasion-db-mybatis](https://github.com/veasion/veasion-db-mybatis)

## 赞助

项目的发展离不开您的支持，请作者喝杯咖啡吧~

ps：辣条也行 ☕

![支付宝](https://veasion.oss-cn-shanghai.aliyuncs.com/alipay.png?x-oss-process=image/resize,m_lfit,h_360,w_360)
