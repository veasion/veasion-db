package cn.veasion.db.model.vo;

import cn.veasion.db.base.Table;
import cn.veasion.db.model.po.StudentPO;

/**
 * StudentVO
 *
 * @author luozhuowei
 * @date 2021/12/12
 */
@Table(entityClass = StudentPO.class)
public class StudentVO extends StudentPO {

    private String className;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}