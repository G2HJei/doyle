package xyz.zlatanov.doyle.config;

import lombok.val;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;
import xyz.zlatanov.doyle.adapter.driven.RocketMqBetSettlementPublisher;
import xyz.zlatanov.doyle.adapter.driving.RocketMqBetSettlementListener;
import xyz.zlatanov.doyle.core.port.driven.BetSettlementPublisher;
import xyz.zlatanov.doyle.core.port.driving.SettleBetUseCase;

import static org.apache.rocketmq.common.consumer.ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET;

@Configuration
public class RocketMqConfig {

    public static final String BET_SETTLEMENTS_TOPIC = "bet-settlements";

    private static final String PRODUCER_GROUP = "doyle-bet-settlements-producer";
    private static final String CONSUMER_GROUP = "doyle-bet-settlements-consumer";

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    DefaultMQProducer betSettlementProducer(@Value("${doyle.rocketmq.name-server}") String nameServer) {
        val producer = new DefaultMQProducer(PRODUCER_GROUP);
        producer.setNamesrvAddr(nameServer);
        return producer;
    }

    @Bean
    BetSettlementPublisher betSettlementPublisher(DefaultMQProducer betSettlementProducer, JsonMapper jsonMapper) {
        return new RocketMqBetSettlementPublisher(betSettlementProducer, jsonMapper, BET_SETTLEMENTS_TOPIC);
    }

    @Bean
    RocketMqBetSettlementListener rocketMqBetSettlementListener(SettleBetUseCase settleBetUseCase,
                                                                JsonMapper jsonMapper) {
        return new RocketMqBetSettlementListener(settleBetUseCase, jsonMapper);
    }

    // consume from the first offset so settlements sent before this group first subscribed are not skipped
    @Bean(initMethod = "start", destroyMethod = "shutdown")
    DefaultMQPushConsumer betSettlementConsumer(@Value("${doyle.rocketmq.name-server}") String nameServer, RocketMqBetSettlementListener listener) throws MQClientException {
        val consumer = new DefaultMQPushConsumer(CONSUMER_GROUP);
        consumer.setNamesrvAddr(nameServer);
        consumer.setConsumeFromWhere(CONSUME_FROM_FIRST_OFFSET);
        consumer.subscribe(BET_SETTLEMENTS_TOPIC, "*");
        consumer.registerMessageListener(listener);
        return consumer;
    }
}
