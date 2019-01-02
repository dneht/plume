package net.dloud.platform.gateway.conf;

import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.provider.CurrentLimit;
import net.dloud.platform.common.provider.Signature;
import net.dloud.platform.common.provider.ValueMock;
import net.dloud.platform.extend.exception.InnerException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @author QuDasheng
 * @create 2018-12-24 20:06
 **/
@Slf4j
@Configuration
public class ServiceProviderLoader {
    @Bean
    public CurrentLimit getCurrentLimit() {
        return getProvider(CurrentLimit.class);
    }

    @Bean
    public ValueMock getValueMock() {
        return getProvider(ValueMock.class);
    }

    /**
     * 签名方法未实现
     */
    public Signature getSignature() {
        return getProvider(Signature.class);
    }

    private <T> T getProvider(Class<T> clazz) {
        final ServiceLoader<T> serviceLoader = ServiceLoader.load(clazz);
        final Iterator<T> searches = serviceLoader.iterator();
        final T search = searches.hasNext() ? searches.next() : null;

        if (search == null) {
            throw new InnerException(String.format("Interface %s has no provider", clazz.getSimpleName()));
        }
        log.info("Load interface provider {}", search);

        return search;
    }
}
