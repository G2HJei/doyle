package xyz.zlatanov.doyle.core;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.zlatanov.doyle.core.domain.EventOutcome;
import xyz.zlatanov.doyle.core.domain.EventOutcomeMessage;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomePublisher;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomeRepository;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RelayEventOutcomesCommandProcessorTest {

    @Mock
    EventOutcomeRepository             outcomeRepository;
    @Mock
    EventOutcomePublisher              publisher;
    @InjectMocks
    RelayEventOutcomesCommandProcessor commandProcessor;

    EventOutcome outcome_1 = new EventOutcome()
            .eventId(UUID.randomUUID())
            .eventName("test event")
            .eventWinnerId(UUID.randomUUID())
            .published(false)
            .version(1);

    @Test
    void shouldDoNothingWhenNoUnpublished() {
        unpublishedEventOutcomes();
        commandProcessor.relayOutcomes();
        verify(publisher, never()).publish(any());
    }

    @Test
    void shouldRelayUnpublished() {
        unpublishedEventOutcomes(outcome_1);
        commandProcessor.relayOutcomes();
        verifyOutboxPublished(outcome_1);
    }

    void unpublishedEventOutcomes(EventOutcome... outcomes) {
        when(outcomeRepository.findUnpublished(anyInt()))
                .thenReturn(Arrays.stream(outcomes).toList());
    }

    EventOutcomeMessage expectedMessage(EventOutcome outcome) {
        return new EventOutcomeMessage()
                .eventId(outcome.eventId())
                .eventName(outcome.eventName())
                .eventWinnerId(outcome.eventWinnerId())
                .version(outcome.version());
    }

    void verifyOutboxPublished(EventOutcome outcome) {
        val inOrder = inOrder(publisher, outcomeRepository);
        inOrder.verify(publisher).publish(expectedMessage(outcome));
        inOrder.verify(outcomeRepository).saveAll(List.of(outcome.published(true)));
    }
}