package org.ming.dubbo.traceid.starter.exception;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 日志信息收集异常
 *
 * @author MingXiangjun
 * @create 2018-08-26 12:00
 */
@NoArgsConstructor
@AllArgsConstructor
public class TraceInfoCollectException extends RuntimeException {
    private String message;
    private int errCode;
    public TraceInfoCollectException(String message){
        super(message);
        this.message = message;
    }

    /**
     * default
     * @param errCode
     */
    public TraceInfoCollectException(int errCode){
        this.errCode = errCode;
    }
    /**
     * default
     * @param ex
     */
    public TraceInfoCollectException(Exception ex){
        super(ex);
    }
}
