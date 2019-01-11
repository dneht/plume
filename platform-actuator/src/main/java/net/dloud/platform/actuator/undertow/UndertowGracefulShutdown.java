package net.dloud.platform.actuator.undertow;

import io.undertow.Undertow;
import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.GracefulShutdownHandler;
import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.actuator.ShutdownContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.boot.web.embedded.undertow.UndertowWebServer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

/**
 * @author QuDasheng
 * @create 2019-01-09 10:57
 **/
@Slf4j
@Component
@ConditionalOnClass(Undertow.class)
public class UndertowGracefulShutdown implements ShutdownContainer, HandlerWrapper {
    private volatile boolean closed = false;

    private volatile GracefulShutdownHandler gracefulShutdownHandler;

    @Autowired
    private WebServerApplicationContext applicationContext;


    @Override
    public HttpHandler wrap(final HttpHandler handler) {
        if (gracefulShutdownHandler == null) {
            gracefulShutdownHandler = new GracefulShutdownHandler(handler);
        }
        return this.gracefulShutdownHandler;
    }

    @Override
    public boolean closed() {
        return closed;
    }

    @Override
    public void shutdown(ContextClosedEvent event) {
        closed = true;
        try {
            gracefulShutdownHandler.shutdown();
            gracefulShutdownHandler.awaitShutdown(60 * 1000);
        } catch (NullPointerException ex) {
            try {
                final UndertowWebServer webServer = (UndertowWebServer) applicationContext.getWebServer();
                final Field field = webServer.getClass().getDeclaredField("undertow");
                field.setAccessible(true);
                final Undertow undertow = (Undertow) field.get(webServer);
                undertow.getWorker().awaitTermination(60, TimeUnit.SECONDS);
                undertow.stop();
            } catch (Exception e) {
                log.warn("Undertow did not shut down gracefully. Proceeding check:  ", e);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
