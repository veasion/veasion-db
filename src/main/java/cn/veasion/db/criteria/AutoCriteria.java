package cn.veasion.db.criteria;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 自动动态查询（约定规则）
 * <pre>
 *    注解建议使用在字段类型为 Map 上，key 为字段名称（前端传），value 为值 （前端传）;
 *    为防止存在SQL注入 key 正则匹配 [_0-9a-zA-Z]+ 才会生效。
 *    约定规则一：value 数值、字符串等基础类型统一 Operator.EQ 处理
 *    约定规则二：value 字符串类型前缀为 % 后缀为 % 全模糊，前缀 % 走左模糊，后缀 % 走右模糊
 *    约定规则三：value 为集合、数组类型走 Operator.IN
 *    约定规则四：key 以 start_ 开头走 Operator.GTE，如 start_createTime 走 createTime >= value
 *    约定规则五：key 以 end_ 开头走 Operator.LTE，如 end_createTime 走 createTime <= value
 *    约定规则六：日期类型自动转 yyyy-MM-dd HH:mm:ss 字符串
 * </pre>
 *
 * @author luozhuowei
 * @date 2021/12/15
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface AutoCriteria {

    /**
     * 是否跳过空值，如空字符串、空集合、空数组等（默认跳过）
     */
    boolean skipEmpty() default true;

    /**
     * 关联类，触发动态关联（默认当前操作实体类）
     */
    Class<?> relation() default Void.class;

}
