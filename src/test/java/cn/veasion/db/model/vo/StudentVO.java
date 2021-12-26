package cn.veasion.db.model.vo;

import cn.veasion.db.base.Table;
import cn.veasion.db.criteria.LoadRelation;
import cn.veasion.db.model.po.ClassesPO;
import cn.veasion.db.model.po.StudentPO;
import cn.veasion.db.model.po.TeacherPO;

import java.util.List;

/**
 * StudentVO
 *
 * @author luozhuowei
 * @date 2021/12/12
 */
@Table(entityClass = StudentPO.class)
public class StudentVO extends StudentPO {

    private String className;
    private String teacherName;

    private List<ClassesPO> classList;
    @LoadRelation(TeacherPO.class)
    private List<TeacherPO> teacherList;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public List<ClassesPO> getClassList() {
        return classList;
    }

    public void setClassList(List<ClassesPO> classList) {
        this.classList = classList;
    }

    public List<TeacherPO> getTeacherList() {
        return teacherList;
    }

    public void setTeacherList(List<TeacherPO> teacherList) {
        this.teacherList = teacherList;
    }
}
