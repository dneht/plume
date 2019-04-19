package net.dloud.platform.parse.kafka.serialization;

import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.domain.message.KafkaMessage;
import net.dloud.platform.common.serialize.KryoBaseUtil;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

/**
 * @author QuDasheng
 * @create 2018-10-12 12:06
 **/
@Slf4j
public class KryoDeserializer implements Deserializer<KafkaMessage> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public KafkaMessage deserialize(String topic, byte[] data) {
        try {
            return KryoBaseUtil.readObjectFromByteArray(data, KafkaMessage.class);
        } catch (Exception e) {
            log.warn("[KAFKA] 反序列化失败", e);
            return null;
        }
    }

    @Override
    public void close() {

    }
}
