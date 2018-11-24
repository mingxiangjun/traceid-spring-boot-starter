package org.ming.dubbo.traceid.starter.aspect;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.ThreadContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.ming.dubbo.traceid.starter.config.TraceInfoConfig;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * rest接口类信息收集：
 *  收集内容：uri+queryString
 *
 * @author MingXiangjun
 * @create 2018-11-24 12:34
 */
@Aspect
@Log4j2
public class ControllerTraceInfoCollectAspect {
    @Resource
    TraceInfoConfig config;
    private static final String TRACE_ID_KEY = "requestId";
    private static final String FILTER_NAME_KEY = "filter";
    /**
     * 是否清除对应的rpc信息
     */
    private boolean shouldBeClear = false;

    @Around(value = "execution(* *..*Controller.*(..)))")
    public Object collectTraceInfo(ProceedingJoinPoint joinPoint) throws Throwable {
        String filter = getFilter(joinPoint);
        String traceId = getTraceId();
        Object result = null;
        try {
            if (config.isPrintLog()){
                long startTime = System.currentTimeMillis();
                log.info("filter is {},requestId is {}", filter,traceId);
                result = joinPoint.proceed();
                log.info("耗时={}(ms), filter is {},requestId is {}", System.currentTimeMillis() - startTime,filter, filter,traceId);
            }else {
                result = joinPoint.proceed();
            }
        } finally {
            clearTraceInfo(shouldBeClear);
        }
        return result;
    }

    /**
     * 清除存储的TraceInfo
     * @param shouldBeClear
     */
    private void clearTraceInfo(boolean shouldBeClear) {
        if (shouldBeClear){
            ThreadContext.remove(TRACE_ID_KEY);
            ThreadContext.remove(FILTER_NAME_KEY);
        }
    }

    /**
     * 获取filter
     * @param joinPoint
     * @return
     */
    private String getFilter(ProceedingJoinPoint joinPoint) {
        String filter = ThreadContext.get(FILTER_NAME_KEY);
        if (filter == null){
            filter = getUri(joinPoint)+"/"+getQueryString(joinPoint);
            ThreadContext.put(FILTER_NAME_KEY,filter);
        }
        return filter;
    }

    /**
     * 获取request请求参数串
     * @param joinPoint
     * @return
     */
    private String getQueryString(ProceedingJoinPoint joinPoint) {
        // 获取参数内容
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return request.getQueryString();
    }

    /**
     * 获取当前接口uri
     * @param joinPoint
     * @return
     */
    private String getUri(ProceedingJoinPoint joinPoint) {
        String uri = "";
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        // 类声明的uri部分
        if (method.getDeclaringClass().isAnnotationPresent(RequestMapping.class)) {
            RequestMapping requestMapping = method.getDeclaringClass().getAnnotation(RequestMapping.class);
            uri += requestMapping.value();
        }
        // 方法声明的uri部分
        if (method.isAnnotationPresent(RequestMapping.class) || method.isAnnotationPresent(GetMapping.class) || method.isAnnotationPresent(PostMapping.class)) {
            RequestMapping annotation = method.getAnnotation(RequestMapping.class);
            if (annotation != null) {
                uri += annotation.name();
            }
            GetMapping getMapping = method.getAnnotation(GetMapping.class);
            if (getMapping != null){
                uri += getMapping.value();
            }
            PostMapping postMapping = method.getAnnotation(PostMapping.class);
            if (postMapping != null){
                uri += postMapping.value();
            }
        }
        return uri;
    }

    /**
     * 获取traceId<br/>
     * 如果traceId为空，则判断context中是否包含traeId：有则直接取，没有则生成
     *
     * @return
     */
    private String getTraceId() {
        String traceId = ThreadContext.get(TRACE_ID_KEY);
        if (StringUtils.isEmpty(traceId)) {
            shouldBeClear = true;
            traceId = UUID.randomUUID().toString().replaceAll("-", "");
        }
        return traceId;
    }
}
