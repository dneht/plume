package net.dloud.platform.parse.utils;

import com.alibaba.dubbo.rpc.RpcContext;
import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.extend.CollectionUtil;
import net.dloud.platform.common.gateway.bean.RequestInfo;
import net.dloud.platform.common.network.IPConvert;
import net.dloud.platform.extend.client.LocalHttpClient;
import net.dloud.platform.extend.constant.RequestHeaderEnum;
import org.apache.http.client.methods.RequestBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static javax.swing.UIManager.get;

/**
 * @author QuDasheng
 * @create 2018-09-02 18:25
 **/
@Slf4j
public class AddressGet {
    private static String LOCAL_IP;
    private static String PUBLIC_IP;


    public static InetAddress getLocal() {
        try {
            final InetAddress address = InetAddress.getLocalHost();
            log.info("获取本机网络信息: {}", address);
            if (null != address) {
                return address;
            }
        } catch (UnknownHostException e) {
            log.info("获取本机网络信息失败: {}", e.getMessage());
        }
        return null;
    }

    public static String getPublic() {
        if (null == PUBLIC_IP) {
            try {
                final Map result = LocalHttpClient.executeJsonResult(RequestBuilder.get()
                        .setUri("http://httpbin.org/ip").build(), Map.class);
                log.info("获取本机公网信息: {}", result);
                if (null != result) {
                    final String origin = String.valueOf(result.get("origin"));
                    if (origin.contains(",")) {
                        PUBLIC_IP = origin.split(",")[0].trim();
                    } else {
                        PUBLIC_IP = origin;
                    }
                }
            } catch (Exception e) {
                log.info("获取本机公网信息失败: {}", e.getMessage());
            }
        }
        return PUBLIC_IP;
    }

    public static String getByDubbo() {
        if (null == LOCAL_IP) {
            LOCAL_IP = RpcContext.getContext().getLocalHost();
        }
        return LOCAL_IP;
    }

    public static InetAddress getByName(String domain) {
        try {
            final InetAddress address = InetAddress.getByAddress(domain.getBytes());
            log.info("获取[{}]网络信息: {}", domain, address);
            return address;
        } catch (UnknownHostException e) {
            log.info("获取[{}]网络信息失败: {}", domain, e.getMessage());
        }
        return null;
    }

    public static int remoteAddress(RequestInfo request) {
        final HttpHeaders headers = request.getHttpHeaders();
        final List<String> realIP = null == headers ? Collections.emptyList() : headers.get(RequestHeaderEnum.X_REAL_IP.value());
        if (CollectionUtil.notEmpty(realIP)) {
            return IPConvert.ip2Num(realIP.get(0));
        } else {
            final InetSocketAddress remoteAddress = request.getRemoteAddress();
            if (null == remoteAddress || null == remoteAddress.getAddress()) {
                return 0;
            } else {
                return IPConvert.ip2Num(remoteAddress.getAddress().getHostAddress());
            }
        }
    }

    public static int remoteAddress(ServerRequest request) {
        final ServerRequest.Headers headers = request.headers();
        final List<String> realIP = headers.header(RequestHeaderEnum.X_REAL_IP.value());
        if (!realIP.isEmpty()) {
            return IPConvert.ip2Num(realIP.get(0));
        } else {
            final InetSocketAddress remoteAddress = headers.host();
            if (null == remoteAddress || null == remoteAddress.getAddress()) {
                return 0;
            } else {
                return IPConvert.ip2Num(remoteAddress.getAddress().getHostAddress());
            }
        }
    }
}
