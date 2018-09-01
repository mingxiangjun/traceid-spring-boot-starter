package org.ming.dubbo.traceid.starter.autoconfig;

import com.alibaba.dubbo.rpc.RpcContext;
import org.ming.dubbo.traceid.starter.aspect.TraceInfoCollcetAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * dubbo追踪信息自动配置类
 *
 * @author MingXiangjun
 * @create 2018-08-26 10:54
 */
@Configuration
public class TraceInfoAutoConfigure {

    @Bean
    @ConditionalOnClass(RpcContext.class)
    @ConditionalOnMissingBean(value = TraceInfoCollcetAspect.class)
    TraceInfoCollcetAspect traceInfoCollcetAspect() {
        return new TraceInfoCollcetAspect();
    }
}
