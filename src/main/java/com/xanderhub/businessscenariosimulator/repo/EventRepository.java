package com.xanderhub.businessscenariosimulator.repo;

import com.xanderhub.businessscenariosimulator.model.Event;
import org.springframework.data.repository.CrudRepository;

public interface EventRepository extends CrudRepository<Event, Long> {
    Event findByName(String name);
}
