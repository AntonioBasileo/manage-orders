package it.manage.orders.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

@Component("kafkaListenerContainer")
@Slf4j
@RequiredArgsConstructor
public class KafkaListenerContainerHealthIndicator extends AbstractHealthIndicator {

  private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;


  @Override
  protected void doHealthCheck(Health.Builder builder) {
    log.debug("doHealthCheck({})", builder);
    var running = true;

    for (MessageListenerContainer messageListenerContainer :
        this.kafkaListenerEndpointRegistry.getAllListenerContainers()) {
      if (!messageListenerContainer.isRunning()) running = false;
    }

    builder.status(running ? Status.UP : Status.DOWN);
  }
}
