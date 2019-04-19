package net.dloud.platform.parse.kafka.serialization;

import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.domain.message.KafkaMessage;
import net.dloud.platform.common.serialize.KryoBaseUtil;
import org.apache.kafka.common.serialization.Serializer;
import org.checkerframework.checker.index.qual.SameLen;

import java.util.Map;

/**
 * @author QuDasheng
 * @create 2018-10-12 12:06
 **/
@Slf4j
public class KryoSerializer implements Serializer<KafkaMessage> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String topic, KafkaMessage data) {
        try {
            return KryoBaseUtil.writeObjectToByteArray(data);
        } catch (Exception e) {
            log.warn("[KAFKA] 序列化失败", e);
            return null;
        }
    }

    @Override
    public void close() {

    }
}
