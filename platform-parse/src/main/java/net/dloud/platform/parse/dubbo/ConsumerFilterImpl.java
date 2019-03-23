package net.dloud.platform.parse.dubbo;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.service.GenericException;
import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.domain.result.ExceptionResult;
import net.dloud.platform.extend.constant.PlatformConstants;
import net.dloud.platform.extend.constant.PlatformExceptionEnum;
import net.dloud.platform.extend.exception.InnerException;
import net.dloud.platform.extend.exception.PassedException;
import net.dloud.platform.extend.exception.RefundException;

import java.util.Map;

/**
 * @author QuDasheng
 * @create 2018-09-02 13:08
 **/
@Slf4j
@Activate(group = Constants.CONSUMER, value = "consumerFilter", order = -40000)
public class ConsumerFilterImpl implements Filter {

    @Override
    @SuppressWarnings("unchecked")
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        final RpcContext context = RpcContext.getContext();
        final String tenant = context.getAttachment(PlatformConstants.FROM_KEY);
        final String proof = context.getAttachment(PlatformConstants.PROOF_KEY);
        log.info("[{}] 消费[DUBBO]服务: 来源: {}, 方法名: {}, 附加参数: {}", PlatformConstants.APPNAME,
                tenant, invocation.getMethodName(), invocation.getAttachments());
        context.setInvoker(invoker).setInvocation(invocation).setLocalAddress(NetUtils.getLocalHost(), 0)
                .setRemoteAddress(invoker.getUrl().getHost(), invoker.getUrl().getPort());
        if (invocation instanceof RpcInvocation) {
            ((RpcInvocation) invocation).setInvoker(invoker);
        }

        try {
            //计算调用时间
            final long startTime = System.nanoTime() / 1000;
            final Result invoke = invoker.invoke(invocation);
            final long endTime = System.nanoTime() / 1000;
            log.info("[{}] 消费DUBBO服务[{}]结束, 凭证: {}, 耗时: [{}]μs", PlatformConstants.APPNAME,
                    invocation.getMethodName(), proof, endTime - startTime);
            final Object value = invoke.getValue();
            if (value instanceof Map || value instanceof ExceptionResult) {
                ExceptionResult exception = null;
                if (value instanceof ExceptionResult) {
                    exception = (ExceptionResult) value;
                } else {
                    final Map map = (Map) value;
                    if ((boolean) map.getOrDefault("failed", false)) {
                        exception = new ExceptionResult((int) map.getOrDefault("type", 1),
                                String.valueOf(map.getOrDefault("code", PlatformExceptionEnum.SYSTEM_BUSY.getCode())),
                                String.valueOf(map.getOrDefault("message", PlatformExceptionEnum.SYSTEM_BUSY.getMessage())),
                                String.valueOf(map.getOrDefault("exception", "")));
                    }
                }

                if (null != exception) {
                    final int type = exception.getType();
                    if (type == PlatformConstants.EXCEPTION_CODE_PASSED) {
                        log.warn("[DUBBO] 业务内部校验不通过, 凭证: {}, 原因: {}", proof, exception.getMessage());
                        return new RpcResult(new PassedException(exception.getCode(), exception.getMessage()));
                    } else if (type == PlatformConstants.EXCEPTION_CODE_REFUND) {
                        log.warn("[DUBBO] 调用了未授权的资源, 凭证: {}, 原因: {}", proof, exception.getMessage());
                        return new RpcResult(new RefundException(exception.getCode(), exception.getMessage()));
                    } else if (type == PlatformConstants.EXCEPTION_CODE_INNER) {
                        log.warn("[DUBBO] 内部调用异常, 凭证: {}, 原因: {}", proof, exception.getException());
                        return new RpcResult(new InnerException(exception.getMessage(), exception.getException()));
                    } else {
                        log.error("[DUBBO] 调用未知异常, 凭证: {}, 原因: {}", proof, exception.getException());
                        return new RpcResult(new InnerException(exception.getMessage(), exception.getException()));
                    }
                }
            }
            RpcContext.getServerContext().setAttachments(invoke.getAttachments());
            return invoke;
        } catch (RpcException ex) {
            log.warn("[DUBBO] 调用出现已知异常, 凭证: {}, 异常: {}, {}, {}", proof, ex.getCode(), ex.getMessage(), ex.getLocalizedMessage());
            if (ex.isSerialization()) {
                throw new PassedException(PlatformExceptionEnum.SERIALIZE_ERROR);
            } else if (ex.isForbidded()) {
                throw new PassedException(PlatformExceptionEnum.FORBIDDEN);
            } else if (ex.isNetwork()) {
                throw new PassedException(PlatformExceptionEnum.CLIENT_ERROR);
            } else if (ex.isTimeout()) {
                throw new PassedException(PlatformExceptionEnum.CLIENT_TIMEOUT);
            } else {
                throw new InnerException(ex.getMessage(), ex.getCause());
            }
        } catch (GenericException ex) {
            log.error("[DUBBO] 调用内部自定义异常, 凭证: {}, 异常: {}", proof, ex);
            throw new InnerException(ex.getExceptionMessage(), ex.getCause());
        } catch (Exception ex) {
            log.error("[DUBBO] 调用未知异常, 凭证: {}, 异常: {}", proof, ex);
            throw new InnerException(ex.getMessage(), ex.getCause());
        } finally {
            context.clearAttachments();
            context.setAttachment(PlatformConstants.FROM_KEY, tenant);
            context.setAttachment(PlatformConstants.PROOF_KEY, proof);
        }
    }
}
