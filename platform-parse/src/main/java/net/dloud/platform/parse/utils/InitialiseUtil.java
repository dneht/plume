package net.dloud.platform.parse.utils;

import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.client.GatewayService;
import net.dloud.platform.common.domain.Pair;
import net.dloud.platform.common.domain.entry.GroupEntry;
import net.dloud.platform.common.domain.result.GatewayGroupResult;
import net.dloud.platform.common.domain.result.GatewayMethodResult;
import net.dloud.platform.common.extend.ArrayUtil;
import net.dloud.platform.extend.constant.PlatformConstants;
import net.dloud.platform.extend.constant.StartupConstants;

import java.util.List;

/**
 * @author QuDasheng
 * @create 2019-04-04 12:46
 **/
@Slf4j
public class InitialiseUtil {
    public static void doInitialise(GatewayService gateway, String parsePath,
                                    byte[][] versions, String version) {
        final GroupEntry groupInfo = new GroupEntry(PlatformConstants.APPID, PlatformConstants.APPNAME,
                StartupConstants.RUN_MODE, PlatformConstants.GROUP, version);
        final GatewayGroupResult groupResult = gateway.groupInfo(groupInfo);
        if (null != groupResult && groupResult.isSuccess() && !groupResult.isConsistent()) {
            log.info("[GATEWAY] 开始初始化网关, 是否是新组({})", groupResult.isNewgroup());
            classResult(gateway, parsePath, groupInfo, groupResult.isNewgroup(), versions.length == 1);
        } else {
            log.warn("[GATEWAY] 当前不需要初始化网关: {}", groupResult);
        }
    }

    public static void classResult(GatewayService gateway, String parsePath,
                                   GroupEntry groupInfo, Boolean isNewgroup, Boolean isSingle) {
        if (isSingle) {
            final byte[] index = ResourceGet.resourceFile2Byte(parsePath + "index");
            methodResult(gateway, parsePath, groupInfo, gateway.clazzInfo(groupInfo, isNewgroup, index));
        } else {
            final byte[][] index = ResourceGet.resourceMulti2Byte(parsePath + "index");
            final Pair<int[], byte[]> pair = ArrayUtil.concatAndOffset(index);
            methodResult(gateway, parsePath, groupInfo, gateway.clazzInfo(groupInfo, isNewgroup, pair.getLast(), pair.getFirst()));
        }
    }

    public static void methodResult(GatewayService gateway, String parsePath,
                                    GroupEntry groupInfo, GatewayMethodResult methodResult) {
        if (methodResult.isSuccess()) {
            final List<String> methodList = methodResult.getClassList();
            final int methodSize = methodList.size();
            if (methodSize <= 0) {
                log.info("[GATEWAY] 没有需要初始化的方法");
                return;
            }

            byte[][] dataList = new byte[methodSize][];
            for (int i = 0; i < methodSize; i++) {
                dataList[i] = ResourceGet.resourceFile2Byte(parsePath + methodList.get(i));
            }

            final GatewayMethodResult newResult = gateway.methodInfo(groupInfo, methodResult.getClassVersion(), dataList);
            if (newResult.isSuccess()) {
                if (null == newResult.getClassList() || newResult.getClassList().isEmpty()) {
                    log.info("[GATEWAY] 初始化网关成功");
                } else {
                    methodResult(gateway, parsePath, groupInfo, methodResult);
                }
            } else {
                log.warn("[GATEWAY] 初始化网关失败, 方法列表: {}", methodList);
            }
        }
    }
}
