package cn.veasion.db.query;

import cn.veasion.db.BaseTest;
import cn.veasion.db.base.Filter;
import cn.veasion.db.base.Operator;
import cn.veasion.db.model.po.ClassesPO;
import cn.veasion.db.model.po.CoursePO;
import cn.veasion.db.model.po.StudentPO;
import cn.veasion.db.model.po.TeacherPO;
import cn.veasion.db.model.vo.StudentVO;

/**
 * SubQueryTest
 *
 * @author luozhuowei
 * @date 2021/12/12
 */
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

        // lambda
        LambdaEntityQuery<ClassesPO> lambdaSubQuery1 = new LambdaEntityQuery<>(ClassesPO.class).select(ClassesPO::getMasterTno).eq(ClassesPO::getClassName, "初一一班");
        LambdaEntityQuery<ClassesPO> lambdaSubQuery2 = new LambdaEntityQuery<>(ClassesPO.class, "c").eq(ClassesPO::getClassName, "初一一班");
        lambdaSubQuery2.join(new LambdaEntityQuery<>(CoursePO.class, "course").select(CoursePO::getTno)).on(ClassesPO::getId, CoursePO::getClassId);
        println(teacherDao.queryList(new LambdaQuery<TeacherPO>()
                .filterSubQuery(TeacherPO::getTno, Operator.IN, SubQueryParam.build(lambdaSubQuery1))
                .addFilters(Filter.or())
                .filterSubQuery(TeacherPO::getTno, Operator.IN, SubQueryParam.build(lambdaSubQuery2))
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
