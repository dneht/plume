package net.dloud.platform.gateway.bean;

import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.rpc.service.GenericService;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.dloud.platform.common.gateway.info.InjectionInfo;

import java.util.Map;

/**
 * @author QuDasheng
 * @create 2018-09-25 18:04
 **/
@Data
@AllArgsConstructor
public class InvokeDetailCache {
    /**
     * 参数字段名
     */
    private String[] names;

    /**
     * 参数类型
     */
    private String[] types;

    /**
     * 是否在白名单
     */
    private Boolean whitelist = false;

    /**
     * 缓存时间
     */
    private Integer cacheTime = 0;

    /**
     * 缓存时间
     */
    private Boolean isTrack = false;

    /**
     * 泛化引用
     */
    private ReferenceConfig<GenericService> reference;

    /**
     * 注入参数
     */
    private Map<String, InjectionInfo> injects;
}
