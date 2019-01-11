package net.dloud.platform.actuator;

import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.actuator.tomcat.TomcatGracefulShutdown;
import net.dloud.platform.actuator.undertow.UndertowGracefulShutdown;
import net.dloud.platform.extend.exception.InnerException;
import org.apache.catalina.startup.Tomcat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.embedded.tomcat.TomcatReactiveWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.embedded.undertow.UndertowReactiveWebServerFactory;
import org.springframework.boot.web.embedded.undertow.UndertowWebServer;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.reactive.socket.client.UndertowWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import org.springframework.web.servlet.DispatcherServlet;
import org.xnio.OptionMap;
import org.xnio.Options;
import org.xnio.Xnio;

import java.io.IOException;

/**
 * @author QuDasheng
 * @create 2018-11-28 00:28
 **/
@Slf4j
@Configuration
public class WebServerInitialize {
    @Value("${server.undertow.io-threads:4}")
    private Integer ioThreads;

    @Value("${server.undertow.worker-threads:16}")
    private Integer workerThreads;


    @Configuration
    @ConditionalOnClass({DispatcherServlet.class, Tomcat.class, TomcatWebServer.class})
    public class InitTomcatServletWebServerFactory {
        @Bean
        public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer(TomcatGracefulShutdown tomcatShutdown) {
            return factory -> factory.addConnectorCustomizers(tomcatShutdown);
        }
    }

    @Configuration
    @ConditionalOnClass({DispatcherHandler.class, Tomcat.class, TomcatWebServer.class})
    public class InitTomcatReactiveWebServerFactory {
        @Bean
        public WebServerFactoryCustomizer<TomcatReactiveWebServerFactory> tomcatCustomizer(TomcatGracefulShutdown tomcatShutdown) {
            return factory -> factory.addConnectorCustomizers(tomcatShutdown);
        }
    }

    @Configuration
    @ConditionalOnClass({DispatcherHandler.class, Undertow.class, UndertowWebServer.class})
    public class InitUndertowReactiveWebServerFactory {
        @Bean
        public WebServerFactoryCustomizer<UndertowReactiveWebServerFactory> undertowCustomizer(UndertowGracefulShutdown undertowShutdown) {
            return (factory) -> {
                factory.addDeploymentInfoCustomizers((builder) -> builder.addInitialHandlerChainWrapper(undertowShutdown));
                factory.addBuilderCustomizers(builder -> builder.setServerOption(UndertowOptions.ENABLE_STATISTICS, true));
            };
        }
    }

    @Configuration
    @ConditionalOnClass({DispatcherHandler.class, Undertow.class, UndertowWebSocketClient.class})
    public class InitUndertowWebSocketClient {
        @Bean
        public WebSocketClient webSocketClient() {
            Xnio xnio = Xnio.getInstance(Undertow.class.getClassLoader());
            try {
                return new UndertowWebSocketClient(xnio.createWorker(OptionMap.builder()
                        .set(Options.WORKER_IO_THREADS, ioThreads)
                        .set(Options.CONNECTION_HIGH_WATER, 1000000)
                        .set(Options.CONNECTION_LOW_WATER, 1000000)
                        .set(Options.WORKER_TASK_CORE_THREADS, workerThreads)
                        .set(Options.WORKER_TASK_MAX_THREADS, workerThreads)
                        .set(Options.USE_DIRECT_BUFFERS, true)
                        .set(Options.TCP_NODELAY, true)
                        .set(Options.CORK, true)
                        .getMap()));
            } catch (IOException ex) {
                log.error("[PLATFORM] 初始化容器失败: ", ex);
                throw new InnerException("初始化容器失败", ex);
            }
        }
    }
}
