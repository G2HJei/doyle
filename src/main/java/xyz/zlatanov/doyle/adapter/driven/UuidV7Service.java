package xyz.zlatanov.doyle.adapter.driven;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.NoArgGenerator;
import xyz.zlatanov.doyle.core.port.driven.UUIDService;

import java.util.UUID;

public class UuidV7Service implements UUIDService {

    private final NoArgGenerator generator = Generators.timeBasedEpochRandomGenerator();

    @Override
    public UUID get() {
        return generator.generate();
    }

}
