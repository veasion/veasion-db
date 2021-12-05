package cn.veasion.db.base;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Column
 *
 * @author luozhuowei
 * @date 2021/12/2
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface Column {

    /**
     * 指定列名，默认驼峰下划线
     */
    String value() default "";

    /**
     * 是否忽略该字段
     */
    boolean ignore() default false;

}
