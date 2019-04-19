package net.dloud.platform.gateway.pack;

import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.extend.CollectionUtil;
import net.dloud.platform.common.extend.StringUtil;
import net.dloud.platform.common.gateway.bean.StartupTime;
import net.dloud.platform.extend.constant.PlatformConstants;
import net.dloud.platform.extend.upstream.Invoker;
import net.dloud.platform.extend.upstream.RandomLoadBalance;
import net.dloud.platform.parse.dubbo.wrapper.DubboWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.addOriginalRequestUrl;

/**
 * @author QuDasheng
 * @create 2019-04-17 09:53
 **/
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class RefreshRouterFilter implements GatewayFilter {
    private final RandomLoadBalance loadBalance = new RandomLoadBalance();

    @Value("${dubbo.init.enable:true}")
    private Boolean dubboEnable;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!dubboEnable) {
            return chain.filter(exchange);
        }
        final Map<String, Map<String, StartupTime>> dubboProvider = DubboWrapper.dubboProvider;
        if (CollectionUtil.isEmpty(dubboProvider)) {
            return chain.filter(exchange);
        }

        final ServerHttpRequest request = exchange.getRequest();
        final URI uri = request.getURI();
        final String path = uri.getRawPath();
        if (StringUtil.isBlank(path) || !path.startsWith("/!")) {
            return chain.filter(exchange);
        }

        final String system;
        final String group;
        final String newPath;
        final int idx = path.indexOf("/", 1);
        if (idx < 0) {
            newPath = "";
            system = path.replaceFirst("/!", "");
            group = PlatformConstants.DEFAULT_GROUP;
        } else {
            newPath = path.substring(idx);
            final String target = path.substring(2, idx);

            if (target.contains("-")) {
                final String[] split = target.split("-");
                system = split[0];
                if (split.length == 2) {
                    group = split[1];
                } else {
                    group = PlatformConstants.DEFAULT_GROUP;
                }
            } else {
                system = target;
                group = PlatformConstants.DEFAULT_GROUP;
            }
        }

        final Map<String, StartupTime> startupMap = dubboProvider.getOrDefault(group, dubboProvider.get(PlatformConstants.DEFAULT_GROUP));
        if (CollectionUtil.isEmpty(startupMap)) {
            log.info("[GATEWAY] 无法获取到可用地址 system={}, group={}, {} -> {}", system, group, path, newPath);
            return chain.filter(exchange);
        }

        final List<Invoker<Integer>> invokers = new ArrayList<>();
        for (Map.Entry<String, StartupTime> entry : startupMap.entrySet()) {
            final String key = entry.getKey();
            final StartupTime startup = entry.getValue();
            if (Objects.equals(system, startup.getKey())) {
                final int pos = key.indexOf(":");
                if (pos > 0) {
                    invokers.add(new Invoker<>("http://" + key.substring(0, pos) + ":" + startup.getHttp()));
                } else {
                    invokers.add(new Invoker<>("http://" + key + startup.getHttp()));
                }
            }
        }
        if (CollectionUtil.isEmpty(invokers)) {
            log.info("[GATEWAY] 无法获取到可用地址 system={}, group={}, {} -> {}", system, group, path, newPath);
            return chain.filter(exchange);
        }

        final URI newUri = UriComponentsBuilder.fromHttpUrl(loadBalance.select(invokers).getUri())
                .path(newPath + (newPath.length() > 1 && path.endsWith("/") ? "/" : "")).build().toUri();
        log.info("[GATEWAY] 路由地址 system={}, group={}, {} -> {}, {} -> {}", system, group, path, newPath, uri, newUri);

        addOriginalRequestUrl(exchange, uri);
        final Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        if (null != route) {
            final RouteDefinition definition = new RouteDefinition();
            definition.setId(route.getId());
            definition.setUri(newUri);
            definition.setOrder(route.getOrder());
            exchange.getAttributes().put(GATEWAY_ROUTE_ATTR, Route.async(definition)
                    .asyncPredicate(route.getPredicate()).filters(route.getFilters()).build());
        }
        exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, newUri);

        return chain.filter(exchange.mutate().request(request.mutate().uri(newUri).build()).build());
    }
}
