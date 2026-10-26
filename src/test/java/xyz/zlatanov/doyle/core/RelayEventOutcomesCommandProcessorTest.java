package xyz.zlatanov.doyle.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomePublisher;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomeRepository;

import java.util.List;

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

    @Test
    void shouldDoNothingWhenNoUnpublished() {
        when(outcomeRepository.findUnpublished(anyInt())).thenReturn(List.of());
        commandProcessor.relayOutcomes();
        verify(publisher, never()).publish(any());
    }
}