package net.dloud.platform.parse.kafka.listener;

import lombok.extern.slf4j.Slf4j;
import net.dloud.platform.common.domain.message.KafkaMessage;
import net.dloud.platform.extend.constant.PlatformConstants;
import net.dloud.platform.parse.context.LocalContext;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * @author QuDasheng
 * @create 2018-10-11 17:36
 **/
@Slf4j
@Component
public final class KafkaMessageListener implements AcknowledgingMessageListener<Long, KafkaMessage> {

    @Autowired
    private MessageExecutor messageExecutor;

    @Override
    public void onMessage(ConsumerRecord<Long, KafkaMessage> data, Acknowledgment acknowledgment) {
        final Long key = data.key();
        final KafkaMessage message = data.value();
        log.info("[MESSAGE] 接收到的消息 topic = {} | partition = {} | {} = {}",
                data.topic(), data.partition(), key, message);

        if (null == message) {
            log.warn("[MESSAGE] 当前消息为空, 不进行消费");
            acknowledgment.acknowledge();
            return;
        }

        final LocalContext local = LocalContext.fromKafka(data);
        if (null == local) {
            log.warn("[MESSAGE] 当前消息来源设置为空");
            acknowledgment.acknowledge();
            return;
        }
        LocalContext.set(local);

        final String messageBean = message.getBean();
        if (null == messageBean) {
            log.info("[MESSAGE] 当前消息消费方法为空, 不进行消费: 来源={}", local);
            acknowledgment.acknowledge();
            return;
        }
        if (null == message.getContent()) {
            log.info("[MESSAGE] 当前消息内容为空, 不进行消费: 来源={}", local);
            acknowledgment.acknowledge();
            return;
        }
        if (message.getOnly() && !PlatformConstants.GROUP.equalsIgnoreCase(message.getGroup())) {
            log.info("[MESSAGE] 当前消息设置为不消费: 来源={}, {}", local, message.getGroup());
            acknowledgment.acknowledge();
            return;
        }

        try {
            // 处理接收到的消息
            messageExecutor.execute(message);
            log.info("[MESSAGE] 消费完成开始提交offset: 来源={}", local);
            acknowledgment.acknowledge();
            log.info("[MESSAGE] 当前消息offset提交成功: 来源={}", local);
        } catch (Exception e) {
            log.error("[MESSAGE] 消息消费过程中出现异常: ", e);
        } finally {
            LocalContext.remove();
        }
    }
}
