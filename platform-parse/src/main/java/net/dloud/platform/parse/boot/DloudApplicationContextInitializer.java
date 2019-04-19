package net.dloud.platform.parse.boot;

import com.ctrip.framework.apollo.spring.config.PropertySourcesConstants;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.extend.StringUtil;
import net.dloud.platform.common.extend.ValidatorUtil;
import net.dloud.platform.extend.constant.StartupConstants;
import net.dloud.platform.extend.exception.InnerException;
import net.dloud.platform.parse.utils.AddressGet;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;

/**
 * @author QuDasheng
 * @create 2018-10-03 14:39
 **/
@Slf4j
public class DloudApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    public static final String BOOTSTRAP_PROPERTY_SOURCE_NAME = "DloudBootstrapPropertySources";


    @Override
    public void initialize(ConfigurableApplicationContext context) {
        // set file encoding
        System.setProperty("file.encoding", StandardCharsets.UTF_8.name());
        System.setProperty("log4j2.asyncLoggerConfigRingBufferSize", String.valueOf(1024 * 1024));

        final String pub = System.getProperty("pub");
        if (StringUtil.isBlank(pub)) {
            StartupConstants.IS_PUBLIC = false;
            StartupConstants.RUN_HOST = AddressGet.getByDubbo();
        } else {
            StartupConstants.IS_PUBLIC = Boolean.parseBoolean(pub);
            if (StartupConstants.IS_PUBLIC) {
                final InetAddress local = AddressGet.getLocal();
                StartupConstants.RUN_HOST = null == local ? AddressGet.getByDubbo() : local.getHostName();
            } else {
                StartupConstants.RUN_HOST = AddressGet.getByDubbo();
            }
        }
        StartupConstants.RUN_MODE = System.getProperty("mode", "dev");
        StartupConstants.RUN_LIBS = System.getProperty("lib.path");

        final ConfigurableEnvironment environment = context.getEnvironment();
        if (environment.getPropertySources().contains(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME)) {
            // apollo already initialized
            final String port = System.getProperty("port");
            if (null == port) {
                StartupConstants.SERVER_PORT = Integer.parseInt(System.getProperty("http", "20000"));
                StartupConstants.DUBBO_PORT = Integer.parseInt(System.getProperty("dubbo", "20880"));
                log.warn("App port will be default value");
            } else {
                final String[] portSplit = port.split("-");
                if (portSplit.length < 1) {
                    StartupConstants.SERVER_PORT = Integer.parseInt(port);
                    StartupConstants.DUBBO_PORT = 20880;
                } else {
                    // port range default is 100
                    final int portStart = Integer.parseInt(portSplit[0]);
                    final int portEnd = portSplit.length == 2 ? Integer.parseInt(portSplit[1]) : portStart + 100;
                    log.info("App port range from {} to {}", portStart, portEnd);

                    int availableCount = 0;
                    int[] availablePorts = new int[2];
                    for (int one = portStart; one < portEnd; one++) {
                        try {
                            final ServerSocket server = new ServerSocket(one);
                            availablePorts[availableCount] = server.getLocalPort();
                            availableCount += 1;
                            server.close();
                            if (availableCount >= 2) {
                                break;
                            }
                        } catch (IOException e) {
                            log.debug("Port {} is already used", one);
                        }
                    }
                    StartupConstants.SERVER_PORT = availablePorts[0];
                    StartupConstants.DUBBO_PORT = availablePorts[1];
                }
            }

            environment.getPropertySources().addFirst(new MapPropertySource(BOOTSTRAP_PROPERTY_SOURCE_NAME, ImmutableMap.of(
                    "run.mode", StartupConstants.RUN_MODE, "run.host", StartupConstants.RUN_HOST,
                    "server.port", StartupConstants.SERVER_PORT, "dubbo.port", StartupConstants.DUBBO_PORT)));
            log.info("This time run.mode={} | run.host={} | server.port={} | dubbo.port = {}",
                    StartupConstants.RUN_MODE, StartupConstants.RUN_HOST, StartupConstants.SERVER_PORT, StartupConstants.DUBBO_PORT);
        } else {
            StartupConstants.SERVER_PORT = Integer.parseInt(environment.getProperty("server.port", "20000"));
            StartupConstants.DUBBO_PORT = Integer.parseInt(environment.getProperty("dubbo.port", "20880"));
        }
    }
}
