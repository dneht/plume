package net.dloud.platform.parse.initial;

import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.extend.constant.PlatformConstants;
import net.dloud.platform.parse.curator.wrapper.CuratorWrapper;
import net.dloud.platform.parse.schedule.TraceDecorator;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author QuDasheng
 * @create 2018-10-03 15:17
 **/
@Slf4j
@Configuration
@AutoConfigureAfter(InitDubbo.class)
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
    public ThreadPoolTaskExecutor taskExecutor() {
        final ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(PlatformConstants.PROCESSOR_NUMBER * 2);
        taskExecutor.setMaxPoolSize(PlatformConstants.PROCESSOR_NUMBER * 4);
        taskExecutor.setTaskDecorator(new TraceDecorator());
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        taskExecutor.initialize();
        return taskExecutor;
    }

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        final ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(PlatformConstants.PROCESSOR_NUMBER * 2);
        taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
        taskScheduler.initialize();
        return taskScheduler;
    }

    @Bean
    public ApplicationEventMulticaster eventMulticaster() {
        final SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
        eventMulticaster.setTaskExecutor(taskExecutor());
        return eventMulticaster;
    }
}
