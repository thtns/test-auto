package cn.thtns.test.auto.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * @author liuyj
 * node
 */
@Configuration
public class RestClientConfig {

	@Bean
	public RestClient restClient() {
		return RestClient.builder().build();
	}
}
