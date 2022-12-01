package cn.veasion.db.model.po;

import cn.veasion.db.base.IBaseId;
import cn.veasion.db.base.Table;
import cn.veasion.db.interceptor.ILogicDelete;
import cn.veasion.db.interceptor.ITenantId;
import cn.veasion.db.model.enums.SexEnum;

import java.util.Date;

/**
 * SaasUserPO
 *
 * @author luozhuowei
 * @date 2022/12/1
 */
@Table("t_saas_user")
public class SaasUserPO implements IBaseId<String>, ILogicDelete, ITenantId {

    private String id;
    private String phone;
    private String name;
    private SexEnum sex;
    private Date createTime;
    private Long isDeleted;
    private Long tenantId;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SexEnum getSex() {
        return sex;
    }

    public void setSex(SexEnum sex) {
        this.sex = sex;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public Long getIsDeleted() {
        return isDeleted;
    }

    @Override
    public void setIsDeleted(Long isDeleted) {
        this.isDeleted = isDeleted;
    }

    @Override
    public Long getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
}
