package net.dloud.platform.gateway.util;

import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.service.GenericException;
import com.fasterxml.jackson.core.JsonParseException;
import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.extend.StringUtil;
import net.dloud.platform.common.gateway.bean.ApiResponse;
import net.dloud.platform.extend.constant.PlatformExceptionEnum;
import net.dloud.platform.extend.exception.InnerException;
import net.dloud.platform.extend.exception.PassedException;
import net.dloud.platform.extend.exception.RefundException;
import org.springframework.core.codec.CodecException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

/**
 * @author QuDasheng
 * @create 2018-10-05 20:05
 **/
@Slf4j
public class ExceptionUtil {
    public static ApiResponse handleReq(Throwable ex) {
        return handle(ex);
    }

    public static Mono<ApiResponse> handleOne(Throwable ex) {
        return Mono.just(handle(ex));
    }

    public static Mono<ApiResponse> handleList(Throwable ex) {
        final ApiResponse response = handle(ex);
        response.setPreload(Collections.singleton(response.getPreload()));
        return Mono.just(response);
    }

    private static ApiResponse handle(Throwable ex) {
        final ApiResponse response = new ApiResponse();
        if (ex instanceof InnerException) {
            final InnerException inner = (InnerException) ex;
            log.warn("[COMMON] 系统内部异常: {}", inner.getException());
            response.exception(PlatformExceptionEnum.SYSTEM_ERROR);
        } else if (ex instanceof PassedException) {
            final PassedException passed = (PassedException) ex;
            log.info("[COMMON] 业务内部校验不通过: {}", ex.getMessage());
            ResultWrapper.err(response, passed);
        } else if (ex instanceof RefundException) {
            final RefundException refund = (RefundException) ex;
            log.info("[COMMON] 调用了未授权的资源: {}", ex.getMessage());
            ResultWrapper.err(response, refund);
        } else if (ex instanceof RpcException) {
            log.error("[COMMON] DUBBO调用异常: ", ex);
            response.exception(PlatformExceptionEnum.CLIENT_TIMEOUT);
        } else if (ex instanceof GenericException) {
            log.error("[COMMON] DUBBO调用内部自定义异常, 具体信息如上");
            response.exception(PlatformExceptionEnum.CLIENT_ERROR);
        } else if (ex instanceof NullPointerException) {
            log.warn("[COMMON] 出现空指针异常, 具体信息: ", ex);
            response.exception(PlatformExceptionEnum.BAD_REQUEST);
        } else if (ex instanceof IOException || ex instanceof ResponseStatusException) {
            log.info("[COMMON] 读取或写入异常: {}", ex.getMessage());
            response.exception(PlatformExceptionEnum.BAD_REQUEST);
        } else if (ex instanceof CodecException) {
            log.info("[COMMON] 输入值解析异常: {}", ex.getMessage());
            response.exception(PlatformExceptionEnum.BAD_REQUEST);
        } else {
            response.exception(PlatformExceptionEnum.SYSTEM_BUSY);
            log.warn("[COMMON] 系统调用异常: ", ex);
        }

        if (null == response.getProof()) {
            response.setProof(UUID.randomUUID().toString());
        }
        return response;
    }
}
