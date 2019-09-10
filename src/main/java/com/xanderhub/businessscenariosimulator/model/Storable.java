package com.xanderhub.businessscenariosimulator.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

public interface Storable<T, R extends CrudRepository> {
   T saveTo(R repository);
}
