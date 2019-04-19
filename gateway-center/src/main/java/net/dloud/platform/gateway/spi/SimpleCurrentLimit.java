package net.dloud.platform.gateway.spi;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.extend.CollectionUtil;
import net.dloud.platform.common.extend.NumberUtil;
import net.dloud.platform.common.gateway.bean.RequestInfo;
import net.dloud.platform.common.provider.CurrentLimit;
import net.dloud.platform.extend.bucket.Bandwidth;
import net.dloud.platform.extend.bucket.Bucket;
import net.dloud.platform.extend.bucket.SimpleBucket;
import net.dloud.platform.parse.utils.AddressGet;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static net.dloud.platform.extend.constant.PlatformConstants.CONFIG;

/**
 * @author QuDasheng
 * @create 2018-11-22 14:00
 **/
@Slf4j
public class SimpleCurrentLimit implements CurrentLimit<Long> {
    private static final long DEFAULT_CONSUME_TOKENS = 1;
    private static Cache<Long, List<Bandwidth>> bucketCache = Caffeine.newBuilder().maximumSize(100_000)
            .expireAfterWrite(20, TimeUnit.MINUTES)
            .build();


    @Override
    public Long getKey(RequestInfo request, Map<String, Object> member) {
        long newKey;
        try {
            newKey = CollectionUtil.notEmpty(member) ? NumberUtil.toLong(member.get("userId")) : convertIp(request);
        } catch (Exception e) {
            log.warn("[GATEWAY] 获取限流key错误: {}", member);
            newKey = convertIp(request);
        }
        return newKey;
    }

    @Override
    public boolean tryConsume(RequestInfo request, Map<String, Object> member) {
        final Long key = getKey(request, member);
        Bucket bucket;
        List<Bandwidth> bandwidths = bucketCache.getIfPresent(key);
        if (null == bandwidths) {
            bucket = loadBucket(key);
        } else {
            bucket = loadBucket(bandwidths);
        }

        boolean consume = bucket.tryConsume(DEFAULT_CONSUME_TOKENS);
        bucketCache.put(key, bucket.getBandwidths());
        return consume;
    }

    private static long convertIp(RequestInfo request) {
        return -((((long) Integer.MAX_VALUE) << 8) + AddressGet.remoteAddress(request));
    }

    private Bucket loadBucket(List<Bandwidth> bandwidths) {
        return SimpleBucket.build(bandwidths);
    }

    private Bucket loadBucket(Long key) {
        if (key <= 0) {
            return SimpleBucket.build(
                    Bandwidth.simple(CONFIG.getIntProperty(
                            "throttle.ip.second.limit", 10), Duration.ofSeconds(1)),
                    Bandwidth.simple(CONFIG.getIntProperty(
                            "throttle.ip.minute.limit", 100), Duration.ofMinutes(1)));
        } else {
            return SimpleBucket.build(
                    Bandwidth.simple(CONFIG.getIntProperty(
                            "throttle.user.second.limit", 10), Duration.ofSeconds(1)),
                    Bandwidth.simple(CONFIG.getIntProperty(
                            "throttle.user.minute.limit", 200), Duration.ofMinutes(1)));
        }
    }
}