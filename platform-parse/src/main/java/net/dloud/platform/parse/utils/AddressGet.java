package net.dloud.platform.parse.utils;

import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.network.IPConvert;
import net.dloud.platform.extend.constant.RequestHeaderEnum;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * @author QuDasheng
 * @create 2018-09-02 18:25
 **/
@Slf4j
public class AddressGet {
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
