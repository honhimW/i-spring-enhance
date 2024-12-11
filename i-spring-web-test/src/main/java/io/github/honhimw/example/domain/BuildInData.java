package io.github.honhimw.example.domain;

import io.github.honhimw.spring.BuildIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author hon_him
 * @since 2024-11-29
 */

@Component
public class BuildInData implements BuildIn {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private NameRepository nameRepository;

    @Override
    public void setup() {
        NameDO name = new NameDO();
        name.setId("1");
        name.setFirstName("hon_him");
        name.setLastName("w");
        nameRepository.save(name);

        PersonDO person = new PersonDO();
        person.setId("1");
        person.setFullName(name);
        personRepository.save(person);
    }
}
