package net.dloud.platform.gateway.fork;

import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.extend.StringUtil;
import net.dloud.platform.common.gateway.bean.ApiRequest;
import net.dloud.platform.common.gateway.bean.ApiResponse;
import net.dloud.platform.common.gateway.bean.InvokeRequest;
import net.dloud.platform.common.gateway.bean.RequestInfo;
import net.dloud.platform.extend.constant.PlatformConstants;
import net.dloud.platform.extend.wrapper.AssertWrapper;
import net.dloud.platform.gateway.pack.DubboInvoker;
import net.dloud.platform.gateway.util.ExceptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.List;
import java.util.Map;

import static org.springframework.web.reactive.function.server.RequestPredicates.OPTIONS;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * @author QuDasheng
 * @create 2018-09-22 15:55
 **/
@Slf4j
@Configuration
public class CustomRouterFunction {
    /**
     * 默认服务前缀
     */
    private final String servicePrefix = "_";
    /**
     * 默认服务后缀
     */
    private final String serviceSuffix = "Service";

    @Autowired
    private DubboInvoker dubboInvoker;

    /**
     * 处理请求
     */
    @Bean
    public RouterFunction<ServerResponse> optionsAll() {
        return route(OPTIONS("/**"), request -> ServerResponse.ok().build());
    }

    /**
     * 匹配路径
     */
    @Bean
    public RouterFunction<ServerResponse> apiPath() {
        return route(POST("/{system}/{clazz}/{method}")
                .or(POST("/{system}/_{method}")), request -> ServerResponse.ok().body(
                request.bodyToMono(ApiRequest.class).flatMap(input -> {
                    final String inputGroup = request.queryParam(PlatformConstants.GROUP_PARAM).orElse(PlatformConstants.DEFAULT_GROUP);
                    final Map<String, String> pathVariables = request.pathVariables();
                    String system = pathVariables.get("system");
                    String clazz = pathVariables.get("clazz");
                    String method = pathVariables.get("method");
                    log.info("[GATEWAY] 分组: {} | 路径参数: {}, {}, {}", inputGroup, system, clazz, method);
                    AssertWrapper.isTrue(StringUtil.notBlank(system) && StringUtil.notBlank(method), "调用方法输入错误!");

                    if (StringUtil.isBlank(clazz)) {
                        clazz = StringUtil.firstLowerCase(system) + serviceSuffix;
                    } else {
                        if (clazz.startsWith(servicePrefix)) {
                            clazz = StringUtil.firstLowerCase(system) + StringUtil.firstUpperCase(clazz.substring(1));
                        } else {
                            clazz = StringUtil.firstLowerCase(clazz);
                        }
                        if (!clazz.endsWith(serviceSuffix)) {
                            clazz += serviceSuffix;
                        }
                    }

                    final String invokeName = system + "." + clazz + "." + method;
                    final ServerRequest.Headers headers = request.headers();
                    return dubboInvoker.doApi(new RequestInfo(request.cookies(), headers.asHttpHeaders(), request.remoteAddress().orElse(headers.host())),
                            input.getToken(), input.getTenant(), inputGroup, invokeName, input.getParam());
                }).onErrorResume(ExceptionUtil::handleOne), ApiResponse.class));
    }

    /**
     * 匹配方法名
     */
    @Bean
    public RouterFunction<ServerResponse> apiName() {
        return route(POST("/api"), request -> ServerResponse.ok().body(
                request.bodyToMono(InvokeRequest.class).flatMap(input -> {
                    final String inputGroup = request.queryParam(PlatformConstants.GROUP_PARAM).orElse(PlatformConstants.DEFAULT_GROUP);
                    final List<String> invokeNames = input.getInvoke();
                    final List<Map<String, Object>> inputParams = input.getParam();

                    AssertWrapper.notNull(invokeNames, "调用方法名不能为空");
                    AssertWrapper.notNull(inputParams, "调用方法参数不能为空");
                    AssertWrapper.isTrue(invokeNames.size() > 0, "调用方法名不能为空");
                    AssertWrapper.isTrue(inputParams.size() > 0, "调用方法名不能为空");
                    AssertWrapper.isTrue(invokeNames.size() == inputParams.size(), "调用类型不匹配");
                    final ServerRequest.Headers headers = request.headers();
                    return dubboInvoker.doApi(new RequestInfo(request.cookies(), headers.asHttpHeaders(), request.remoteAddress().orElse(headers.host())),
                            input.getToken(), input.getTenant(), inputGroup, invokeNames, inputParams);
                }).onErrorResume(ExceptionUtil::handleList), ApiResponse.class));
    }
}