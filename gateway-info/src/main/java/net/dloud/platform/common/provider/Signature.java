package net.dloud.platform.common.provider;

import net.dloud.platform.common.gateway.bean.ApiRequest;
import net.dloud.platform.common.gateway.bean.InvokeRequest;

/**
 * @author QuDasheng
 * @create 2018-12-24 16:51
 **/
public interface Signature {
    /**
     * 签名方法
     *
     * @param request
     * @return
     */
    String sign(InvokeRequest request);

    /**
     * 签名方法
     *
     * @param method  调用的方法
     * @param request
     * @return
     */
    String sign(String method, ApiRequest request);

    /**
     * 校验签名
     *
     * @param request
     * @param input   输入的签名
     * @return
     */
    boolean verify(InvokeRequest request, String input);

    /**
     * 校验签名
     *
     * @param request
     * @param input   输入的签名
     * @return
     */
    boolean verify(String method, ApiRequest request, String input);
}
