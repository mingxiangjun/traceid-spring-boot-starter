package org.ming.dubbo.traceid.starter.aspect;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.RpcContext;
import lombok.extern.log4j.Log4j2;
import org.apache.log4j.MDC;
import org.apache.logging.log4j.ThreadContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ming.dubbo.traceid.starter.exception.TraceInfoCollectException;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * 追踪信息搜集切面类
 *
 * @author MingXiangjun
 * @create 2018-08-26 10:59
 */
@Aspect
@Log4j2
public class TraceInfoCollcetAspect {

    private static final String TRACE_ID_KEY = "traceId";
    private static final String APP_NAME_KEY = "appName";

    @Around(value = "execution(* *..*ServiceImpl.*(..))")
    public Object collectTraceInfo(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        log.info("==============>>>>>collect rpc info<<<<<<=============");
        // 获取traceId
        Object traceId = MDC.get(TRACE_ID_KEY);
        RpcContext context = RpcContext.getContext();

        if (context.isProviderSide()) {
            log.info("==============>>>>>collect rpc info from provider>>>>>>>");
            // 服务提供方
            if (StringUtils.isEmpty(traceId) && context.getAttachment(TRACE_ID_KEY) == null) {
                traceId = UUID.randomUUID().toString().replaceAll("-", "");
            }
            // 服务提供方，从attachment中获取调用方AppName
            String appName = context.getAttachment(APP_NAME_KEY);
            traceId += appName;
            MDC.put(TRACE_ID_KEY, traceId);
        } else if (context.isConsumerSide()) {
            log.info("<<<<<<collect rpc info from consumer<<<<<<<<=============");
            // 如果traceId 不存在，则生成一个32位uuid
            if (StringUtils.isEmpty(traceId)) {
                traceId = UUID.randomUUID().toString().replaceAll("-", "");

            }
            // 服务消费方
            if (!StringUtils.isEmpty(context.getAttachment(APP_NAME_KEY))) {
                throw new TraceInfoCollectException("RpcContext Attachment get the same key：appName.you must define a new key in your application");
            }
            // 服务消费方，从url中获取本地appName，并放入rpcContext
            URL rpcUrl = context.getUrl();
            String application = rpcUrl!=null?rpcUrl.getParameter("application"):"empty application";
            context.setAttachment(APP_NAME_KEY, application);
            context.setAttachment(TRACE_ID_KEY, traceId.toString());
            traceId += application;
            MDC.put(TRACE_ID_KEY, traceId);
        }
        // 正式调用方法
        Object result = proceedingJoinPoint.proceed();
        // 方法调用完成后清除MDC
        MDC.clear();

        return result;
    }
}
