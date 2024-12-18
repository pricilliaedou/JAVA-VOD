package fr.vod;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

	@Value("${app.url}")
	private String allowedOrigin;

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**") // Allow all endpoints
//	                        .allowedOrigins("http://localhost:9090") // Allow React app origin
						.allowedOrigins(allowedOrigin)
						.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allow HTTP  methods
						.allowedHeaders("*") // Allow all headers
						.allowCredentials(true); // Allow credentials (cookies, etc.)
			}
		};
	}
}

//package fr.vod;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//public class CorsConfig implements WebMvcConfigurer {
//
//    @Value("${app.url}")
//    private String allowedOrigin;
//
//@Override
//public void addCorsMappings(CorsRegistry registry) {
//
//registry.addMapping("/**")
//.allowedOrigins(allowedOrigin)
//.allowedMethods("*")
//.allowedHeaders("*")
//.allowCredentials(true);
//}
//}
