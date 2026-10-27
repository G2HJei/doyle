package xyz.zlatanov.doyle.adapter.driving;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.zlatanov.doyle.core.port.driving.MatchEventOutcomeToBetsToSettleUseCase;

import java.time.Duration;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchEventOutcomeToBetsToSettleScheduledJobTest {

    @Mock
    MatchEventOutcomeToBetsToSettleUseCase useCase;

    @Test
    void shouldMatchUntilNothingLeft() {
        when(useCase.match()).thenReturn(true, true, false);
        new MatchEventOutcomeToBetsToSettleScheduledJob(useCase, Duration.ofMinutes(1)).run();
        verify(useCase, times(3)).match();
    }

    @Test
    void shouldMatchOnceWhenNothingLeft() {
        when(useCase.match()).thenReturn(false);
        new MatchEventOutcomeToBetsToSettleScheduledJob(useCase, Duration.ofMinutes(1)).run();
        verify(useCase).match();
    }

    @Test
    void shouldStopMatchingWhenOutOfTime() {
        when(useCase.match()).thenReturn(true);
        new MatchEventOutcomeToBetsToSettleScheduledJob(useCase, Duration.ZERO).run();
        verify(useCase).match();
    }
}
