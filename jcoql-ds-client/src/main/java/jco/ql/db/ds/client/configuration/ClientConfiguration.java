package jco.ql.db.ds.client.configuration;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.jline.PromptProvider;

import jco.ql.db.ds.core.client.service.ClientConnectionManager;

@Configuration
@EnableConfigurationProperties({
	ClientConnectionProperties.class
})
public class ClientConfiguration {
	
	@Bean
	public ClientConnectionManager clientConnectionManager(ClientConnectionProperties props) {
		return new ClientConnectionManager(props.getHost(), props.getPort());
	}
	
	@Bean
	public PromptProvider promptProvider() {
		return new PromptProvider() {
			
			@Override
			public AttributedString getPrompt() {
				return new AttributedString("JCoDS:>",
                        AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
			}
		};
	}

}
