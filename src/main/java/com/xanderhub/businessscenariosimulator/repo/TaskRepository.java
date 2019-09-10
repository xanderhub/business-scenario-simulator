package com.xanderhub.businessscenariosimulator.repo;

import com.xanderhub.businessscenariosimulator.model.Task;
import org.springframework.data.repository.CrudRepository;

public interface TaskRepository extends CrudRepository<Task, Long> {

}

