package net.dloud.platform.actuator.config;

import net.dloud.platform.actuator.ShutdownContainer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;

import java.util.concurrent.TimeUnit;

/**
 * @author QuDasheng
 * @create 2019-01-11 23:00
 **/
@Configuration
@ConditionalOnMissingBean(ShutdownContainer.class)
public class DefaultShutdownConfiguration {

    @Bean
    public ShutdownContainer defaultGracefulShutdown() {
        return new ShutdownContainer() {
            private volatile boolean closed = false;

            @Override
            public boolean closed() {
                return closed;
            }

            @Override
            public void shutdown(ContextClosedEvent event) {
                this.closed = true;
                try {
                    TimeUnit.MINUTES.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };
    }
}
