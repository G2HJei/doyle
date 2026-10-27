package xyz.zlatanov.doyle.adapter.driving;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.message.MessageExt;
import tools.jackson.databind.json.JsonMapper;
import xyz.zlatanov.doyle.core.domain.command.SettleBetCommand;
import xyz.zlatanov.doyle.core.domain.id.BetId;
import xyz.zlatanov.doyle.core.domain.message.BetSettlementMessage;
import xyz.zlatanov.doyle.core.port.driving.SettleBetUseCase;

import java.util.List;

import static org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus.SUCCESS;
import static org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;

// orderly: messages of a queue (all settlements of a user) are consumed one at a time, in the order they were sent
@Slf4j
@RequiredArgsConstructor
public class RocketMqBetSettlementListener implements MessageListenerOrderly {

    private final SettleBetUseCase settleBetUseCase;
    private final JsonMapper       jsonMapper;

    @Override
    public ConsumeOrderlyStatus consumeMessage(List<MessageExt> messages, ConsumeOrderlyContext context) {
        try {
            messages.forEach(this::settle); //todo batch instead of forEach
            return SUCCESS;
        } catch (RuntimeException e) {
            // suspending the queue keeps later settlements of the user behind the failed one
            log.warn("Bet settlement failed, queue suspended until redelivery", e);
            return SUSPEND_CURRENT_QUEUE_A_MOMENT;
        }
    }

    private void settle(MessageExt message) {
        val settlement = jsonMapper.readValue(message.getBody(), BetSettlementMessage.class);
        settleBetUseCase.settle(new SettleBetCommand(new BetId(settlement.betId())));
    }
}
