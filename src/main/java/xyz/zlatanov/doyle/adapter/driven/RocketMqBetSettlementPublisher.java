package xyz.zlatanov.doyle.adapter.driven;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.selector.SelectMessageQueueByHash;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import tools.jackson.databind.json.JsonMapper;
import xyz.zlatanov.doyle.core.domain.message.BetSettlementMessage;
import xyz.zlatanov.doyle.core.port.driven.BetSettlementPublisher;

import static org.apache.rocketmq.client.producer.SendStatus.SEND_OK;

@RequiredArgsConstructor
public class RocketMqBetSettlementPublisher implements BetSettlementPublisher {

    private static final MessageQueueSelector BY_KEY_HASH = new SelectMessageQueueByHash();

    private final DefaultMQProducer producer;
    private final JsonMapper        jsonMapper;
    private final String            topic;

    @Override
    public void publish(BetSettlementMessage settlement) {
        val message = new Message(topic, jsonMapper.writeValueAsBytes(settlement));
        val partitionKey = settlement.userId().toString();
        try {
            val status = producer.send(message, BY_KEY_HASH, partitionKey).getSendStatus();
            if (status != SEND_OK) {
                throw notSent(settlement, "send status " + status, null);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw notSent(settlement, "interrupted", e);
        } catch (MQClientException | RemotingException | MQBrokerException e) {
            throw notSent(settlement, e.getMessage(), e);
        }
    }

    private static IllegalStateException notSent(BetSettlementMessage settlement, String reason, Throwable cause) {
        return new IllegalStateException(
                "Bet settlement for bet %s not sent: %s".formatted(settlement.betId(), reason), cause);
    }
}