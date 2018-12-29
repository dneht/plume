package net.dloud.platform.gateway.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author QuDasheng
 * @create 2018-10-04 10:27
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvokeCache {
    /**
     * 是否全是白名单
     */
    private boolean whitelist = false;

    /**
     * 返回信息最多调用名
     */
    private String invokeName;

    /**
     * 需要使用缓存的方法
     */
    private Map<String, Integer> needCaches;

    /**
     * 方法缓存
     */
    private List<InvokeDetailCache> invokeDetails;
}
