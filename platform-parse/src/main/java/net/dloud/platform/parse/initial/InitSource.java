package net.dloud.platform.parse.initial;

import com.alibaba.dubbo.config.annotation.Reference;
import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.client.InitialService;
import net.dloud.platform.common.domain.entry.GroupEntry;
import net.dloud.platform.common.domain.result.KeystoreResult;
import net.dloud.platform.common.domain.result.PairResult;
import net.dloud.platform.common.security.Twins;
import net.dloud.platform.common.serialize.KryoBaseUtil;
import net.dloud.platform.extend.constant.PlatformConstants;
import net.dloud.platform.extend.constant.StartupConstants;
import net.dloud.platform.extend.exception.InnerException;
import net.dloud.platform.parse.utils.SourceGet;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.Map;

/**
 * 初始化缓存、数据库等
 *
 * @author QuDasheng
 * @create 2018-07-16 10:53
 **/
@Slf4j
@EnableTransactionManagement
@Configuration
@ConditionalOnProperty(name = "source.init.enable", matchIfMissing = true, havingValue = "true")
@AutoConfigureAfter(InitDubbo.class)
public class InitSource {

    @Value("${cache.ttl:900}")
    private Integer cacheTtl;

    @Value("#{${cache.configs:'{}'}}")
    private Map<String, Integer> cacheConfigs;

    @Reference
    private InitialService sourceInit;

    private byte[] enckey;

    private Map<String, KeystoreResult> keystore;


    @PostConstruct
    private void initSource() {
        final PairResult<byte[], byte[]> source = sourceInit.getSource(new GroupEntry(
                PlatformConstants.APPID, PlatformConstants.APPNAME, StartupConstants.RUN_MODE,
                PlatformConstants.GROUP, ""));
        if (null != source && source.isSuccess()) {
            enckey = source.getFirst();
            keystore = KryoBaseUtil.readFromByteArray(source.getLast());
            log.info("[{}] 获取初始化资源成功", PlatformConstants.APPNAME);
        } else {
            log.error("[{}] 获取初始化资源失败: {}", PlatformConstants.APPNAME, source);
            throw new InnerException("初始化资源失败");
        }
    }

    @Bean
    public RedisConnectionFactory redisFactory() {
        try {
            final KeystoreResult keystore = this.keystore.get(PlatformConstants.SOURCE_REDIS);
            if (null == keystore) {
                throw new InnerException("初始化资源失败");
            }
            return SourceGet.getLettuceFactory(Twins.decrypt(keystore.getUrls(), enckey),
                    Twins.decrypt(keystore.getPassword(), enckey));
        } catch (Exception e) {
            log.error("[{}] REDIS初始化失败: {}, {}", PlatformConstants.APPNAME, e.getMessage(), e);
            throw new InnerException("初始化资源失败");
        }
    }

    /**
     * 创建数据库连接
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        return dataSourceByKey(PlatformConstants.SOURCE_MYSQL);
    }

    @Bean("pubDataSource")
    public DataSource pubDataSource() {
        return dataSourceByKey(PlatformConstants.SOURCE_MYSQL_PUBLIC);
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    public Jdbi createHandle(DataSource dataSource) {
        return Jdbi.create(dataSource);
    }

    private DataSource dataSourceByKey(String key) {
        try {
            final KeystoreResult keystore = this.keystore.get(key);
            if (null == keystore) {
                throw new InnerException("初始化资源失败");
            }
            return SourceGet.getHikariSource(Twins.decrypt(keystore.getUrls(), enckey),
                    keystore.getUsername(), Twins.decrypt(keystore.getPassword(), enckey),
                    keystore.getDriveClass());
        } catch (Exception e) {
            log.error("[{}] MYSQL初始化失败: {}", PlatformConstants.APPNAME, e.getMessage());
            log.error("MYSQL初始化失败:", e);
            throw new InnerException("初始化资源失败");
        }
    }
}