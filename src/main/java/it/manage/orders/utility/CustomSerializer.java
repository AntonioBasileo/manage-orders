package it.manage.orders.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.manage.orders.dto.OrderDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomSerializer implements Serializer<OrderDTO> {

    private final ObjectMapper objectMapper;


    @Override
    public byte[] serialize(String topic, OrderDTO data) {
        try {
            if (data == null) {
                return null;
            }

            return objectMapper.writeValueAsBytes(data);

        } catch (Exception e) {
            log.error("Error serializing Order to byte[]. Order id: {}, Error: {}",
                    data.getId(),
                    e.getMessage(), e);
            throw new SerializationException("Error when serializing Order to byte[]: " + e.getMessage(), e);
        }
    }
}
