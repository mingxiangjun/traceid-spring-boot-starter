package org.ming.dubbo.traceid.starter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置文件
 *
 * @author MingXiangjun
 * @create 2018-11-24 12:48
 */
@Data
@ConfigurationProperties(prefix = "dubbo.trace")
public class TraceInfoConfig {
    /**
     * 是否开启日志追踪信息
     */
    private boolean open;
    /**
     * 是否打印基础日志信息
     */
    private boolean printLog;
}
