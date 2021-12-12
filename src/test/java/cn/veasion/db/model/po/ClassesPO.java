package cn.veasion.db.model.po;

import cn.veasion.db.base.Table;

/**
 * ClassesPO
 *
 * @author luozhuowei
 * @date 2021/12/12
 */
@Table("t_classes")
public class ClassesPO extends BasePO {

    private String className;
    private String masterTno;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMasterTno() {
        return masterTno;
    }

    public void setMasterTno(String masterTno) {
        this.masterTno = masterTno;
    }

}
