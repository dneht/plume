package net.dloud.platform.maven.client;

import net.dloud.platform.common.annotation.Permission;

/**
 * 测试服务测试服务
 *
 * @author QuDasheng
 * @title 测试服务
 * @time 2018-08-27 09:28
 **/
@Permission(false)
public interface TestService {
    /**
     * 判断当前分组是否需要更新
     *
     * @return
     */
    TestResult testInfo(TestEntry testEntry);
}
