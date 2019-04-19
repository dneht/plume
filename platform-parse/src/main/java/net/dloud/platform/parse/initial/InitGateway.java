package net.dloud.platform.parse.initial;

import com.alibaba.dubbo.config.annotation.Reference;
import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.client.GatewayService;
import net.dloud.platform.common.extend.ArrayUtil;
import net.dloud.platform.common.security.Digests;
import net.dloud.platform.extend.constant.PlatformConstants;
import net.dloud.platform.parse.utils.InitialiseUtil;
import net.dloud.platform.parse.utils.ResourceGet;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * @author QuDasheng
 * @create 2018-08-27 09:30
 **/
@Slf4j
@Configuration
@ConditionalOnProperty(name = "gateway.notice.enable", matchIfMissing = true, havingValue = "true")
@AutoConfigureAfter(InitSource.class)
public class InitGateway {
    @Reference
    private GatewayService gateway;


    @Bean("gatewayListener")
    public ApplicationListener<ContextRefreshedEvent> gatewayListener() {
        return (event) -> {
            try {
                final String parsePath = PlatformConstants.PARSE_BASE_PATH + PlatformConstants.APPID + "/";
                final byte[][] versions = ResourceGet.resourceMulti2Byte(parsePath + "version");

                final String version;
                if (versions.length == 1) {
                    version = new String(versions[0]);
                    log.info("[GATEWAY] 开始初始化网关, 当前版本({})", version);
                } else if (versions.length > 1) {
                    version = Digests.sha256(ArrayUtil.concat(versions));
                    log.info("[GATEWAY] 开始初始化网关, 存在多个需要解析的包，计算版本({})", version);
                } else {
                    log.info("[GATEWAY] 不需要初始化网关，不存在需要解析的包");
                    return;
                }

                InitialiseUtil.doInitialise(gateway, parsePath, versions, version);
            } catch (Exception e) {
                log.warn("[GATEWAY] 初始化网关失败: ", e);
            }
        };
    }
}
