package it.subito.orders.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.subito.orders.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
public class CustomeDeserializer implements Deserializer<Order> {

    private final ObjectMapper objectMapper = new ObjectMapper();


    public CustomeDeserializer() {
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public Order deserialize(String topic, byte[] data) {
        try {
            return data == null ? null : objectMapper.readValue(new String(data, StandardCharsets.UTF_8), Order.class);
        } catch (Exception e) {
            throw new SerializationException("Error when deserializing byte[] to OrderDTO");
        }
    }

    @Override
    public void close() {
    }
}
