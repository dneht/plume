package net.dloud.platform.parse.initial;

import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.extend.constant.PlatformConstants;
import net.dloud.platform.parse.curator.wrapper.CuratorWrapper;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author QuDasheng
 * @create 2018-10-03 15:17
 **/
@Slf4j
@Configuration
public class InitDefault {
    private static CuratorFramework curatorClient;


    @PostConstruct
    private void init() {
        curatorClient = CuratorWrapper.initClient();
    }

    @PreDestroy
    private void destroy() {
        curatorClient.close();
    }

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        final ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(PlatformConstants.PROCESSOR_NUMBER * 2);
        taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
        taskScheduler.initialize();
        return taskScheduler;
    }
}
