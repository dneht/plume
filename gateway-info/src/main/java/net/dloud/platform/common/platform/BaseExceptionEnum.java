package net.dloud.platform.common.platform;

/**
 * @author QuDasheng
 * @create 2018-09-22 14:00
 **/
public interface BaseExceptionEnum {
    /**
     * 错误码，网关返回
     */
    String getCode();

    /**
     * 错误消息，网关返回
     */
    String getMessage();
}
