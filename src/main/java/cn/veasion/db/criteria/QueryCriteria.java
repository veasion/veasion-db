package cn.veasion.db.criteria;

import cn.veasion.db.base.Operator;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 定义字段动态查询
 *
 * @author luozhuowei
 * @date 2021/12/15
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface QueryCriteria {

    /**
     * 过滤类型（默认EQ）
     */
    Operator value() default Operator.EQ;

    /**
     * 指定字段（默认当前字段名）
     */
    String field() default "";

    /**
     * 指定多个字段 OR 关系
     */
    String[] orFields() default {};

    /**
     * 是否跳过空值，如空字符串、空集合、空数组等（默认跳过）
     */
    boolean skipEmpty() default true;

    /**
     * 关联类，触发动态关联（默认当前操作实体类）
     */
    Class<?> relation() default Void.class;

}
