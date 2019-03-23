package net.dloud.platform.parse.aspect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author QuDasheng
 * @create 2019-02-24 20:48
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ShardingFrom {
    /**
     * 分表使用的参数，如果为空默认为入参参数的第一个
     */
    String value() default "";

}
