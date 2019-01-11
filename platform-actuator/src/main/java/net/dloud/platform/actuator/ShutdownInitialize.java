package net.dloud.platform.actuator;

import io.elasticjob.lite.reg.zookeeper.ZookeeperRegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * @author QuDasheng
 * @create 2019-01-09 11:07
 **/
@Slf4j
@Configuration
public class ShutdownInitialize {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Autowired
    private MessageListenerContainer listenerContainer;

    @Autowired
    private ZookeeperRegistryCenter registryCenter;

    @Autowired
    private ShutdownContainer shutdownContainer;


    @Bean("shutdownListener")
    public ApplicationListener<ContextClosedEvent> shutdownListener() {
        return (event) -> {
            log.info("[PLATFORM] 开始关闭当前服务");
            try {
                listenerContainer.stop();
                registryCenter.close();
                shutdownContainer.shutdown(event);
                taskScheduler.shutdown();
            } catch (Exception e) {
                log.error("[PLATFORM] 关闭当前服务异常: ", e);
            }
        };
    }
}
