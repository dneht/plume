package net.dloud.platform.gateway.pack;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author QuDasheng
 * @create 2018-12-27 14:19
 **/
@Slf4j
@Component
public class GatewayCache {
    private static final char separator = ':';

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    public Object getValue(String method, Object param, String tenant, String group) {
        return redisTemplate.opsForValue().get(method + separator + tenant + separator + group
                + separator + param.hashCode());
    }

    public void setValue(String method, Object param, String tenant, String group, Object value, int time) {
        redisTemplate.opsForValue().set(method + separator + tenant + separator + group
                + separator + param.hashCode(), value, time, TimeUnit.MINUTES);
    }
}
