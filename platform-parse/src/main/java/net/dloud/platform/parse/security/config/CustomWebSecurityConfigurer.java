package net.dloud.platform.parse.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * @author QuDasheng
 * @create 2019-01-09 21:12
 **/
@EnableWebSecurity
public class CustomWebSecurityConfigurer {
    @Value("${security.admin.password}")
    private String adminPwd;

    @Value("${security.user.password}")
    private String userPwd;


    @Bean
    public MapReactiveUserDetailsService simpleUserDetailsService() {
        return new MapReactiveUserDetailsService(
                User.withUsername("admin").password(PasswordEncoderFactories
                        .createDelegatingPasswordEncoder().encode(adminPwd)).roles("ADMIN").build(),
                User.withUsername("user").password(PasswordEncoderFactories
                        .createDelegatingPasswordEncoder().encode(userPwd)).roles("USER").build());
    }

    @Configuration
    @ConditionalOnClass({DispatcherHandler.class})
    public class WebfluxSecurityConfigurerAdapter {
        @Bean
        public SecurityWebFilterChain customSecurityFilterChain(ServerHttpSecurity http) {
            return http.authorizeExchange()
                    .matchers(EndpointRequest.to(HealthEndpoint.class, InfoEndpoint.class)).authenticated()
                    .pathMatchers("/run/**", "/admin/**", "/actuator/**").authenticated()
                    .anyExchange().permitAll().and().httpBasic()
                    .and().csrf().disable().build();
        }
    }

    @Configuration
    @ConditionalOnClass({DispatcherServlet.class})
    public class ServletSecurityConfigurerAdapter {
        @Bean
        public WebSecurityConfigurerAdapter webSecurityConfigurerAdapter() {
            return new WebSecurityConfigurerAdapter() {
                @Override
                protected void configure(HttpSecurity http) throws Exception {
                    // 允许所有请求通过，同时定制session策略，不创建和使用Security的session
                    http.authorizeRequests()
                            .mvcMatchers("/run/**", "/admin/**", "/actuator/**").authenticated()
                            .anyRequest().permitAll().and().httpBasic()
                            .and().csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                            .and().httpBasic();
                }
            };
        }
    }
}