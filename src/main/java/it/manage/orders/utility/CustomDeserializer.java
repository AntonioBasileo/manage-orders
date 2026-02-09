package it.manage.orders.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.manage.orders.dto.OrderDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomDeserializer implements Deserializer<OrderDTO> {

    private final ObjectMapper objectMapper;


    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public OrderDTO deserialize(String topic, byte[] data) {
        try {
            return data == null ? null : objectMapper.readValue(new String(data, StandardCharsets.UTF_8), OrderDTO.class);
        } catch (Exception e) {
            throw new SerializationException("Error when deserializing byte[] to Order");
        }
    }

    @Override
    public void close() {
    }
}
