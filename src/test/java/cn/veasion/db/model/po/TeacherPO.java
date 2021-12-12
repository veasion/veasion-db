package cn.veasion.db.model.po;

import cn.veasion.db.base.Table;

/**
 * TeacherPO
 *
 * @author luozhuowei
 * @date 2021/12/12
 */
@Table("t_teacher")
public class TeacherPO extends BasePO {

    private String tno;
    private String name;
    private Integer sex;
    private Integer workYears;
    private String competent;
    private String department;

    public String getTno() {
        return tno;
    }

    public void setTno(String tno) {
        this.tno = tno;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public Integer getWorkYears() {
        return workYears;
    }

    public void setWorkYears(Integer workYears) {
        this.workYears = workYears;
    }

    public String getCompetent() {
        return competent;
    }

    public void setCompetent(String competent) {
        this.competent = competent;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

}
