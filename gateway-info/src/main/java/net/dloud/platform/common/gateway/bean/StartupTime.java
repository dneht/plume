package net.dloud.platform.common.gateway.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author QuDasheng
 * @create 2018-09-11 11:51
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StartupTime implements Serializable {
    private static final long serialVersionUID = 5088573129716322169L;

    /**
     * 系统id
     */
    private Integer id;

    /**
     * 系统名
     */
    private String key;

    /**
     * http端口
     */
    private Integer http;

    /**
     * dubbo端口
     */
    private Integer dubbo;
}
