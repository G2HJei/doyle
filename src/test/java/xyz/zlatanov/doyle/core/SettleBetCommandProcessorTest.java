package xyz.zlatanov.doyle.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.zlatanov.doyle.core.domain.Bet;
import xyz.zlatanov.doyle.core.domain.EventOutcome;
import xyz.zlatanov.doyle.core.domain.SettleBetCommand;
import xyz.zlatanov.doyle.core.port.driven.BetRepository;
import xyz.zlatanov.doyle.core.port.driven.BetSettlementRepository;
import xyz.zlatanov.doyle.core.port.driven.EventOutcomeLookup;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static xyz.zlatanov.doyle.core.domain.Bet.BetStatus.PLACED;

@ExtendWith(MockitoExtension.class)
class SettleBetCommandProcessorTest {

    @Mock
    EventOutcomeLookup      eventOutcomeLookup;
    @Mock
    BetRepository           betRepository;
    @Mock
    BetSettlementRepository betSettlementRepository;

    @InjectMocks
    SettleBetCommandProcessor commandProcessor;

    EventOutcome outcome   = new EventOutcome()
            .eventId(UUID.randomUUID())
            .eventWinnerId(UUID.randomUUID())
            .version(1);
    Bet          losingBet = new Bet()
            .id(UUID.randomUUID())
            .eventId(outcome.eventId())
            .eventWinnerId(UUID.randomUUID()) //not the winner
            .status(PLACED);

    @Test
    void shouldSettleBetAsLost() {
        when(betRepository.findById(losingBet.id())).thenReturn(losingBet);
        when(eventOutcomeLookup.getEventOutcome(outcome.eventId())).thenReturn(outcome);

        commandProcessor.settle(new SettleBetCommand().betId(losingBet.id()));

        verify(betRepository).markLost(losingBet.id());
        verifyNoInteractions(betSettlementRepository);
    }
}