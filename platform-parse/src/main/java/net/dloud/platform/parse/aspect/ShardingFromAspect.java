package net.dloud.platform.parse.aspect;

import net.dloud.platform.common.extend.StringUtil;
import net.dloud.platform.parse.aspect.annotation.ShardingFrom;
import net.dloud.platform.parse.utils.SourceGet;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * @author QuDasheng
 * @create 2019-02-24 20:48
 **/
@Aspect
@Component
public class ShardingFromAspect {
    @Pointcut("@annotation(net.dloud.platform.parse.aspect.annotation.ShardingFrom)")
    private void pointCut() {
    }

    @Before("pointCut() && @annotation(name)")
    public void beforeInvoke(JoinPoint point, ShardingFrom name) {
        if (StringUtil.notBlank(name.value())) {
            SourceGet.clientFrom.set(name.value());
        }
        final Object[] args = point.getArgs();
        if (null != args && args.length > 0) {
            SourceGet.clientFrom.set(String.valueOf(args[0]));
        }
    }

    @AfterReturning(pointcut = "pointCut()", returning = "value")
    public Object afterReturning(Object value) {
        SourceGet.clientFrom.remove();
        return value;
    }

    @AfterThrowing(pointcut = "pointCut()")
    public void afterThrowing() {
        SourceGet.clientFrom.remove();
    }
}
