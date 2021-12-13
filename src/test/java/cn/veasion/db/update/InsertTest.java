package cn.veasion.db.update;

import cn.veasion.db.BaseTest;
import cn.veasion.db.model.po.StudentPO;
import cn.veasion.db.query.EQ;
import cn.veasion.db.query.Q;
import cn.veasion.db.query.SubQueryParam;

import java.util.Arrays;
import java.util.Date;

/**
 * InsertTest
 *
 * @author luozhuowei
 * @date 2021/12/12
 */
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
