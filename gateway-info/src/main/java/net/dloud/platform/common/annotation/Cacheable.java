package net.dloud.platform.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author QuDasheng
 * @create 2018-12-29 15:15
 **/
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface Cacheable {
    /**
     * 缓存时间，单位分钟
     */
    int value() default 1;
}
