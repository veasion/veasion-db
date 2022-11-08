package cn.veasion.db.model.po;

import cn.veasion.db.base.IBaseId;
import cn.veasion.db.base.Table;
import cn.veasion.db.interceptor.ILogicDelete;

import java.util.Date;

/**
 * AreaPO
 *
 * @author luozhuowei
 * @date 2022/11/8
 */
@Table("t_area")
public class AreaPO implements IBaseId<Long>, ILogicDelete {

    private Long id;
    private String code;
    private String name;
    private Integer level;
    private String abbreviation;
    private String parentCode;
    private String postCode;
    private String nameLan2;
    private Long isDeleted;
    private Date createTime;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getParentCode() {
        return parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public String getNameLan2() {
        return nameLan2;
    }

    public void setNameLan2(String nameLan2) {
        this.nameLan2 = nameLan2;
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
}
