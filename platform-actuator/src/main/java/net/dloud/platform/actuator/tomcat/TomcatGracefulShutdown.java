package net.dloud.platform.actuator.tomcat;

import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.actuator.ShutdownContainer;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * @author QuDasheng
 * @create 2019-01-11 11:17
 **/
@Slf4j
@Component
@ConditionalOnClass(Tomcat.class)
public class TomcatGracefulShutdown implements ShutdownContainer, TomcatConnectorCustomizer {
    private volatile boolean closed = false;

    private volatile Connector connector;

    @Override
    public void customize(Connector connector) {
        this.connector = connector;
    }

    @Override
    public boolean closed() {
        return closed;
    }

    @Override
    public void shutdown(ContextClosedEvent event) {
        closed = true;
        final Executor executor = this.connector.getProtocolHandler().getExecutor();
        if (executor instanceof ThreadPoolExecutor) {
            try {
                ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
                threadPoolExecutor.shutdown();
                if (!threadPoolExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.warn("Tomcat thread pool did not shut down gracefully within 60 seconds. Proceeding with forceful shutdown");
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
