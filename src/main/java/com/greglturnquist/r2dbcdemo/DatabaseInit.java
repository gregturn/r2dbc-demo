package com.greglturnquist.r2dbcdemo;

import io.r2dbc.spi.ConnectionFactory;
import reactor.test.StepVerifier;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.connectionfactory.init.ConnectionFactoryInitializer;
import org.springframework.data.r2dbc.connectionfactory.init.ResourceDatabasePopulator;

@Configuration
public class DatabaseInit {

	@Bean
	public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {

		ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
		initializer.setConnectionFactory(connectionFactory);
		initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("schema.sql")));
		return initializer;
	}

	@Bean
	public CommandLineRunner init(EmployeeRepository repository) {

		return args -> {
			repository //
					.save(new Employee("Frodo Baggins", "ring bearer")) //
					.as(StepVerifier::create) //
					.expectNextCount(1) //
					.verifyComplete();

			repository //
					.save(new Employee("Bilbo Baggins", "burglar")) //
					.as(StepVerifier::create) //
					.expectNextCount(1) //
					.verifyComplete();
		};
	}

}
