package com.greglturnquist.r2dbcdemo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmployeeController {

	private final EmployeeRepository repository;
	private final DatabaseClient dbClient;

	public EmployeeController(EmployeeRepository repository, DatabaseClient dbClient) {

		this.repository = repository;
		this.dbClient = dbClient;
	}

	/**
	 * An aggregate root to fetch ALL {@link Employee}s.
	 */
	@GetMapping("/api/employees")
	Flux<Employee> all() {
		return this.repository.findAll();
	}

	/**
	 * Create new {@link Employee}s through the aggregate root.
	 *
	 * @param newEmployee
	 */
	@PostMapping("/api/employees")
	Mono<ResponseEntity<Employee>> create(@RequestBody Employee newEmployee) {

		return this.repository //
				.save(new Employee(newEmployee.getName(), newEmployee.getRole())) //
				.map(ResponseEntity::ok);
	}

	/**
	 * Fetch a single-item {@link Employee}.
	 *
	 * @param id
	 */
	@GetMapping("/api/employees/{id}")
	Mono<Employee> one(@PathVariable long id) {
		return this.repository.findById(id);
	}

	/**
	 * Update/replace an {@link Employee} at a designated location.
	 *
	 * @param id
	 * @param employee
	 */
	@PutMapping("/api/employees/{id}")
	Mono<ResponseEntity<?>> createOrReplace(@PathVariable Long id, @RequestBody Employee employee) {

		return this.repository.existsById(id) //
				.map(exists -> exists ? update(id, employee) : insert(id, employee)) //
				.map(ResponseEntity::ok);
	}

	/**
	 * Update an existing {@link Employee}. Will cause an exception if the row doesn't already exist.
	 *
	 * @param id
	 * @param employeeToUpdate
	 */
	private Mono<Employee> update(Long id, Employee employeeToUpdate) {

		return this.repository.findById(id) //
				.map(employee -> new Employee(id, employeeToUpdate.getName(), employeeToUpdate.getRole())) //
				.flatMap(this.repository::save);
	}

	/**
	 * Insert a new {@link Employee}. Will cause an exception if you try to insert the same row twice.
	 *
	 * @param id
	 * @param employeeToInsert
	 */
	private Mono<Employee> insert(Long id, Employee employeeToInsert) {

		return this.dbClient //
				.insert() //
				.into(Employee.class) //
				.using(new Employee(id, employeeToInsert.getName(), employeeToInsert.getRole())) //
				.then() //
				.then(this.repository.findById(id));
	}

	/**
	 * Create some form of a "search" to find {@link Employee}s based on varying criteria.
	 *
	 * @param params
	 */
	@GetMapping("/api/employees/search")
	ResponseEntity<?> searchPartialName(@RequestParam Map<String, String> params) {

		if (params.containsKey("nameContains")) {
			return ResponseEntity.ok(this.repository.findByNameContains(params.get("nameContains")));
		} else if (params.containsKey("contains")) {
			String contains = params.get("contains");
			return ResponseEntity.ok(this.repository.findByNameContainsOrRoleContains(contains, contains));
		}

		return ResponseEntity //
				.badRequest() //
				.body("You must use either '?nameContains' or '?contains'");
	}
}
