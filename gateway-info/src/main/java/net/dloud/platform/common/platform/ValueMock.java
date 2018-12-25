package net.dloud.platform.common.platform;

import java.util.Map;

/**
 * @author QuDasheng
 * @create 2018-12-24 16:50
 **/
public interface ValueMock {
    /**
     * 输入参数mock
     *
     * @param input
     * @return
     */
    Object paramMock(Map<String, Object> input);

    /**
     * 返回结果mock
     *
     * @param input
     * @return
     */
    Object returnMock(Map<String, Object> input);
}
