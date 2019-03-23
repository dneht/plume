package net.dloud.platform.parse.eureka;

import net.dloud.platform.extend.constant.StartupConstants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * @author QuDasheng
 * @create 2018-09-02 18:45
 **/
@Primary
@Component
@ConditionalOnClass(value = org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean.class)
public class EurekaConfigBean extends EurekaInstanceConfigBean {

    public EurekaConfigBean(InetUtils inetUtils) {
        super(inetUtils);
    }

    @Override
    public void setInstanceId(String instanceId) {
        super.setInstanceId(instanceId.toLowerCase());
    }

    @Override
    public void setPreferIpAddress(boolean preferIpAddress) {
        if (StartupConstants.IS_PUBLIC) {
            super.setPreferIpAddress(false);
        } else {
            super.setPreferIpAddress(true);
        }
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }
}
