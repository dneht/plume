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
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    public Object getValue(String method) {
        return redisTemplate.opsForValue().get(method);
    }

    public void setValue(String method, Object value, int time) {
        redisTemplate.opsForValue().set(method, value, time, TimeUnit.MINUTES);
    }
}
