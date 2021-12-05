package cn.veasion.db.base;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Table
 *
 * @author luozhuowei
 * @date 2021/12/1
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Table {

    /**
     * 指定表名，为空默认类名驼峰下划线
     */
    String value() default "";

    /**
     * Id 是否自增：需要实现 IBaseId
     */
    boolean autoIncrement() default false;

    /**
     * 映射到 PO 实体类
     */
    Class<?> entityClass() default Void.class;

}
