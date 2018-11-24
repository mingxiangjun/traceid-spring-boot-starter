package org.ming.dubbo.traceid.starter.aspect;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.RpcContext;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.ThreadContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ming.dubbo.traceid.starter.config.TraceInfoConfig;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
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
    private static final String FILTER_NAME_KEY = "filter";
    /**
     * 是否清除对应的rpc信息
      */
    private boolean shouldBeClear = false;

    @Resource
    private TraceInfoConfig config;

    /**
     * 处理Service层的调用信息链路追踪
     *
     * @param proceedingJoinPoint 切点
     * @throws Throwable
     */
    @Around(value = "execution(* *..*ServiceImpl.*(..))")
    public Object collectTraceInfo(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        RpcContext context = RpcContext.getContext();

        // 获取traceId
        Object traceId = getTraceId(context);
        context.setAttachment(TRACE_ID_KEY, traceId.toString());
        ThreadContext.put(TRACE_ID_KEY, traceId.toString());
        // filter日志信息：主要是appName+方法参数展示
        String filter = getFilter(context, proceedingJoinPoint);
        ThreadContext.put(FILTER_NAME_KEY, filter);

        Object result;
        try {
            if (config.isPrintLog()) {
                // 如果配置了日志打印，则打印对应的方法签名，参数等信息
                long startTime = System.currentTimeMillis();
                log.info("className={}, methodName={}, params={}", proceedingJoinPoint.getSignature().getDeclaringTypeName(), proceedingJoinPoint.getSignature().getName(), Arrays.toString(proceedingJoinPoint.getArgs()));
                result = proceedingJoinPoint.proceed();
                log.info("耗时={}(ms), className={}, methodName={}, result={}", System.currentTimeMillis() - startTime, proceedingJoinPoint.getSignature().getDeclaringTypeName(), proceedingJoinPoint.getSignature().getName(), result);
            } else {
                result = proceedingJoinPoint.proceed();
            }
        } finally {
            // 最终根据标识符决定是否清理日志追踪信息
            clearTraceInfo(shouldBeClear);
        }
        return result;
    }

    /**
     * 获取Filter展示
     *
     * @param context
     * @param proceedingJoinPoint
     * @return
     */
    private String getFilter(RpcContext context, ProceedingJoinPoint proceedingJoinPoint) {
        String filter = ThreadContext.get(FILTER_NAME_KEY);
        if (filter == null) {
            // 拼接Filter ： appName调用链+调用参数
            StringBuilder filterBuilder = new StringBuilder();
            String appName = getAppName(context);
            Object[] args = proceedingJoinPoint.getArgs();
            filterBuilder.append(appName).append(Arrays.toString(args));
            filter = filterBuilder.toString();
        }
        return filter;
    }

    /**
     * 清除追踪信息
     *
     * @param shouldBeClear
     */
    private void clearTraceInfo(boolean shouldBeClear) {
        if (shouldBeClear) {
            ThreadContext.remove(TRACE_ID_KEY);
            ThreadContext.remove(APP_NAME_KEY);
            ThreadContext.remove(FILTER_NAME_KEY);
        }
    }

    /**
     * 获取dubbo调用接口的application：
     * 获取从MDC中集成来的appName，然后添加dubbo调用中的服务名
     *
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
        String application = rpcUrl != null ? rpcUrl.getParameter("application") : "";
        if (!StringUtils.isEmpty(appName) && !StringUtils.isEmpty(application) && !application.contains(appName)) {
            application += ">>" + appName;
        } else {
            application = StringUtils.isEmpty(appName) ? application : appName;
        }
        if (!StringUtils.isEmpty(application)) {
            context.setAttachment(APP_NAME_KEY, application);
        }
        return application;
    }

    /**
     * 获取traceId<br/>
     * 如果traceId为空，则判断context中是否包含traeId：有则直接取，没有则生成
     *
     * @param context
     * @return
     */
    private Object getTraceId(RpcContext context) {
        Object traceId = ThreadContext.get(TRACE_ID_KEY);
        if (StringUtils.isEmpty(traceId)) {
            if (context.getAttachment(TRACE_ID_KEY) == null) {
                // only firstTimeIn，firstTimeout
                shouldBeClear = true;
                traceId = UUID.randomUUID().toString().replaceAll("-", "");
            } else {
                traceId = context.getAttachment(TRACE_ID_KEY);
            }
        }
        return traceId;
    }
}
