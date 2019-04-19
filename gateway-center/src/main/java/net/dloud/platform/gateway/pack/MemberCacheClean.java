package net.dloud.platform.gateway.pack;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.domain.BaseResult;
import net.dloud.platform.common.gateway.InjectEnum;
import net.dloud.platform.common.gateway.bean.TokenKey;
import net.dloud.platform.parse.kafka.KafkaConsumer;
import net.dloud.platform.parse.kafka.annotation.Consumer;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static net.dloud.platform.gateway.pack.GatewayCache.getTokenCache;

/**
 * @author QuDasheng
 * @create 2018-12-27 14:19
 **/
@Slf4j
@Consumer(value = "memberCacheClean", describe = "清理用户缓存")
public class MemberCacheClean implements KafkaConsumer<TokenKey> {

    @Override
    public BaseResult onMessage(TokenKey tokenKey) {
        log.info("[GATEWAY] 要删除网关中用户缓存是: {}", tokenKey);
        final Cache<TokenKey, Map<String, Object>> tokenCache = getTokenCache();
        for (InjectEnum inject : InjectEnum.values()) {
            tokenKey.setInject(inject.method());
            tokenCache.invalidate(tokenKey);
        }
        return new BaseResult();
    }
}
