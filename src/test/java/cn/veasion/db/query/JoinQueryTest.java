package cn.veasion.db.query;

import cn.veasion.db.BaseTest;
import cn.veasion.db.base.Expression;
import cn.veasion.db.base.Filter;
import cn.veasion.db.model.po.ClassesPO;
import cn.veasion.db.model.po.CoursePO;
import cn.veasion.db.model.po.ScorePO;
import cn.veasion.db.model.po.StudentPO;
import cn.veasion.db.model.po.TeacherPO;
import cn.veasion.db.model.vo.CourseScoreVO;
import cn.veasion.db.model.vo.StudentCourseScoreVO;
import cn.veasion.db.model.vo.StudentVO;

/**
 * JoinQueryTest (join优化：mysql建议用小表关联大表（clickhouse相反），最大join数不超过3)
 *
 * @author luozhuowei
 * @date 2021/12/12
 */
public class JoinQueryTest extends BaseTest {

    public static void main(String[] args) {
        // 查询学生及所在班级
        // select s.*, c.class_name from t_student s join t_classes c on s.class_id = c.id
        EQ student = new EQ(StudentPO.class, "s");
        student.join(new EQ(ClassesPO.class, "c").select("className")).on("classId", "id");
        student.selectAll();
        println(studentDao.queryList(student, StudentVO.class));

        // lambda
        LambdaEntityQuery<StudentPO> lambdaStudent = new LambdaEntityQuery<>(StudentPO.class, "s");
        lambdaStudent.join(new LambdaEntityQuery<>(ClassesPO.class, "c").select(ClassesPO::getClassName)).on(StudentPO::getClassId, ClassesPO::getId);
        lambdaStudent.selectAll();
        println(studentDao.queryList(lambdaStudent, StudentVO.class));

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

        // 查询学生学科分数排名（开窗1）
        // select student.name, course.course_name, score.score, rank() over w1 as `rank`
        // from t_student student
        // join t_score score on student.sno = score.sno
        // join t_course course on score.course_id = course.id
        // where score.score > 60
        // window w1 as (partition by course.course_name order by score.score desc)
        // order by score.score desc
        EQ query = new EQ(StudentPO.class, "student");
        query.join(new EQ(ScorePO.class, "score")).on("sno", "sno");
        query.join(new EQ(CoursePO.class, "course")).on("score.courseId", "id");
        query.selects("name", "course.courseName", "score.score");
        query.overWithWindow(Expression.select("rank() over w1", "`rank`"));
        query.gte("score.score", 60);
        query.window(new Window("w1", Expression.sql("partition by course.course_name order by score.score desc")));
        query.desc("score.score");
        println(studentDao.listForMap(query));

        // 查询学生学科分数排名（开窗2）
        // select student.name, course.course_name, score.score, rank() over (partition by course.course_name order by score.score desc) as `rank`
        // from t_student student
        // join t_score score on student.sno = score.sno
        // join t_course course on score.course_id = course.id
        // where score.score > 60
        // order by score.score desc
        EQ _query = new EQ(StudentPO.class, "student");
        _query.join(new EQ(ScorePO.class, "score")).on("sno", "sno");
        _query.join(new EQ(CoursePO.class, "course")).on("score.courseId", "id");
        _query.selects("name", "course.courseName", "score.score");
        _query.overWithWindow(Expression.select("rank() over (partition by course.course_name order by score.score desc)", "`rank`"));
        _query.gte("score.score", 60);
        _query.desc("score.score");
        println(studentDao.listForMap(_query));
    }

}
