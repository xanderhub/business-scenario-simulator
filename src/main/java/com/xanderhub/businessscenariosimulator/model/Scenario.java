package com.xanderhub.businessscenariosimulator.model;

import com.xanderhub.businessscenariosimulator.repo.ScenarioRepository;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;

@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Scenario implements Storable<Scenario, ScenarioRepository> {
    @Transient
    private final Logger LOG = LoggerFactory.getLogger(Scenario.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;

    @Override
    public Scenario saveTo(ScenarioRepository repository) {
        return repository.save(this);
    }
}
