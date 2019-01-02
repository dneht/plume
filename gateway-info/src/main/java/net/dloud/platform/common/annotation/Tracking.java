package net.dloud.platform.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author QuDasheng
 * @create 2019-01-02 11:09
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Tracking {
}
