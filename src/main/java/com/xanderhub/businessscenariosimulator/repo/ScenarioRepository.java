package com.xanderhub.businessscenariosimulator.repo;

import com.xanderhub.businessscenariosimulator.model.Scenario;
import org.springframework.data.repository.CrudRepository;

public interface ScenarioRepository extends CrudRepository<Scenario, Long> {
    Scenario findByName(String name);
}
