package net.dloud.platform.parse.module;

import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.extend.assist.dataccess.WrapperLock;
import net.dloud.platform.extend.constant.PlatformConstants;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author QuDasheng
 * @create 2018-10-07 12:37
 **/
@Slf4j
@Component
@ConditionalOnProperty(name = "assist.init.enable", matchIfMissing = true, havingValue = "true")
public class AssistComponent {
    private final RedissonClient redis;
    private final ConcurrentMap<String, RTopic> topics = new ConcurrentHashMap<>();


    @Autowired
    public AssistComponent(RedissonClient redis) {
        this.redis = redis;
    }

    /**
     * 竞争锁
     *
     * @param key
     * @return
     */
    public WrapperLock getLock(Object key) {
        return new WrapperLock(redis.getLock(PlatformConstants.APPID + "-" + key));
    }

    /**
     * 公平锁
     *
     * @param key
     * @return
     */
    public WrapperLock getFairLock(Object key) {
        return new WrapperLock(redis.getFairLock(PlatformConstants.APPID + "-" + key));
    }
}
