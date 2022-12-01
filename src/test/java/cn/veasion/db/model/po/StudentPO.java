package cn.veasion.db.model.po;

import cn.veasion.db.base.Column;
import cn.veasion.db.base.Table;
import cn.veasion.db.model.enums.SexEnum;

/**
 * StudentPO
 *
 * @author luozhuowei
 * @date 2021/12/12
 */
@Table("t_student")
public class StudentPO extends BasePO {

    private String sno;
    private String name;
    private Long classId;
    private SexEnum sex;
    private Integer age;
    @Column("`desc`")
    private String desc;

    public String getSno() {
        return sno;
    }

    public void setSno(String sno) {
        this.sno = sno;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public SexEnum getSex() {
        return sex;
    }

    public void setSex(SexEnum sex) {
        this.sex = sex;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

}
