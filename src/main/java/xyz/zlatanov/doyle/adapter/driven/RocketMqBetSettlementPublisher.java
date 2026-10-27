package xyz.zlatanov.doyle.adapter.driven;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import tools.jackson.databind.json.JsonMapper;
import xyz.zlatanov.doyle.core.domain.BetSettlementMessage;
import xyz.zlatanov.doyle.core.port.driven.BetSettlementPublisher;

import static org.apache.rocketmq.client.producer.SendStatus.SEND_OK;

@RequiredArgsConstructor
public class RocketMqBetSettlementPublisher implements BetSettlementPublisher {

    private final DefaultMQProducer producer;
    private final JsonMapper        jsonMapper;
    private final String            topic;

    @Override
    public void publish(BetSettlementMessage message) {
        val msg = new Message(topic, jsonMapper.writeValueAsBytes(message));
        //todo add partition key (user id)
        try {
            val result = producer.send(msg);
            if (result.getSendStatus() != SEND_OK) {
                throw new IllegalStateException("Bet settlement for bet %s not sent: %s".formatted(message.betId(), result.getSendStatus()));
            }
        } catch (MQClientException | RemotingException | MQBrokerException e) {
            throw new IllegalStateException("Bet settlement for bet %s not sent".formatted(message.betId()), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Bet settlement for bet %s not sent".formatted(message.betId()), e);
        }
    }
}
