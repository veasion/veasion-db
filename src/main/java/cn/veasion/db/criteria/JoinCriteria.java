package cn.veasion.db.criteria;

import cn.veasion.db.base.JoinTypeEnum;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 定义动态触发或静态关联查询
 *
 * @author luozhuowei
 * @date 2021/12/15
 */

@Target(TYPE)
@Retention(RUNTIME)
public @interface JoinCriteria {

    /**
     * join 左边的类（被join的类，默认当前操作实体类）
     */
    Class<?> value() default Void.class;

    /**
     * 关联对象类型（关联后 field 作用会映射到关联对象条件上）
     */
    Class<?> join() default Void.class;

    /**
     * 关联类型，默认 join
     */
    JoinTypeEnum joinType() default JoinTypeEnum.JOIN;

    /**
     * 当前实体类和关联类的 on 字段 <br><br>
     * 示例一（学生关联班级）： {"classId", "id"} <br>
     * 示例二（班级关联班主任教师）: {"masterTno", "tno"} <br>
     * 示例三（班级关联班主任课程，多个字段关联）：{"id", "classId", "masterTno", "tno"} <br>
     * 解释（成对出现）：{"当前字段1", "关联字段1", "当前字段2", "关联字段2", "当前字段3", "关联字段3"}
     */
    String[] onFields() default {};

    /**
     * 是否静态关联（默认false）： true 静态的，任意条件都会关联，false 动态的，只有字段触发关联才关联
     */
    boolean staticJoin() default false;

}
