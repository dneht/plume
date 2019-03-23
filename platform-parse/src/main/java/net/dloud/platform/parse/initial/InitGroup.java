package net.dloud.platform.parse.initial;

import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.extend.constant.PlatformConstants;
import net.dloud.platform.parse.curator.wrapper.CuratorWrapper;
import net.dloud.platform.parse.dubbo.wrapper.DubboWrapper;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

import static net.dloud.platform.parse.dubbo.wrapper.DubboWrapper.DUBBO_GROUP_PATH;

/**
 * @author QuDasheng
 * @create 2019-01-31 11:47
 **/
@Slf4j
@Configuration
@ConditionalOnProperty(name = "dubbo.group.enable", matchIfMissing = true, havingValue = "true")
@AutoConfigureAfter(InitDubbo.class)
public class InitGroup {

    @Bean("dubboListener")
    public ApplicationListener<ContextRefreshedEvent> dubboListener() {
        return (event) -> {
            final String currentPath = DubboWrapper.currentPath();
            log.info("[PLATFORM] DUBBO初始化GROUP使用地址: {}", currentPath);

            try {
                CuratorWrapper.addListeners(() -> CuratorWrapper.childrenCache(DubboWrapper.dubboListener
                        (PlatformConstants.GROUP, DUBBO_GROUP_PATH), currentPath, DUBBO_GROUP_PATH));
            } catch (Exception e) {
                log.error("[PLATFORM] DUBBO初始化GROUP失败: {}, {}", currentPath, e.getMessage());
            }
        };
    }
}
