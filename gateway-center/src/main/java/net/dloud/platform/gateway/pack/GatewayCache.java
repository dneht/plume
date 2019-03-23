package net.dloud.platform.gateway.pack;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ConsumerConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.extend.CollectionUtil;
import net.dloud.platform.common.extend.StringUtil;
import net.dloud.platform.common.gateway.InjectEnum;
import net.dloud.platform.common.gateway.bean.InvokeKey;
import net.dloud.platform.common.gateway.bean.TokenKey;
import net.dloud.platform.common.gateway.info.InjectionInfo;
import net.dloud.platform.common.serialize.KryoBaseUtil;
import net.dloud.platform.dal.InfoComponent;
import net.dloud.platform.dal.entity.InfoMethodGateway;
import net.dloud.platform.extend.constant.PlatformConstants;
import net.dloud.platform.extend.constant.PlatformExceptionEnum;
import net.dloud.platform.extend.exception.PassedException;
import net.dloud.platform.gateway.bean.InvokeDetailCache;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static net.dloud.platform.parse.dubbo.wrapper.DubboWrapper.dubboProvider;

/**
 * @author QuDasheng
 * @create 2018-12-29 17:14
 **/
@Slf4j
@Component
public class GatewayCache {
    /**
     * 缓存服务和查询结果
     */
    private static Cache<InvokeKey, InvokeDetailCache> genericCache;
    /**
     * 用户信息相关
     */
    private static Cache<TokenKey, Map<String, Object>> tokenCache;

    /**
     * 缓存配置
     */
    @Value("${cache.expire-after-write:30}")
    private int expireAfterWrite;
    @Value("${cache.refresh-after-write:10}")
    private int refreshAfterWrite;
    @Value("${default.field-filter}")
    private String fieldFilter;

    @Autowired
    private Jdbi jdbi;

    /**
     * dubbo配置
     */
    @Autowired
    private ApplicationConfig applicationConfig;
    @Autowired
    private RegistryConfig registryConfig;
    @Autowired
    private ConsumerConfig consumerConfig;

    @Autowired
    private InfoComponent infoComponent;


    @PostConstruct
    private void init() {
        if (null == genericCache) {
            genericCache = Caffeine.newBuilder().maximumSize(1_000)
                    .expireAfterWrite(expireAfterWrite, TimeUnit.MINUTES)
                    .refreshAfterWrite(refreshAfterWrite, TimeUnit.MINUTES)
                    .removalListener(this::invokeRemove)
                    .build(this::invokeCache);
        }
        if (null == tokenCache) {
            tokenCache = Caffeine.newBuilder().maximumSize(100_000)
                    .expireAfterWrite(expireAfterWrite, TimeUnit.MINUTES)
                    .refreshAfterWrite(refreshAfterWrite, TimeUnit.MINUTES)
                    .build(this::tokenCache);
        }
    }

    public InvokeDetailCache invokeCache(InvokeKey key) {
        return invokeCache(key, false);
    }

    public InvokeDetailCache invokeCache(InvokeKey key, boolean token) {
        InvokeDetailCache present = genericCache.getIfPresent(key);
        if (null == present) {
            final String inputGroup = key.getGroup();
            final String invokeName = key.getInvoke();
            final int invokeSize = key.getLength();

            final InfoMethodGateway methodSimple = jdbi.withHandle(handle ->
                    infoComponent.getGatewayMethod(handle, inputGroup, invokeName, invokeSize))
                    .orElseThrow(() -> new PassedException(PlatformExceptionEnum.NOT_FOUND));

            //拼装方法名和类型
            List<String> names = Lists.newArrayListWithExpectedSize(invokeSize + 1);
            List<String> types = Lists.newArrayListWithExpectedSize(invokeSize + 1);
            if (null != methodSimple.getSimpleParameter()) {
                //这是一个链表
                final Map<String, String> simpleParam = KryoBaseUtil.readFromByteArray(methodSimple.getSimpleParameter());
                for (Map.Entry<String, String> one : simpleParam.entrySet()) {
                    names.add(one.getKey());
                    types.add(one.getValue());
                }
            }
            Map<String, InjectionInfo> injectParam = Maps.newHashMapWithExpectedSize(invokeSize);
            if (!token) {
                log.info("[GATEWAY}] 当前输入参数对应的名称和类型: {}", names, types);

                if (null != methodSimple.getInjectionInfo()) {
                    injectParam = KryoBaseUtil.readFromByteArray(methodSimple.getInjectionInfo());
                    log.info("[GATEWAY] 当前要通过网关注入的参数: {}", injectParam);
                } else {
                    log.info("[GATEWAY] 当前没有需要通过网关注入的参数");
                }
            }

            ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
            reference.setInterface(methodSimple.getClazzName());
            reference.setApplication(applicationConfig);
            reference.setRegistry(registryConfig);
            reference.setConsumer(consumerConfig);
            reference.setGeneric(true);

            present = new InvokeDetailCache(names.toArray(new String[0]), types.toArray(new String[0]),
                    methodSimple.getIsWhitelist(), methodSimple.getCacheTime(), methodSimple.getIsTrack(), reference, injectParam);
            genericCache.put(key, present);
        }
        return present;
    }

    public void invokeRemove(InvokeKey key, InvokeDetailCache cache, RemovalCause cause) {
        log.info("[GATEWAY] 网关缓存[{}]将被移除[{}]", key, cause);
        final ReferenceConfig<GenericService> reference = cache.getReference();
        if (null != reference) {
            reference.destroy();
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> tokenCache(TokenKey key) {
        //如果token存在则进行之后的操作
        final String token = key.getToken();
        if (StringUtil.isBlank(token) || (token.length() != 64 && token.length() != 128)) {
            return Collections.emptyMap();
        }
        if (StringUtil.isBlank(key.getInject())) {
            key.setInject(InjectEnum.MEMBER_ID.method());
        }

        Map<String, Object> present = tokenCache.getIfPresent(key);
        if (null == present) {
            //先给定一个默认值
            present = Collections.emptyMap();
            String proof = RpcContext.getContext().getAttachment(PlatformConstants.PROOF_KEY);
            String inject = key.getInject();
            if (StringUtil.isBlank(inject)) {
                inject = InjectEnum.MEMBER_ID.method();
            }
            log.info("[GATEWAY] 当前校验等级: {}", inject);

            try {
                final InvokeDetailCache invokeDetailCache = invokeCache(new InvokeKey(key.getGroup(), inject, 1), true);
                present = (Map) invokeDetailCache.getReference().get().$invoke(StringUtil.splitLastByDot(inject),
                        invokeDetailCache.getTypes(), new Object[]{token});
                log.info("[GATEWAY] 用户获取完毕, 信息: {}", present);

                tokenCache.put(key, present);
            } catch (PassedException e) {
                log.warn("[GATEWAY] {}", e.getMessage());

                tokenCache.put(key, present);
            } catch (Exception e) {
                log.warn("[GATEWAY] 获取用户信息出错: ", e);
            } finally {
                setRpcContext(key.getTenant(), key.getGroup(), proof);
            }
        }
        return present;
    }

    public void setRpcContext(String inputTenant, String inputGroup, String proof) {
        final RpcContext context = RpcContext.getContext();
        context.setAttachment(PlatformConstants.PROOF_KEY, proof);
        context.setAttachment(PlatformConstants.SUBGROUP_KEY, inputGroup);
        context.setAttachment(PlatformConstants.FROM_KEY, inputTenant);
        if (!PlatformConstants.DEFAULT_GROUP.equals(inputGroup) && CollectionUtil.notEmpty(dubboProvider.get(inputGroup))) {
            context.setAttachment(PlatformConstants.HANDGROUP_KEY, KryoBaseUtil.writeObjectToString(dubboProvider.get(inputGroup)));
        }
    }

    public Cache<InvokeKey, InvokeDetailCache> getGenericCache() {
        return genericCache;
    }

    public Cache<TokenKey, Map<String, Object>> getTokenCache() {
        return tokenCache;
    }
}
