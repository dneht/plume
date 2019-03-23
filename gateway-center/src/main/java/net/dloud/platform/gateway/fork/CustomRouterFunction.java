package net.dloud.platform.gateway.fork;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.extend.StringUtil;
import net.dloud.platform.common.gateway.InjectEnum;
import net.dloud.platform.common.gateway.bean.ApiRequest;
import net.dloud.platform.common.gateway.bean.ApiResponse;
import net.dloud.platform.common.gateway.bean.InvokeKey;
import net.dloud.platform.common.gateway.bean.InvokeRequest;
import net.dloud.platform.common.gateway.bean.TokenKey;
import net.dloud.platform.common.gateway.info.InjectionInfo;
import net.dloud.platform.common.provider.CurrentLimit;
import net.dloud.platform.extend.constant.PlatformConstants;
import net.dloud.platform.extend.constant.PlatformExceptionEnum;
import net.dloud.platform.extend.exception.PassedException;
import net.dloud.platform.extend.exception.RefundException;
import net.dloud.platform.extend.wrapper.AssertWrapper;
import net.dloud.platform.gateway.bean.InvokeCache;
import net.dloud.platform.gateway.bean.InvokeDetailCache;
import net.dloud.platform.gateway.pack.GatewayCache;
import net.dloud.platform.gateway.pack.MethodResultCache;
import net.dloud.platform.gateway.util.ExceptionUtil;
import net.dloud.platform.gateway.util.LimitUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpCookie;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

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
    /**
     * 默认方法分隔符
     */
    private final String methodSplit = "|";

    @Autowired
    private CurrentLimit currentLimit;

    @Autowired
    private GatewayCache gatewayCache;

    @Autowired
    private MethodResultCache methodResultCache;


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
                    final String inputGroup = request.queryParam(PlatformConstants.GROUP_KEY).orElse(PlatformConstants.DEFAULT_GROUP);
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
                    return doApi(request, input.getToken(), input.getTenant(), inputGroup, invokeName, input.getParam());
                }).onErrorResume(ExceptionUtil::handleOne), ApiResponse.class));
    }

    /**
     * 匹配方法名
     */
    @Bean
    public RouterFunction<ServerResponse> apiName() {
        return route(POST("/api"), request -> ServerResponse.ok().body(
                request.bodyToMono(InvokeRequest.class).flatMap(input -> {
                    final String inputGroup = request.queryParam(PlatformConstants.GROUP_KEY).orElse(PlatformConstants.DEFAULT_GROUP);
                    final List<String> invokeNames = input.getInvoke();
                    final List<Map<String, Object>> inputParams = input.getParam();

                    AssertWrapper.notNull(invokeNames, "调用方法名不能为空");
                    AssertWrapper.notNull(inputParams, "调用方法参数不能为空");
                    AssertWrapper.isTrue(invokeNames.size() > 0, "调用方法名不能为空");
                    AssertWrapper.isTrue(inputParams.size() > 0, "调用方法名不能为空");
                    AssertWrapper.isTrue(invokeNames.size() == inputParams.size(), "调用类型不匹配");
                    return doApi(request, input.getToken(), input.getTenant(), inputGroup, invokeNames, inputParams);
                }).onErrorResume(ExceptionUtil::handleList), ApiResponse.class));
    }

    /**
     * 调用单个方法
     */
    private Mono<ApiResponse> doApi(ServerRequest request, String token, String inputTenant, String inputGroup,
                                    String invokeName, Map<String, Object> inputParam) {
        return doApi(request, token, inputTenant, inputGroup, Collections.singletonList(invokeName), Collections.singletonList(inputParam), true);
    }

    /**
     * 调用多个方法
     */
    private Mono<ApiResponse> doApi(ServerRequest request, String token, String inputTenant, String inputGroup,
                                    List<String> invokeNames, List<Map<String, Object>> inputParams) {
        return doApi(request, token, inputTenant, inputGroup, invokeNames, inputParams, false);
    }

    /**
     * 泛化调用方法
     */
    private Mono<ApiResponse> doApi(ServerRequest request, String token, String inputTenant, String inputGroup,
                                    List<String> invokeNames, List<Map<String, Object>> inputParams, boolean fromPath) {
        final String proof = UUID.randomUUID().toString();
        final ApiResponse response = new ApiResponse(true, proof);
        log.info("[GATEWAY] 来源: {} | 分组: {} | 调用方法: {} | 输入参数: {} | 凭证: {}",
                inputTenant, inputGroup, invokeNames, inputParams, proof);
        AssertWrapper.isTrue(null != inputTenant && null != inputGroup, "调用方法输入错误!");
        gatewayCache.setRpcContext(inputTenant, inputGroup, proof);

        int size = invokeNames.size();
        InvokeCache paramCache = doCache(inputGroup, inputParams, invokeNames);
        //校验白名单及用户信息
        Map<String, Object> memberInfo = doMember(inputTenant, inputGroup, paramCache, token);

        //用户或ip维度的限流
        if (!currentLimit.tryConsume(request, memberInfo)) {
            throw new PassedException(PlatformExceptionEnum.API_ACCESS_LIMIT);
        }

        final Map<String, Integer> needs = paramCache.getNeedCaches();
        final List<InvokeDetailCache> caches = paramCache.getInvokeDetails();
        final Mono<List<Object>> outcome = Flux.fromStream(() -> IntStream.range(0, size).mapToObj(i -> {
            final String invokeName = invokeNames.get(i);
            final Map<String, Object> inputParam = null == inputParams.get(i) ? Collections.emptyMap() : inputParams.get(i);
            final InvokeDetailCache invokeDetailCache = caches.get(i);
            AssertWrapper.notNull(invokeName, "调用方法名不能为空");
            AssertWrapper.notNull(invokeDetailCache, "调用方法名未找到");

            final Object result;
            //处理网关缓存
            final String needKey = invokeName + methodSplit + inputParam.size();
            final Integer cacheTime = needs.getOrDefault(needKey, 0);
            if (cacheTime > 0) {
                final Object value = methodResultCache.getValue(needKey, inputParam, inputTenant, inputGroup);
                if (null == value) {
                    result = doResult(request, invokeName, inputParam, memberInfo, invokeDetailCache);
                    methodResultCache.setValue(needKey, inputParam, inputTenant, inputGroup, result, cacheTime);
                } else {
                    result = value;
                }
            } else {
                result = doResult(request, invokeName, inputParam, memberInfo, invokeDetailCache);
            }
            return result;
        })).collectList();

        final Mono<ApiResponse> result;
        if (response.getCode() == PlatformConstants.CORRECT_CODE) {
            result = outcome.map(list -> {
                if (fromPath) {
                    if (list.isEmpty()) {
                        response.exception(PlatformExceptionEnum.RESULT_ERROR);
                    } else {
                        response.setPreload(list.get(0));
                    }
                } else {
                    response.setPreload(list);
                }
                if (!fromPath && response.getCode() != 0) {
                    response.setPreload(Collections.singleton(response.getPreload()));
                }
                log.info("[GATEWAY] 来源: {} | 分组: {} | 调用方法: {} | 返回结果: {} | 凭证: {}",
                        inputTenant, inputGroup, invokeNames, response.getPreload(), proof);

                return response;
            });
        } else {
            response.setProof(proof);
            result = Mono.just(response);
        }
        return result;
    }

    private Map<String, Object> doMember(String inputTenant, String inoutGroup, InvokeCache paramCache, String token) {
        Map<String, Object> memberInfo = gatewayCache.tokenCache(new TokenKey(token, inputTenant, inoutGroup, paramCache.getInvokeName()));
        if (!paramCache.isWhitelist()) {
            if (StringUtil.isBlank(token)) {
                throw new RefundException(PlatformExceptionEnum.LOGIN_NONE);
            }
            if (memberInfo.isEmpty() || memberInfo.containsKey("code")) {
                throw new RefundException(PlatformExceptionEnum.LOGIN_EXPIRE);
            }
        }
        return memberInfo;
    }

    private InvokeCache doCache(String inputGroup, List<Map<String, Object>> inputParams,
                                List<String> invokeNames) {
        final int size = invokeNames.size();

        boolean whitelist = true;
        int invokeLevel = 0;
        String invokeMember = null;
        List<InvokeDetailCache> invokeDetails = Lists.newArrayListWithExpectedSize(size);
        Map<String, Integer> needCaches = Maps.newHashMapWithExpectedSize(size);

        for (int i = 0; i < size; i++) {
            String invokeName = invokeNames.get(i);
            int paramSize = 0;
            if (null != inputParams.get(i)) {
                paramSize = inputParams.get(i).size();
            }

            //获取缓存的数据
            final InvokeDetailCache invokeDetail = gatewayCache.invokeCache(new InvokeKey(inputGroup, invokeName, paramSize));
            if (whitelist && !invokeDetail.getWhitelist()) {
                log.info("[GATEWAY] 方法 {} 没有设置白名单", invokeName);
                whitelist = false;
            }
            if (null != invokeDetail.getCacheTime() && invokeDetail.getCacheTime() > 0) {
                log.info("[GATEWAY] 方法 {}|{} 将使用网关缓存", invokeName, paramSize);
                needCaches.put(invokeName + methodSplit + paramSize, invokeDetail.getCacheTime());
            }
            final Map<String, InjectionInfo> injects = invokeDetail.getInjects();
            if (null != injects) {
                for (InjectionInfo inject : injects.values()) {
                    final int newLevel = InjectEnum.getLevel(inject.getInjectType());
                    if (newLevel > invokeLevel) {
                        invokeLevel = newLevel;
                        invokeMember = inject.getInvokeName();
                    }
                }
            }

            invokeDetails.add(invokeDetail);
        }
        return new InvokeCache(whitelist, invokeMember, needCaches, invokeDetails);
    }

    private Object doResult(ServerRequest request, String invokeName, Map<String, Object> inputParam, Map<String, Object> memberInfo,
                            InvokeDetailCache invokeDetailCache) {
        //分割获取方法名
        String methodName = StringUtil.splitLastByDot(invokeName);
        // 获取注入信息
        final Map<String, InjectionInfo> injects = invokeDetailCache.getInjects();

        //拼装输入参数
        final List<Object> invokeParams = new ArrayList<>();
        final String[] invokeCacheNames = invokeDetailCache.getNames();
        for (int j = 0; j < invokeCacheNames.length; j++) {
            String getName = invokeCacheNames[j];
            Object getParam = inputParam.get(getName);
            if (null != injects && injects.keySet().contains(getName)) {
                getParam = doInject(getParam, injects.get(getName), memberInfo, request);
            }
            invokeParams.add(getParam);
        }

        Object result = invokeDetailCache.getReference().get().$invoke(methodName, invokeDetailCache.getTypes(), invokeParams.toArray());
        if (null == result) {
            throw new PassedException(PlatformExceptionEnum.RESULT_ERROR);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Object doInject(Object getParam, InjectionInfo info, Map<String, Object> member, ServerRequest request) {
        if (null == info) {
            return null == getParam ? Collections.emptyMap() : getParam;
        }

        if (InjectEnum.getLevel(info.getInjectType()) > 1) {
            return member;
        } else {
            final Object userId = member.get("userId");
            if (getParam instanceof Map) {
                final ServerRequest.Headers headers = request.headers();
                final Map<String, Object> mapParam = (Map) getParam;
                if (null != userId) {
                    mapParam.put("userId", userId);
                }
                if (info.getHaveAddress()) {
                    mapParam.put("requestIp", LimitUtil.remoteAddress(request));
                }
                final Set<String> cookieNames = info.getCookieNames();
                if (null != cookieNames && cookieNames.size() > 0) {
                    final MultiValueMap<String, HttpCookie> getCookies = request.cookies();
                    final Map<String, String> retCookies = Maps.newHashMapWithExpectedSize(cookieNames.size());
                    for (String cookieName : cookieNames) {
                        final List<HttpCookie> httpCookies = getCookies.get(cookieName);
                        if (null != httpCookies && !httpCookies.isEmpty()) {
                            retCookies.put(cookieName, httpCookies.get(0).getValue());
                        }
                    }
                    mapParam.put("requestCookies", retCookies);
                }
                final Set<String> headerNames = info.getHeaderNames();
                if (null != headerNames && headerNames.size() > 0) {
                    final Map<String, byte[]> retHeaders = Maps.newHashMapWithExpectedSize(headerNames.size());
                    for (String headerName : headerNames) {
                        final List<String> httpHeader = headers.header(headerName);
                        if (!httpHeader.isEmpty()) {
                            retHeaders.put(headerName, httpHeader.get(0).getBytes());
                        }
                    }

                    mapParam.put("requestHeaders", retHeaders);
                }
                return mapParam;
            } else {
                return userId;
            }
        }
    }
}
