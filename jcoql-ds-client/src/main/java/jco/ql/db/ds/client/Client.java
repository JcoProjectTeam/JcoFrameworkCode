package jco.ql.db.ds.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class Client {
	private static final Logger logger = LoggerFactory.getLogger(Client.class);
	
	public static void main(String[] args) {
		String profile = "shell";
		if(args != null && args.length > 0 && "-WEB".equals(args[0].toUpperCase())) {
			profile = "web";
		}
		
		logger.info("Starting JCoDS Client with profile {}", profile);
		
		new SpringApplicationBuilder(Client.class)
			.profiles(profile)
			.build()
			.run(args);
	}
	
}
