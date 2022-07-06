package jco.ql.db.ds.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class Server {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplicationBuilder(Server.class)
				.properties("spring.config.name=settings")
				.properties("spring.config.location=config/")
				.properties("spring.shell.interactive.enabled=false")
				.profiles("server")
			.build();
		application.run(args);
	}

}
