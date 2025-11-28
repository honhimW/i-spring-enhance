package io.github.honhimw.example.domain.jimmer;

import io.github.honhimw.spring.BuildIn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author honhimW
 * @since 2025-06-16
 */

@Slf4j
@Component
public class BuildInJimmerData implements BuildIn {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private NameJimmerRepository nameJimmerRepository;

    @Override
    public void setup() {
        Name name = nameJimmerRepository.save(new NameDraft.Builder().id("111")
            .firstName("John")
            .lastName("Doe")
            .build());
        Name chambers = NameDraft.$.produce(name, draft -> draft.setLastName("Chambers"));
        Player save = playerRepository.save(PlayerDraft.$.produce(draft -> draft
            .setId("10")
            .setAge(18)
            .setFullName(chambers)
            .setFullNameId("111")
            .applySbd(draft1 -> draft1
                .setSquat(135)
                .setBenchPress(102)
                .setDeadLift(175)
            )
            .setBitflags(0b111)
        ));
        log.info("player: {}", save);
        save.print();
    }
}
