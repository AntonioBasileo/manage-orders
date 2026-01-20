package it.subito.orders.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.subito.orders.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

@Slf4j
public class CustomSerializer implements Serializer<Order> {

    private final ObjectMapper objectMapper = new ObjectMapper();


    public CustomSerializer() {
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public byte[] serialize(String topic, Order data) {
        try {
            return data == null ? null : objectMapper.writeValueAsBytes(data);
        } catch (Exception e) {
            throw new SerializationException("Error when serializing Order to byte[]");
        }
    }

    @Override
    public void close() {
    }
}
