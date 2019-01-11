package net.dloud.platform.gateway.pack;


import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.extend.bucket.Bandwidth;
import net.dloud.platform.extend.bucket.Bucket;
import net.dloud.platform.extend.bucket.SimpleBucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.Duration;

/**
 * 给定一个全局的限流filter
 *
 * @author QuDasheng
 * @create 2018-09-07 23:57
 **/
@Slf4j
@Order(1)
@Component
public class ThrottleGlobalFilter implements GlobalFilter {
    @Value("${global.second.limit:2000}")
    private Integer limit;

    private Bucket bucket;


    @PostConstruct
    private void init() {
        bucket = SimpleBucket.build(Bandwidth.simple(limit, Duration.ofSeconds(1)));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (bucket.tryConsume(1)) {
            return chain.filter(exchange);
        }
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        return exchange.getResponse().setComplete();
    }
}
