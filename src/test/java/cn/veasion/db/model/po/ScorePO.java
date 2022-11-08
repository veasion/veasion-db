package cn.veasion.db.model.po;

import cn.veasion.db.base.Table;
import cn.veasion.db.interceptor.ILogicDelete;

import java.util.Date;

/**
 * ScorePO
 *
 * @author luozhuowei
 * @date 2021/12/12
 */
@Table("t_score")
public class ScorePO implements ILogicDelete {

    private String sno;
    private Long courseId;
    private Integer score;
    private Long isDeleted;
    private Date createTime;
    private Date updateTime;

    public String getSno() {
        return sno;
    }

    public void setSno(String sno) {
        this.sno = sno;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Long getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Long isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

}
