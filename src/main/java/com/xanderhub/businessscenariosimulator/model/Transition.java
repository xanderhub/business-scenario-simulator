package com.xanderhub.businessscenariosimulator.model;


import com.xanderhub.businessscenariosimulator.repo.TransitionRepository;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;

@Entity
@SuperBuilder
@Getter
@NoArgsConstructor
public class Transition implements Storable<Transition, TransitionRepository>{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private State sourceState;
    @ManyToOne
    private State targetState;
    @OneToOne
    private Event onEvent;

    @Override
    public Transition saveTo(TransitionRepository repository) {
        return repository.save(this);
    }
}
