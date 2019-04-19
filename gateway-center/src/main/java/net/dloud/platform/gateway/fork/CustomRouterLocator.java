package net.dloud.platform.gateway.fork;

import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.gateway.pack.RefreshRouterFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author QuDasheng
 * @create 2018-09-16 15:50
 */
@Slf4j
@Configuration
public class CustomRouterLocator {
    @Autowired
    private RefreshRouterFilter refreshRouterFilter;


    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {
        return routeLocatorBuilder.routes()
                .route(r -> r.path("/!{system}/**")
                        .filters(f -> f.filter(refreshRouterFilter))
                        .uri("http://localhost")
                )
                .route(r -> r.path("/!{system}-{group}/**")
                        .filters(f -> f.filter(refreshRouterFilter))
                        .uri("http://localhost")
                )
                .route(r -> r.path("/_{system}/**")
                        .filters(f -> f.filter(refreshRouterFilter))
                        .uri("http://localhost")
                )
                .route(r -> r.path("/_{system}-{group}/**")
                        .filters(f -> f.filter(refreshRouterFilter))
                        .uri("http://localhost")
                )
                .build();
    }
}
