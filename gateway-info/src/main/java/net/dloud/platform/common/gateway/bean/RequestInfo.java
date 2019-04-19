package net.dloud.platform.common.gateway.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

import java.net.InetSocketAddress;

/**
 * @author QuDasheng
 * @create 2019-01-02 20:09
 **/
@Data
@AllArgsConstructor
public class RequestInfo {
    /**
     * websocket无法获得cookie
     */
    private MultiValueMap<String, HttpCookie> httpCookies;

    /**
     * header有两种类型,但可以转化
     */
    private HttpHeaders httpHeaders;

    /**
     * 请求地址
     */
    private InetSocketAddress remoteAddress;
}
