package com.xanderhub.businessscenariosimulator.repo;

import com.xanderhub.businessscenariosimulator.model.Scenario;
import com.xanderhub.businessscenariosimulator.model.State;
import com.xanderhub.businessscenariosimulator.model.StateType;
import org.springframework.data.repository.CrudRepository;

public interface StateRepository extends CrudRepository<State, Long> {
    State findByScenarioAndType(Scenario scenario, StateType type);
}
