package cn.veasion.db.criteria;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 加载结果关联数据（resultClass / queryClass 字段上使用，关联关系在 QueryCriteria class 上提前定义）<br>
 * <br>
 * resultClass 上使用时每次都会加载，queryClass 字段上使用时触发加载。
 *
 * @author luozhuowei
 * @date 2021/12/26
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface LoadRelation {

    Class<?> value() default Void.class;

    /**
     * 在 queryClass 字段使用时生效，表示 resultClass 加载字段
     */
    String resultClassField() default "";

}
