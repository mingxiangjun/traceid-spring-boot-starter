package org.ming.dubbo.traceid.starter.autoconfig;

import com.alibaba.dubbo.rpc.RpcContext;
import org.ming.dubbo.traceid.starter.aspect.ControllerTraceInfoCollectAspect;
import org.ming.dubbo.traceid.starter.aspect.TraceInfoCollcetAspect;
import org.ming.dubbo.traceid.starter.config.TraceInfoConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * dubbo追踪信息自动配置类
 *
 * @author MingXiangjun
 * @create 2018-08-26 10:54
 */
@Configuration
@ConditionalOnProperty(prefix = "dubbo.trace",name = "open",havingValue = "true")
@EnableConfigurationProperties(TraceInfoConfig.class)
public class TraceInfoAutoConfigure {
    /**
     * service 层的信息处理
     * @return
     */
    @Bean
    @ConditionalOnClass(RpcContext.class)
    @ConditionalOnMissingBean(value = TraceInfoCollcetAspect.class)
    TraceInfoCollcetAspect traceInfoCollcetAspect() {
        return new TraceInfoCollcetAspect();
    }

    /**
     * controller 的信息追踪处理
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(value = ControllerTraceInfoCollectAspect.class)
    ControllerTraceInfoCollectAspect controllerTraceInfoCollectAspect() {
        return new ControllerTraceInfoCollectAspect();
    }
}
