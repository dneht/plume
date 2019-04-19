package net.dloud.platform.extend.constant;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;

/**
 * @author QuDasheng
 * @create 2018-09-02 16:23
 **/
public class PlatformConstants {
    public static final Config CONFIG = ConfigService.getAppConfig();

    public static final Config COMMON = ConfigService.getConfig("DEV.COMM");

    public static final int APPID = CONFIG.getIntProperty("app.id", 0);

    public static final String APPKEY = CONFIG.getProperty("app.key", "error").toLowerCase();

    public static final String APPNAME = CONFIG.getProperty("app.name", "");

    public static final String GROUP_PARAM = "group";

    public static final String DEFAULT_GROUP = "stable";

    public static final String GROUP = CONFIG.getProperty("app.group", DEFAULT_GROUP);

    public static final String SECRET = CONFIG.getProperty("app.secret", "");

    public static final String MODE_DEV = "dev";

    public static final int PROCESSOR_NUMBER = Runtime.getRuntime().availableProcessors();

    public static final String ZK_ADDRESS = COMMON.getProperty("zookeeper.address", "");

    public static final int ZK_SLEEP_TIME = COMMON.getIntProperty("zookeeper.sleep-time-in-millis", 1000);

    public static final int ZK_MAX_RETRIES = COMMON.getIntProperty("zookeeper.max-retries", 3);

    public static final String DUBBO_LOAD_BALANCE = COMMON.getProperty("dubbo.provider.loadbalance", "random");

    public static final String KAFKA_TOPIC = PlatformConstants.APPID + "-" + StartupConstants.RUN_MODE;

    public static final String KAFKA_CONSUMER_GROUP = PlatformConstants.KAFKA_TOPIC + "-" + PlatformConstants.GROUP;

    public static final String KAFKA_TOPIC_ALL = KAFKA_TOPIC + "-all";

    public static final String KAFKA_CONSUMER_GROUP_DIFF = KAFKA_CONSUMER_GROUP + "-" + System.getProperty("mac", "");

    public static final String BASE_PACKAGE = "net.dloud";

    public static final String PLATFORM_PACKAGE = "net.dloud.platform";

    public static final String PARSE_BASE_PATH = "classpath*:PARSE-INF/";


    public static final int CORRECT_CODE = 0;

    public static final String FROM_KEY = "__TENANT";

    public static final String GROUP_KEY = "__GROUP";

    public static final String PROOF_KEY = "__PROOF";

    public static final String SUBGROUP_KEY = "__SUB_GROUP";

    public static final String HANDGROUP_KEY = "__HAND_GROUP";

    public static final String SPREAD_KEY = "__SPREAD";


    public static final String SOURCE_MYSQL = "mysql";
    public static final String SOURCE_MYSQL_PUBLIC = "mysql_public";
    public static final String SOURCE_REDIS = "redis";
    public static final String SOURCE_REDIS_CORE = "redis_core";
    public static final String SOURCE_IGNITE = "ignite";
    public static final String SOURCE_ELASTIC = "elastic";
    public static final String SOURCE_DRUID = "druid";

    public static final int EXCEPTION_CODE_PASSED = 1;
    public static final int EXCEPTION_CODE_INNER = 2;
    public static final int EXCEPTION_CODE_REFUND = -1;
    public static final int EXCEPTION_CODE_UNKNOWN = 9;
}
