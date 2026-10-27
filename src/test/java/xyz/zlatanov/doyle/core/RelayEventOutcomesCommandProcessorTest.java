package xyz.zlatanov.doyle.core;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.zlatanov.doyle.core.domain.entity.EventOutcome;
import xyz.zlatanov.doyle.core.domain.id.CompetitorId;
import xyz.zlatanov.doyle.core.domain.id.EventId;
import xyz.zlatanov.doyle.core.domain.id.EventOutcomeId;
import xyz.zlatanov.doyle.core.domain.message.EventOutcomeMessage;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomePublisher;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomeRepository;

import java.util.Arrays;
import java.util.List;

import static java.util.UUID.randomUUID;
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
            .id(new EventOutcomeId(randomUUID()))
            .eventId(new EventId(randomUUID()))
            .eventName("test event")
            .eventWinnerId(new CompetitorId(randomUUID()))
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
        return new EventOutcomeMessage(outcome);
    }

    void verifyOutboxPublished(EventOutcome outcome) {
        val inOrder = inOrder(publisher, outcomeRepository);
        inOrder.verify(publisher).publish(expectedMessage(outcome));
        inOrder.verify(outcomeRepository).markPublished(List.of(outcome));
    }
}