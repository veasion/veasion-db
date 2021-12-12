package cn.veasion.db.model.po;

import cn.veasion.db.base.Table;

/**
 * CoursePO
 *
 * @author luozhuowei
 * @date 2021/12/12
 */
@Table("t_course")
public class CoursePO extends BasePO {

    private String courseName;
    private Long classId;
    private String tno;

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public String getTno() {
        return tno;
    }

    public void setTno(String tno) {
        this.tno = tno;
    }

}
