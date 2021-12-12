package cn.veasion.db.model.po;

import cn.veasion.db.base.IBaseId;

import java.util.Date;

/**
 * BasePO
 *
 * @author luozhuowei
 * @date 2021/12/3
 */
public class BasePO implements IBaseId<Long> {

    private Long id;
    private Integer version;
    private Long isDeleted;
    private Date createTime;
    private Date updateTime;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
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
