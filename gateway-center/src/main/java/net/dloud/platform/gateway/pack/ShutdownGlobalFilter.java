package net.dloud.platform.gateway.pack;


import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.actuator.ShutdownContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 关闭时拒绝服务
 *
 * @author QuDasheng
 * @create 2019-01-10 21:43
 **/
@Slf4j
@Order(-1)
@Component
public class ShutdownGlobalFilter implements GlobalFilter {
    @Autowired
    private ShutdownContainer shutdownContainer;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!shutdownContainer.closed()) {
            return chain.filter(exchange);
        }
        exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        return exchange.getResponse().setComplete();
    }
}
