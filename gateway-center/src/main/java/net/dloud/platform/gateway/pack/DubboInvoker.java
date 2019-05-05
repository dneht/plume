package net.dloud.platform.gateway.pack;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.extend.CollectionUtil;
import net.dloud.platform.common.extend.StringUtil;
import net.dloud.platform.common.gateway.InjectEnum;
import net.dloud.platform.common.gateway.bean.ApiResponse;
import net.dloud.platform.common.gateway.bean.InvokeKey;
import net.dloud.platform.common.gateway.bean.RequestInfo;
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
import net.dloud.platform.parse.utils.AddressGet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * @author QuDasheng
 * @create 2019-03-28 15:54
 **/
@Slf4j
@Component
public class DubboInvoker {
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
     * 调用单个方法
     */
    public Mono<ApiResponse> doApi(RequestInfo request, String token, String inputTenant, String inputGroup,
                                   String invokeName, Map<String, Object> inputParam) {
        return doApi(request, token, inputTenant, inputGroup, Collections.singletonList(invokeName), Collections.singletonList(inputParam), true);
    }

    /**
     * 调用多个方法
     */
    public Mono<ApiResponse> doApi(RequestInfo request, String token, String inputTenant, String inputGroup,
                                   List<String> invokeNames, List<Map<String, Object>> inputParams) {
        return doApi(request, token, inputTenant, inputGroup, invokeNames, inputParams, false);
    }

    /**
     * 泛化调用方法
     */
    public Mono<ApiResponse> doApi(RequestInfo request, String token, String inputTenant, String inputGroup,
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
                final Object value = methodResultCache.getValue(needKey, inputParam, inputTenant, inputGroup, token);
                if (null == value) {
                    result = doResult(request, invokeName, inputParam, memberInfo, invokeDetailCache, proof);
                    methodResultCache.setValue(needKey, inputParam, inputTenant, inputGroup, token, result, cacheTime);
                } else {
                    result = value;
                }
            } else {
                result = doResult(request, invokeName, inputParam, memberInfo, invokeDetailCache, proof);
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
                log.debug("[GATEWAY] 来源: {} | 分组: {} | 调用方法: {} | 返回结果: {} | 凭证: {}",
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

    private Object doResult(RequestInfo request, String invokeName,
                            Map<String, Object> inputParam, Map<String, Object> memberInfo,
                            InvokeDetailCache cache, String proof) {
        //分割获取方法名
        String methodName = StringUtil.splitLastByDot(invokeName);
        // 获取注入信息
        final Map<String, InjectionInfo> injects = cache.getInjects();

        //拼装输入参数
        final List<Object> invokeParams = new ArrayList<>();
        final String[] invokeCacheNames = cache.getNames();
        for (int j = 0; j < invokeCacheNames.length; j++) {
            String getName = invokeCacheNames[j];
            Object getParam = inputParam.get(getName);
            if (null != injects && injects.keySet().contains(getName)) {
                getParam = doInject(getParam, injects.get(getName), memberInfo, request);
            }
            invokeParams.add(getParam);
        }

        log.debug("[GATEWAY] 将调用的方法参数为: {} = {} | 凭证: {}", methodName, cache, proof);
        Object result = null;
        try {
            result = gatewayCache.referenceConfig(cache.getClassName()).get()
                    .$invoke(methodName, cache.getTypes(), invokeParams.toArray());
        } catch (NoSuchMethodError ex) {
            gatewayCache.referenceClean(cache.getClassName());
        }
        if (null == result) {
            throw new PassedException(PlatformExceptionEnum.RESULT_ERROR);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Object doInject(Object getParam, InjectionInfo info, Map<String, Object> member, RequestInfo request) {
        if (null == info) {
            return null == getParam ? Collections.emptyMap() : getParam;
        }

        if (InjectEnum.getLevel(info.getInjectType()) > 1) {
            return member;
        } else {
            final Object userId = member.get("userId");
            if (getParam instanceof Map) {
                final HttpHeaders headers = request.getHttpHeaders();
                final Map<String, Object> mapParam = (Map) getParam;
                if (null != userId) {
                    mapParam.put("userId", userId);
                }
                if (info.getHaveAddress()) {
                    mapParam.put("requestIp", AddressGet.remoteAddress(request));
                }
                final Set<String> cookieNames = info.getCookieNames();
                if (null != cookieNames && cookieNames.size() > 0) {
                    final MultiValueMap<String, HttpCookie> getCookies = request.getHttpCookies();
                    if (CollectionUtil.notEmpty(getCookies)) {
                        final Map<String, String> retCookies = Maps.newHashMapWithExpectedSize(cookieNames.size());
                        for (String cookieName : cookieNames) {
                            final List<HttpCookie> httpCookies = getCookies.get(cookieName);
                            if (null != httpCookies && !httpCookies.isEmpty()) {
                                retCookies.put(cookieName, httpCookies.get(0).getValue());
                            }
                        }
                        mapParam.put("requestCookies", retCookies);
                    }
                }
                final Set<String> headerNames = info.getHeaderNames();
                if (null != headerNames && headerNames.size() > 0) {
                    final Map<String, byte[]> retHeaders = Maps.newHashMapWithExpectedSize(headerNames.size());
                    for (String headerName : headerNames) {
                        final List<String> httpHeader = headers.get(headerName);
                        if (CollectionUtil.notEmpty(httpHeader)) {
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
