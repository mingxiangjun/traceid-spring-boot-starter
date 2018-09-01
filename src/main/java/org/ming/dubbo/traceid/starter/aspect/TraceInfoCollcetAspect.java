package org.ming.dubbo.traceid.starter.aspect;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.RpcContext;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.ThreadContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ming.dubbo.traceid.starter.exception.TraceInfoCollectException;
import org.springframework.util.StringUtils;

import java.util.Arrays;
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

    /**
     * 全链路追踪key
     */
    private static final String TRACE_ID_KEY = "requestId";
    private static final String APP_NAME_KEY = "appName";
    private static final String UNKNOWN_KEY = "unknown";

    @Around(value = "execution(* *..*ServiceImpl.*(..)) || execution(* *..*Controller.*(..))")
    public Object collectTraceInfo(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        RpcContext context = RpcContext.getContext();

        // 获取traceId
        Object traceId = getTraceId(context);
        context.setAttachment(TRACE_ID_KEY, traceId.toString());
        ThreadContext.put(TRACE_ID_KEY, traceId.toString());

        String appName = getAppName(context);
        ThreadContext.put(APP_NAME_KEY,appName);
        context.setAttachment(APP_NAME_KEY,appName);

        // 正式调用方法
        long startTime = System.currentTimeMillis();
        log.info("className={}, methodName={}, params={}", proceedingJoinPoint.getSignature().getDeclaringTypeName(), proceedingJoinPoint.getSignature().getName(), Arrays.toString(proceedingJoinPoint.getArgs()));
        Object result = proceedingJoinPoint.proceed();
        log.info("耗时={}(ms), className={}, methodName={}, result={}", System.currentTimeMillis() - startTime, proceedingJoinPoint.getSignature().getDeclaringTypeName(), proceedingJoinPoint.getSignature().getName(), result);

        // 方法调用完成后清除ThreadContext
        ThreadContext.clearAll();
        return result;
    }

    /**
     * 获取dubbo调用接口的application：
     *  获取从MDC中集成来的appName，然后添加dubbo调用中的服务名
     * @param context
     * @return
     */
    private String getAppName(RpcContext context) {
        if (context.getUrl() != null && context.isProviderSide()) {
            log.info("==============>>>>>collect rpc info from provider>>>>>>>");
        } else if (context.getUrl() != null && context.isConsumerSide()) {
            log.info("<<<<<<collect rpc info from consumer<<<<<<<<=============");
        }
        // 服务消费方，从url中获取本地appName，并放入rpcContext
        String appName = context.getAttachment(APP_NAME_KEY);
        URL rpcUrl = context.getUrl();
        String application = rpcUrl!=null?rpcUrl.getParameter("application"): UNKNOWN_KEY;
        if (!StringUtils.isEmpty(appName)){
            application += ">>" +appName;
        }
        return application;
    }

    /**
     * 获取traceId<br/>
     *  如果traceId为空，则判断context中是否包含traeId：有则直接取，没有则生成
     * @param context
     * @return
     */
    private Object getTraceId(RpcContext context) {
        Object traceId = ThreadContext.get(TRACE_ID_KEY);
        if (StringUtils.isEmpty(traceId)) {
            if (context.getAttachment(TRACE_ID_KEY) == null){
                traceId = UUID.randomUUID().toString().replaceAll("-", "");
            }else{
                traceId = context.getAttachment(TRACE_ID_KEY);
            }
        }
        return traceId;
    }
}
