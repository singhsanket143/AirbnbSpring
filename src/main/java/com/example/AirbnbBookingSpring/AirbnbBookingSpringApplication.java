package com.example.AirbnbBookingSpring;


import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import io.github.cdimascio.dotenv.internal.DotenvParser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Map;
import java.util.Set;

@SpringBootApplication
@EnableJpaAuditing
public class AirbnbBookingSpringApplication {

	Dotenv dotenv = Dotenv.configure()
			.ignoreIfMalformed()
			.ignoreIfMissing()
			.load();


	public static void main(String[] args) {

		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMalformed()
				.ignoreIfMissing()
				.load();


		for(DotenvEntry e : dotenv.entries()){
			dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
		}
		SpringApplication.run(AirbnbBookingSpringApplication.class, args);
	}

}
