package io.github.honhimw.example.domain.jpa.player;

import io.github.honhimw.example.domain.jpa.NameDO;
import io.github.honhimw.example.domain.jpa.NameRepository;
import io.github.honhimw.spring.BuildIn;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author hon_him
 * @since 2024-11-29
 */

//@Component
public class BuildInPlayer implements BuildIn {

    @Autowired
    private PlayerJpaRepository playerRepository;

    @Autowired
    private NameRepository nameRepository;

    @Override
    public void setup() {
        {
            NameDO name = new NameDO();
            name.setId("2");
            name.setFirstName("John");
            name.setLastName("Harker");
            nameRepository.save(name);
            PlayerDO player = new PlayerDO();
            player.setId("1");
            player.setFullName(name);
            player.setAge(29);
            player.setSbd(new PlayerDO.SBD(100, 200, 300));
            player.setBigflags(0b111);
            playerRepository.save(player);
        }
        {
            NameDO name = new NameDO();
            name.setId("3");
            name.setFirstName("Chris");
            name.setLastName("Bumstead");
            nameRepository.save(name);
            PlayerDO player = new PlayerDO();
            player.setId("2");
            player.setFullName(name);
            player.setAge(22);
            player.setSbd(new PlayerDO.SBD(100, 200, 300));
            playerRepository.save(player);
        }
    }
}
