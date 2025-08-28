package com.enigma.library_app;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
@EnableMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
@SpringBootApplication
@EnableScheduling
@OpenAPIDefinition(
		info = @Info(
				title = "Library System REST API Documentation",
				description = "REST API documentation for the Library Management System, including book management, loan, fines, bot telegram and reporting.",
				version = "v1",
				contact = @Contact(
						name = "Library Development Team",
						url = "http://localhost:8080"
				),
				license = @License(
						name = "Apache License 2.0",
						url = "https://www.apache.org/licenses/LICENSE-2.0"
				)
		),
		externalDocs = @ExternalDocumentation(
				description = "Library System Swagger UI",
				url = "http://localhost:8080/swagger-ui/index.html"
		)
)
public class LibraryAppApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();
		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

		SpringApplication.run(LibraryAppApplication.class, args);
	}

}
