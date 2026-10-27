package xyz.zlatanov.doyle.adapter.driving;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import tools.jackson.databind.json.JsonMapper;
import xyz.zlatanov.doyle.core.domain.BetSettlementMessage;
import xyz.zlatanov.doyle.core.domain.SettleBetCommand;
import xyz.zlatanov.doyle.core.port.driving.SettleBetUseCase;

import java.util.List;

import static org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
import static org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus.RECONSUME_LATER;

@Slf4j
@RequiredArgsConstructor
public class RocketMqBetSettlementListener implements MessageListenerConcurrently {

    private final SettleBetUseCase settleBetUseCase;
    private final JsonMapper       jsonMapper;

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messages, ConsumeConcurrentlyContext context) {
        try {
            messages.forEach(this::settle); //todo batch instead of forEach
            return CONSUME_SUCCESS;
        } catch (RuntimeException e) {
            log.warn("Bet settlement failed, will be redelivered", e);
            return RECONSUME_LATER;
        }
    }

    private void settle(MessageExt message) {
        val settlement = jsonMapper.readValue(message.getBody(), BetSettlementMessage.class);
        settleBetUseCase.settle(new SettleBetCommand().betId(settlement.betId()));
    }
}
