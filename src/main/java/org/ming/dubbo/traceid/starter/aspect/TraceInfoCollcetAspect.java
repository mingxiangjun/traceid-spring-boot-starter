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

    @Around(value = "execution(* *..*ServiceImpl.*(..)) or execution(* *..*Controller.*(..))")
    public Object collectTraceInfo(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        RpcContext context = RpcContext.getContext();

        // 获取traceId
        Object traceId = getTraceId(context);

        if (context.isProviderSide()) {
            // 服务提供方
            log.info("==============>>>>>collect rpc info from provider>>>>>>>");
            // 从attachment中获取调用方AppName
            String appName = context.getAttachment(APP_NAME_KEY);
            appName = StringUtils.isEmpty(appName)?UNKNOWN_KEY:appName;
            ThreadContext.put(TRACE_ID_KEY, traceId.toString());
            ThreadContext.put(APP_NAME_KEY,appName);
            log.info("remote application:{} call local dubbo method {}",appName,context.getMethodName());
        } else if (context.isConsumerSide()) {
            log.info("<<<<<<collect rpc info from consumer<<<<<<<<=============");
            // 服务消费方
            if (!StringUtils.isEmpty(context.getAttachment(APP_NAME_KEY))) {
                throw new TraceInfoCollectException("RpcContext Attachment get the same key：appName.you must define a new key in your application");
            }
            // 服务消费方，从url中获取本地appName，并放入rpcContext
            URL rpcUrl = context.getUrl();
            String application = rpcUrl!=null?rpcUrl.getParameter("application"): UNKNOWN_KEY;
            context.setAttachment(APP_NAME_KEY, application);
            context.setAttachment(TRACE_ID_KEY, traceId.toString());
            ThreadContext.put(TRACE_ID_KEY, traceId.toString());
            ThreadContext.put(APP_NAME_KEY,application);
            log.info("call remote dubbo method {}",context.getMethodName());
        } else {
            // 找不到dubbo服务上下文
            context.setAttachment(TRACE_ID_KEY,traceId.toString());
            ThreadContext.put(TRACE_ID_KEY, traceId.toString());
            log.info("call method {}",context.getMethodName());
        }

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
