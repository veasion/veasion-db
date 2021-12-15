package cn.veasion.db.criteria;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 定义动态触发或静态关联查询集合
 *
 * @author luozhuowei
 * @date 2021/12/15
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface JoinCriteriaMulti {

    JoinCriteria[] value() default {};

}
