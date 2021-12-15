package cn.veasion.db.update;

import cn.veasion.db.BaseTest;
import cn.veasion.db.model.po.ClassesPO;
import cn.veasion.db.model.po.CoursePO;
import cn.veasion.db.model.po.StudentPO;
import cn.veasion.db.model.po.TeacherPO;

/**
 * UpdateTest
 *
 * @author luozhuowei
 * @date 2021/12/12
 */
public class UpdateTest extends BaseTest {

    public static void main(String[] args) {

        // 普通更新
        // update t_student set age = 20 where id = 2
        println(studentDao.update(new U("age", 20).eq("id", 2)));
        // update t_student set age = 20, sex = 1 where id = 2
        println(studentDao.update(new U().update("age", 20).update("sex", 1).eq("id", 2)));

        // 特殊更新，如乐观锁 version = version + 1
        //  update t_student set age = 20, version = version + 1 where id = 2 and version = 0
        println(studentDao.update(new U()
                .update("age", 18)
                .updateExpression("version", "version + 1")
                .eq("id", 2).eq("version", 0)
        ));

        // 根据ID更新对象不为null的字段
        // update t_student set ... where id = ?
        StudentPO studentPO = new StudentPO();
        studentPO.setId(2L);
        studentPO.setAge(20);
        studentPO.setDesc("哈哈");
        println(studentDao.updateById(studentPO));

        // 年龄累加
        // update t_student set age = age + 20 where id = ?
        println(studentDao.update(new EU(studentPO).eq("id").updateExpression("age", "${age} + #{age}")));

        // 对象乐观锁更新
        // update t_student set version = version = version + 1, age = 20 where id = 2 and version = 1
        studentPO.setVersion(1);
        EntityUpdate entityUpdate = new EntityUpdate(studentPO);
        entityUpdate.updateExpression("version", "version + 1");
        entityUpdate.eq("id").eq("version").excludeUpdateFilterFields();
        println(studentDao.update(entityUpdate));

        // 关联更新（教师修改编码）
        // update t_teacher t
        // left join t_classes c on t.tno = c.master_tno
        // left join t_course course ON t.tno = course.tno
        // set t.tno = ?, c.master_tno = ?, course.tno = ?
        // where t.tno = ?
        EU teacher = new EU(TeacherPO.class, "t");
        EU classes = new EU(ClassesPO.class, "c");
        EU course = new EU(CoursePO.class, "course");
        teacher.leftJoin(classes).on("tno", "masterTno");
        teacher.leftJoin(course).on("tno", "tno");
        teacher.update("tno", "new_t003");
        classes.update("masterTno", "new_t003");
        course.update("tno", "new_t003");
        teacher.eq("tno", "t003");
        println(teacherDao.update(teacher));

    }

}
