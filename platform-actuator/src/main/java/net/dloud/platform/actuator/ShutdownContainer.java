package net.dloud.platform.actuator;

import org.springframework.context.event.ContextClosedEvent;

/**
 * @author QuDasheng
 * @create 2019-01-09 11:07
 **/
public interface ShutdownContainer {
    /**
     * 是否关闭
     */
    boolean closed();

    /**
     * 关闭容器
     */
    void shutdown(ContextClosedEvent event);
}
