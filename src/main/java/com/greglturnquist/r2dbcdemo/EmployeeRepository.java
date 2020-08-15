package com.greglturnquist.r2dbcdemo;

import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Flux;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface EmployeeRepository extends ReactiveCrudRepository<Employee, Long> {

	Flux<Employee> findByNameContains(String partialName);

	Flux<Employee> findByNameContainsOrRoleContains(String nameContains, String roleContains);

}
