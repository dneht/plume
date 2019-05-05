package net.dloud.platform.parse.context;

import com.alibaba.dubbo.rpc.RpcContext;
import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.domain.message.KafkaMessage;
import net.dloud.platform.common.extend.StringUtil;
import net.dloud.platform.extend.constant.PlatformConstants;
import net.dloud.platform.extend.exception.InnerException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;

import java.util.UUID;

/**
 * @author QuDasheng
 * @create 2019-04-30 15:37
 **/
@Slf4j
public class LocalContext {
    private static final InheritableThreadLocal<LocalContext> HOLDER = new InheritableThreadLocal<LocalContext>() {
        @Override
        protected LocalContext initialValue() {
            return new LocalContext();
        }

        @Override
        protected LocalContext childValue(LocalContext parent) {
            return new LocalContext(parent);
        }
    };

    private String from;

    private String proof;


    public LocalContext() {
    }

    public LocalContext(LocalContext parent) {
        this.from = parent.getFrom();
        this.proof = parent.getProof();
    }

    public LocalContext(String from, String proof) {
        this.from = from;
        this.proof = proof;
    }


    public static LocalContext fromDubbo() {
        final LocalContext context = new LocalContext();
        final String from = RpcContext.getContext().getAttachment(PlatformConstants.FROM_KEY);
        if (StringUtil.isBlank(from)) {
            return null;
        }
        context.from = from;

        final String proof = RpcContext.getContext().getAttachment(PlatformConstants.PROOF_KEY);
        context.proof = null == proof ? UUID.randomUUID().toString() : proof;
        log.trace("[PLATFORM] 从dubbo中读取: {}", context);
        return context;
    }

    public static LocalContext fromKafka(ConsumerRecord<Long, KafkaMessage> data) {
        final byte[] bytes;
        final Iterable<Header> iterable = data.headers().headers(PlatformConstants.LOCAL_KEY);
        if (null != iterable && iterable.iterator().hasNext()) {
            bytes = iterable.iterator().next().value();
        } else {
            bytes = null;
        }
        if (null == bytes) {
            return null;
        }

        final LocalContext context = fromByte(bytes);
        log.trace("[PLATFORM] 从kafka中读取: {}", context);
        return context;
    }

    public static LocalContext fromByte(byte[] data) {
        final String text = new String(data);
        if (StringUtil.isBlank(text)) {
            return null;
        } else {
            final String[] split = text.split("\\|\\|");
            if (split.length != 2) {
                return null;
            } else {
                return new LocalContext(split[0], split[1]);
            }
        }
    }

    public static void set(String from) {
        HOLDER.set(new LocalContext(from, UUID.randomUUID().toString()));
    }

    public static void set(String from, String proof) {
        HOLDER.set(new LocalContext(from, proof));
    }

    public static void set(LocalContext context) {
        HOLDER.set(context);
    }

    public static LocalContext get() {
        final LocalContext context = HOLDER.get();
        log.trace("[PLATFORM] 从context中读取: {}", context);
        return context;
    }

    public static LocalContext copy(LocalContext context) {
        HOLDER.set(context);
        return context;
    }

    public static void remove() {
        HOLDER.remove();
    }

    public static LocalContext load() {
        LocalContext context = fromDubbo();
        if (null != context && null != context.getFrom()) {
            return context;
        }

        context = HOLDER.get();
        if (null == context || null == context.getFrom()) {
            throw new InnerException("来源读取错误");
        }
        if (null == context.getProof()) {
            context.setProof(UUID.randomUUID().toString());
        }
        return context;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getProof() {
        return proof;
    }

    public void setProof(String proof) {
        this.proof = proof;
    }

    public String toText() {
        return from + "||" + proof;
    }

    public byte[] toByte() {
        return toText().getBytes();
    }

    @Override
    public String toString() {
        return "LocalContext{" +
                "from='" + from + '\'' +
                ", proof='" + proof + '\'' +
                '}';
    }
}
