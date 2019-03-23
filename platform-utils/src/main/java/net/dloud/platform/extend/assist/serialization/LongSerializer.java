package net.dloud.platform.extend.assist.serialization;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.lang.Nullable;

/**
 * @author QuDasheng
 * @create 2018-10-12 12:06
 **/
public class LongSerializer implements RedisSerializer<Long> {

    @Override
    public Long deserialize(@Nullable byte[] data) throws SerializationException {
        if (null == data || data.length == 0) {
            return null;
        }

        return Long.valueOf(new String(data));
    }

    @Override
    public byte[] serialize(@Nullable Long data) throws SerializationException {
        if (null == data) {
            return null;
        }

        return String.valueOf(data).getBytes();
    }
}
