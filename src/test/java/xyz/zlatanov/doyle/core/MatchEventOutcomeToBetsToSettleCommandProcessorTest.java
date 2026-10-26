package xyz.zlatanov.doyle.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.zlatanov.doyle.core.port.driven.BetSettlementPublisher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MatchEventOutcomeToBetsToSettleCommandProcessorTest {

    @Mock
    BetSettlementPublisher                          publisher;
    @InjectMocks
    MatchEventOutcomeToBetsToSettleCommandProcessor commandProcessor;

    @Test
    void shouldDoNothingWhenNoPendingJobs() {
        commandProcessor.match();
        verify(publisher, never()).publish(any());
    }
}