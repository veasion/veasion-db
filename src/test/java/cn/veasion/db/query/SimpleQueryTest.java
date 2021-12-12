package cn.veasion.db.query;

import cn.veasion.db.BaseTest;
import cn.veasion.db.base.Expression;
import cn.veasion.db.base.Filter;
import cn.veasion.db.interceptor.LogicDeleteInterceptor;
import cn.veasion.db.model.po.Dual;

/**
 * SimpleQueryTest
 *
 * @author luozhuowei
 * @date 2021/12/12
 */
public class SimpleQueryTest extends BaseTest {

    public static void main(String[] args) {
        // 查询全部学生
        // select * from t_student
        println(studentDao.queryList(new Q()));

        // 根据id查询学生
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
                .selectExpression(Expression.select("count(1)", "count"))
                .selectExpression(Expression.select("avg(${age})", "avgAge"))
        ));

        // 统计学生性别人数小于5的性别及人数
        // select sex, count(id) as count from t_student group by sex having count < 5
        println(studentDao.queryForMap(new Q()
                .select("sex")
                .selectExpression(Expression.select("count(id)", "count"))
                .groupBy("sex")
                .having(Filter.lt("count", 5))
        ));

        // 查询小于平均年龄的女学生
        // select * from t_student where sex = 2 and age < (select avg(age) from t_student)
        println(studentDao.queryList(new Q()
                .eq("sex", 2)
                .filterSubQuery("age", Filter.Operator.LT, SubQueryParam.build(
                        new Q().selectExpression(Expression.select("avg(age)", null))
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
        LogicDeleteInterceptor.skip(Dual.class);
        println(studentDao.queryForType(new EQ(Dual.class).select("1"), Integer.class));
    }

}
