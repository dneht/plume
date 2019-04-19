package net.dloud.platform.parse.dubbo.wrapper;

import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.gateway.bean.StartupTime;
import net.dloud.platform.common.serialize.KryoBaseUtil;
import net.dloud.platform.extend.constant.PlatformConstants;
import net.dloud.platform.extend.constant.StartupConstants;
import net.dloud.platform.extend.tuple.PairTuple;
import net.dloud.platform.parse.curator.wrapper.CuratorWrapper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.context.ApplicationEventPublisher;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author QuDasheng
 * @create 2018-09-25 14:57
 **/
@Slf4j
public class DubboWrapper {
    public static final String DUBBO_ZK_PATH = "/dubbo_group";
    public static final String DUBBO_GROUP_PATH = DUBBO_ZK_PATH + "/" + PlatformConstants.GROUP;
    public static Map<String, Map<String, StartupTime>> dubboProvider = new HashMap<>();

    private static final String DEFAULT_SPLIT = " || ";


    public static String currentPath() {
        return DUBBO_GROUP_PATH + "/" + CuratorWrapper.currentPath(StartupConstants.RUN_HOST, StartupConstants.DUBBO_PORT);
    }

    /**
     * 异常信息合并
     */
    public static String getMessage(String code, String message) {
        return code + DEFAULT_SPLIT + message;
    }

    /**
     * 异常信息合并
     */
    public static PairTuple<String, String> parseMessage(String message) {
        if (null == message) {
            return new PairTuple<>("", "");
        }

        final String[] split = message.split(DEFAULT_SPLIT);
        if (split.length == 0) {
            return new PairTuple<>("", "");
        }

        if (split.length == 1) {
            return new PairTuple<>(split[0], "");
        } else {
            return new PairTuple<>(split[0], split[1]);
        }
    }

    public static void nodeCreate(CuratorFramework curatorClient, String currentPath) {
        try {
            curatorClient.create().creatingParentContainersIfNeeded().withMode(CreateMode.EPHEMERAL)
                    .forPath(currentPath, KryoBaseUtil.writeObjectToByteArray(new StartupTime(
                            PlatformConstants.APPID, PlatformConstants.APPKEY,
                            StartupConstants.SERVER_PORT, StartupConstants.DUBBO_PORT)));
        } catch (Exception e) {
            log.error("[{}] 新建CURATOR节点失败: {}, {}", PlatformConstants.APPNAME, currentPath, e.getMessage());
        }
    }

    public static PathChildrenCacheListener dubboListener(String group, String basePath) {
        final Map<String, StartupTime> availableProvider = dubboProvider.getOrDefault(group, new ConcurrentHashMap<>());
        if (!dubboProvider.containsKey(group)) {
            dubboProvider.put(group, availableProvider);
        }

        return (client, event) -> {
            final ChildData data = event.getData();
            switch (event.getType()) {
                case INITIALIZED:
                    log.info("[PLATFORM] 初始化GROUP[{}]", group);
                    break;
                case CHILD_ADDED:
                    final String add = data.getPath().replaceFirst(basePath + "/", "");
                    final StartupTime startup = KryoBaseUtil.readObjectFromByteArray(client.getData().forPath(data.getPath()), StartupTime.class);
                    availableProvider.put(add, startup);
                    log.info("[PLATFORM] 节点[{}]加入GROUP[{}]", add, group);
                    break;
                case CHILD_REMOVED:
                    final String del = data.getPath().replaceFirst(basePath + "/", "");
                    if (client.getState() == CuratorFrameworkState.STARTED) {
                        final Stat stat = client.checkExists().forPath(data.getPath());
                        if (null == stat) {
                            availableProvider.remove(del);
                            log.info("[PLATFORM] 节点[{}]移出GROUP[{}]", del, group);
                            final String currentPath = currentPath();
                            if (Objects.equals(currentPath, data.getPath())) {
                                final Stat now = client.checkExists().forPath(currentPath);
                                if (null == now) {
                                    log.info("[{}] 节点({})不存在, 需要重建", PlatformConstants.APPNAME, currentPath);
                                    nodeCreate(client, currentPath);
                                }
                            }
                        }
                    }
                    break;
                default:
                    log.info("[PLATFORM] 当前节点状态: {}", event.getType());
            }
        };
    }
}
