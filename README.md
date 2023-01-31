# veasion-db

veasion-db 是一个轻量级持久层ORM框架，除slf4j-api外不依赖任何第三方jar，该框架提供丰富灵活的数据库操作，
单元测试 query/update 目录下有大量示例及demo。

框架无需写SQL，基本支持任意查询或更新，如多表关联查询、多表关联更新、子查询、union、with、window、insert select、replace、不同数据库分页等。

框架支持自定义拦截器，内置逻辑删除、数据隔离拦截器，可通过SPI或调用InterceptorUtils.addInterceptor方法加入扩展。
## maven 依赖
添加 veasion-db 依赖
```xml
<dependency>
    <groupId>cn.veasion</groupId>
    <artifactId>veasion-db</artifactId>
    <version>1.2.6</version>
</dependency>
```
支持sql解析生成veasion-db代码
```
String sql = "select * from t_student where id = 1";
String code = SQLParseUtils.parseSQLConvert(sql);
// 直接把SQL转换成对应的代码，示例参考单元测试 SqlDbConvertTest

// 该功能为扩展功能需要加入第三方依赖，示例见单元测试 SqlDbConvertTest
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
        // 查询全部学生（*）
        // select * from t_student
        studentDao.queryList(new Q());

        // 查询全部学生（全部字段）
        // select id, sno, name, class_id, sex, age, `desc`, version, is_deleted, create_time, update_time from t_student
        studentDao.queryList(new Q().selectAllWithNoAsterisk());

        // 根据id查询学生
        // select * from t_student where id = 1
        studentDao.getById(1L);

        // 根据id查询学生性别（性别转枚举）
        // select sex from t_student where id = 1
        SexEnum sexEnum = studentDao.queryForType(new Q("sex").eq("id", 1), SexEnum.class);

        // 查询学号为s001的学生名称
        // select name from t_student where sno = 's001'
        studentDao.queryForType(new Q("name").eq("sno", "s001"), String.class);
        // lambda
        studentDao.queryForType(new LambdaQuery<>(StudentPO::getName).eq(StudentPO::getSno, "s001"), String.class);

        // 查询所有班级名称
        // select class_name from t_classes
        classesDao.queryList(new Q("className"), String.class);
        classesDao.queryList(new LambdaQuery<>(ClassesPO::getClassName), String.class);

        // 查询年龄满18的学生
        // select * from t_student where age >= 18
        studentDao.queryList(new Q().gte("age", 18));
        studentDao.queryList(new LambdaQuery<StudentPO>().gte(StudentPO::getAge, 18));

        // 查询年龄在16-18之间的男学生
        // select * from t_student where sex = 1 and age between 16 and 18
        studentDao.queryList(new Q().eq("sex", 1).between("age", 16, 18));
        studentDao.queryList(new LambdaQuery<StudentPO>().eq(StudentPO::getSex, 1).between(StudentPO::getAge, 16, 18));

        // 查询熊姓学生
        // select * from t_student where name like '熊%'
        studentDao.queryList(new Q().likeRight("name", "熊"));
        studentDao.queryList(new LambdaQuery<StudentPO>().likeRight(StudentPO::getName, "熊"));

        // 查询特殊备注的学生
        // select sno, name, `desc` from t_student where `desc` is not null
        studentDao.queryList(new Q("sno", "name", "desc").isNotNull("desc"));
        studentDao.queryList(new LambdaQuery<>(StudentPO::getSno, StudentPO::getName, StudentPO::getDesc).isNotNull(StudentPO::getDesc));

        // 查询年龄最大学生
        // select * from t_student order by age desc limit 1
        studentDao.query(new Q().desc("age").page(1, 1));

        // 查询学生人数和平均年龄
        // select count(1) as count, avg(age) as avgAge from t_student
        studentDao.queryForMap(new Q()
                .selectExpression("count(1)", "count")
                .selectExpression("avg(${age})", "avgAge")
        );

        // 统计学生性别人数小于5的性别及人数
        // select sex, count(id) as count from t_student group by sex having count < 5
        studentDao.queryForMap(new Q()
                .select("sex")
                .selectExpression("count(id)", "count")
                .groupBy("sex")
                .having(Filter.lt("count", 5))
        );

        // 查询小于平均年龄的女学生
        // select * from t_student where sex = 2 and age < (select avg(age) from t_student)
        studentDao.queryList(new Q()
                .eq("sex", 2)
                .filterSubQuery("age", Operator.LT, SubQueryParam.build(
                        new Q().selectExpression("avg(age)", null)
                ))
        );

        // 查询姓名里存在“熊”或者“小”的男学生
        // select * from t_student where sex = 1 and (name like '%熊%' or name like '%小%')
        studentDao.queryList(new Q()
                .eq("sex", 1)
                .andBracket(Filter.like("name", "熊"), Filter.or(), Filter.like("name", "小"))
        );

        // 分页查询学生（第二页）
        // select * from t_student limit 10, 10
        studentDao.queryPage(new Q().page(2, 10));

        // 查询空表
        // select 1 from dual
        studentDao.queryForType(new EQ(new TableEntity("dual")).select("1"), Integer.class);

        // 无表查询
        // select 1
        studentDao.queryForType(new EQ(new TableEntity(null)).select("1"), Integer.class);
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
        studentDao.queryList(student, StudentVO.class);

        // lambda
        LambdaEntityQuery<StudentPO> lambdaStudent = new LambdaEntityQuery<>(StudentPO.class, "s");
        lambdaStudent.join(new LambdaEntityQuery<>(ClassesPO.class, "c").select(ClassesPO::getClassName)).on(StudentPO::getClassId, ClassesPO::getId);
        lambdaStudent.selectAll();
        studentDao.queryList(lambdaStudent, StudentVO.class);

        // 查询平均分及格的所有课程
        // select c.*, avg(s.score) as avgScore from t_course c join t_score s on c.id = s.course_id having avgScore >= 60
        EQ c = new EQ(CoursePO.class, "c");
        c.join(new EQ(ScorePO.class, "s")).on("id", "courseId");
        c.selectAll().selectExpression("avg(s.score)", "avgScore");
        c.having(Filter.gte("avgScore", 60));
        courseDao.queryList(c, CourseScoreVO.class);

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
        classesDao.queryList(classes1, TeacherPO.class);

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

        studentDao.queryList(_student, StudentCourseScoreVO.class);
    }

}
```
### 子查询示例
```java
public class SubQueryTest extends BaseTest {

    public static void main(String[] args) {
        // 简单子查询
        // select count(id) from (select * from t_student) t
        studentDao.queryForType(new SubQuery(new Q(), "t").selectExpression("count(id)", "count"), Integer.class);

        // 通过子查询关联查询小于平均年龄的男同学
        // select s.* from t_student s join (select avg(age) as age from t_student) t on s.age < t.age where s.sex = 2
        EQ student = new EQ(StudentPO.class, "s");
        student.join(
                new SubQuery(new Q().selectExpression("avg(age)", "age"), "t")
        ).on(Filter.expression("s.age", Operator.LT, "t.age"));
        student.selectAll().eq("sex", 2);
        studentDao.queryList(student);

        // 通过子查询查询 “初一一班” 班主任及所有任课老师
        // select * from t_teacher where
        // tno in (select master_tno from t_classes where class_name = '初一一班')
        // or
        // tno in (select course.tno from t_classes c inner join t_course course on c.id = course.class_id where c.class_name = '初一一班')
        EntityQuery subQuery1 = new EQ(ClassesPO.class).select("masterTno").eq("className", "初一一班");
        EntityQuery subQuery2 = new EQ(ClassesPO.class, "c").eq("className", "初一一班");
        subQuery2.join(new EQ(CoursePO.class, "course").select("tno")).on("id", "classId");
        teacherDao.queryList(new Q()
                .filterSubQuery("tno", Operator.IN, SubQueryParam.build(subQuery1))
                .addFilters(Filter.or())
                .filterSubQuery("tno", Operator.IN, SubQueryParam.build(subQuery2))
        );

        // 通过子查询来查询学生班级名称
        // select s.*, (select class_name from t_classes where id = s.class_id) as className from t_student
        studentDao.queryList(new EQ(StudentPO.class, "s")
                .selectAll()
                .selectSubQuery(SubQueryParam.build(
                        new EQ(ClassesPO.class)
                                .select("className")
                                .filterExpression("id", Operator.EQ, "${s.classId}")
                )), StudentVO.class
        );

        /*
        // 模拟 oracle 分页
        // 等价于 studentDao.queryList(new Q().page(new OraclePage(1, 10)))
        // select t.* from (select t.*, ROWNUM as row from (select * from t_student) t where ROWNUM <= 10) t where t.row > 0
        int page = 1, size = 10;
        studentDao.queryList(
                new SubQuery(
                        new SubQuery(new Q(), "t")
                                .selectAll()
                                .realSelect("ROWNUM", "row")
                                .realFilter(Filter.lte("ROWNUM", page * size)),
                        "t"
                ).gt("row", page * size - size)
        );
        */
    }

}
```

### With查询示例
```java
public class WithQueryTest extends BaseTest {

    public static void main(String[] args) {
        // 通过 with 联合查询
        // with area1 as (select * from t_area where level = 1),
        // area2 as (select * from t_area where level = 2)
        // select * from area1
        // union all
        // select * from area2
        EntityQuery entityQuery = With.build()
                .with(new EQ(AreaPO.class).eq("level", 1), "area1")
                .with(new EQ(AreaPO.class).eq("level", 2), "area2")
                .buildQuery(
                        new EQ(new TableEntity("area1")).unionAll(new EQ(new TableEntity("area2")))
                );
        areaDao.queryList(entityQuery);

        // 通过 with 递归查询
        // with recursive area as (
        //   select * from t_area where code = '310000'
        //   union all
        //   select t1.* from t_area t1 join area t2 on t1.parent_code = t2.code
        // )
        // select * from area
        EntityQuery t1 = new EQ(AreaPO.class, "t1");
        EntityQuery t2 = new EQ(new TableEntity("area"), "t2");
        t1.join(t2).on("parentCode", "code");
        t1.selectAll();

        EntityQuery entityQuery2 = With.buildRecursive()
                .with(new EQ(AreaPO.class).eq("code", "310000").unionAll(t1), "area")
                .buildQuery(new EQ(new TableEntity("area")));
        areaDao.queryList(entityQuery2);
    }

}
```

### 新增示例
```java
public class InsertTest extends BaseTest {

    public static void main(String[] args) {

        // 新增一个学生
        // insert into t_student(...) values (...)
        studentDao.add(getStudent());

        // 批量新增学生
        // insert into t_student(...) values (...), (...), (...)
        studentDao.batchAdd(Arrays.asList(getStudent(), getStudent(), getStudent()));

        // insert select 新增学生
        // insert into t_student(...) select ... from student order by id desc limit 1
        Long[] ids = studentDao.batchAdd(new BatchEntityInsert(
                new EQ(StudentPO.class)
                        .selects("age", "sex", "version", "isDeleted", "createTime")
                        .selectExpression("concat('copy_', name)", "name")
                        .selectExpression("concat('copy_', sno)", "sno")
                        .desc("id").page(1, 1)
        ));
        studentDao.queryList(new Q().in("id", ids));

        // 防学号重复新增学生
        // insert into t_student(...) select concat('copy_', sno), ... from t_student
        // where sno = 's001' and not exists (select 1 from t_student where sno = 'copy_s001')
        studentDao.batchAdd(new BatchEntityInsert(
                new EQ(StudentPO.class)
                        .selects("age", "sex", "version", "isDeleted", "createTime")
                        .selectExpression("concat('copy_', name)", "name")
                        .selectExpression("concat('copy_', sno)", "sno")
                        .eq("sno", "s001")
                        .notExists(SubQueryParam.build(new Q("1").eq("sno", "copy_s001")))
        ));

        // replace into
        StudentPO student = getStudent();
        student.setId(System.currentTimeMillis());
        student.setDesc("replace: add");
        studentDao.add(new EntityInsert(student).withReplace());
        student.setDesc("replace: update");
        studentDao.add(new EntityInsert(student).withReplace());
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
        studentPO.setSex(SexEnum.MALE);
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
非常灵活的实现前端传参后端动态查询，支持后端不需要写任何代码，根据前端传参自动关联表进行各种条件的动态查询
<br><br>
动态查询说明：<br>
前端传 { id: 1 } 自动映射成 id = 1 <br>
前端传 { id: [1, 2, 3] } 自动映射成 id in (1,2,3) <br>
前端传 { gte_age: 18 } 自动映射成 age >= 18 <br>
前端传 { gt_age: 18 } 自动映射成 age > 18 <br>
前端传 { lte_age: 30 } 自动映射成 age <= 30 <br>
前端传 { lt_age: 30 } 自动映射成 age < 30 <br>
前端传 { neq_age: 30 } 自动映射成 age != 30 <br>
前端传 { neq_age: [1, 2, 3] } 自动映射成 age not in (1,2,3) <br>
前端传 { name: '罗' } 自动映射成 name = '罗' <br>
前端传 { name: '罗%' } 自动映射成 name like '罗%' <br>
前端传 { name: '%罗%' } 自动映射成 name like '%罗%' <br>

动态关联说明：<br>
有一张学生表 t_student 和一张班级表 t_classes
如果前端传了 className 字段（学生表只有 class_id 关联班级表）就会进行自动关联 t_classes 表去查询，不用写任何代码自动根据参数去动态关联表查询。<br>
前端传 { id: 1 } 自动映射成 select * from t_student where id = 1 <br>
前端传 { id: 1, className: '三年二班' } 自动映射成 select s.* from t_student s join t_classes c on s.class_id = c.id where s.id = 1 and c.name = '三年二班' <br>

具体参考单元测试 cn.veasion.db.criteria.QueryCriteriaTest

### 拦截器
自定义拦截器可继承抽象类 cn.veasion.db.interceptor.AbstractInterceptor<br>

内置：逻辑删除拦截器 cn.veasion.db.interceptor.LogicDeleteInterceptor <br>
内置：拒绝无条件修改删除拦截器 cn.veasion.db.interceptor.UpdateDeleteNoFilterInterceptor <br>

其他如租户SaaS数据隔离拦截器实现见单元测试：cn.veasion.db.interceptor.TenantInterceptor <br>

需要使用上面拦截器功能可在项目 resources/META-INF/services 目录下新建 SPI 文件 cn.veasion.db.interceptor.EntityDaoInterceptor 中加入指定拦截器类，或显性调用InterceptorUtils.addInterceptor方法添加<br>

具体参考单元测试代码示例。

### 类型转换
框架默认支持基本数据类型转换，其他类型可自定义扩展，SPI 实现 cn.veasion.db.utils.TypeConvert 接口<br>

示例：枚举转换扩展见单元测试 cn.veasion.db.interceptor.ExtTypeConvert

### 动态表名
见 cn.veasion.db.jdbc.DefaultDynamicTableExt 加入SPI支持。<br>

具体参考单元测试：cn.veasion.db.table.DynamicTableTest

### spring 项目接入 veasion-db
推荐使用基础框架 [veasion-project-base](https://github.com/veasion/veasion-project-base) 接入

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

### 基础封装
基于 springboot 基础封装见项目 [veasion-project-base](https://github.com/veasion/veasion-project-base)

## 赞助

项目的发展离不开您的支持，请作者喝杯咖啡吧~

![支付宝](https://veasion.oss-cn-shanghai.aliyuncs.com/alipay.png?x-oss-process=image/resize,m_lfit,h_360,w_360)
