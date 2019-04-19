package net.dloud.platform.gateway.spi;

import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.listener.ExporterListenerAdapter;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;

import static net.dloud.platform.gateway.pack.GatewayCache.getReferenceCache;

/**
 * @author QuDasheng
 * @create 2018-09-02 13:08
 **/
@Slf4j
@Activate(value = "unExporterListener", order = 100)
public class UnExporterListener extends ExporterListenerAdapter {

    @Override
    public void unexported(Exporter<?> exporter) throws RpcException {
        final Cache<String, ReferenceConfig<GenericService>> referenceCache = getReferenceCache();
        final String clazzName = exporter.getInvoker().getInterface().getName();
        log.info("[GATEWAY] 网关缓存[{}]因失效将被移除", clazzName);
        referenceCache.invalidate(clazzName);
    }
}
