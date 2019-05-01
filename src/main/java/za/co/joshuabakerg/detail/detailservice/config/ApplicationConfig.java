package za.co.joshuabakerg.detail.detailservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class ApplicationConfig {

    @Profile("prod")
    @Bean("authServer")
    public String prodAuthServer(){
        return "https://joshuabakerg-auth-service.herokuapp.com";
    }

    @Profile("!prod")
    @Bean("authServer")
    public String localAuthServer(){
        return "http://localhost:8080";
    }

    @Bean
    public OkHttpClient okHttpClient(){
        return new OkHttpClient();
    }

    @Bean
    public ObjectMapper detailServiceObjectMapper(){
        return new ObjectMapper();
    }

}
