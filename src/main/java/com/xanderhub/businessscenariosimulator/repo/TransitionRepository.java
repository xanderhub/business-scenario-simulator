package com.xanderhub.businessscenariosimulator.repo;

import com.xanderhub.businessscenariosimulator.model.Transition;
import org.springframework.data.repository.CrudRepository;

public interface TransitionRepository extends CrudRepository<Transition, Long> {
}
