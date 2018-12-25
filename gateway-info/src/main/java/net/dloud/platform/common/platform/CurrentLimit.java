package net.dloud.platform.common.platform;


import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;

/**
 * @author QuDasheng
 * @create 2018-12-24 17:11
 **/
public interface CurrentLimit<T> {
    /**
     * 获取限流使用的key
     *
     * @param request 请求信息
     * @param member  用户信息
     * @return
     */
    T getKey(ServerRequest request, Map<String, Object> member);

    /**
     * 消费token
     *
     * @param request   请求信息
     * @param member    用户信息
     * @return
     */
    boolean tryConsume(ServerRequest request, Map<String, Object> member);
}
