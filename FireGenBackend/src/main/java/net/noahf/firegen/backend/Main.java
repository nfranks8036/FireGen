package net.noahf.firegen.backend;

import net.noahf.firegen.backend.database.DatabaseInitializer;
import net.noahf.firegen.backend.database.DatabaseManager;
import net.noahf.firegen.backend.structure.StructureManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class})
public class Main {

    public static DatabaseManager db;
    public static StructureManager st;

    public static void main(String[] args) {
        st = new StructureManager("MontCo");

        db = new DatabaseInitializer(null, null).database();

        SpringApplication.run(Main.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://127.0.0.1:5500", "http://localhost:5500", "http://127.0.0.1", "http://localhost")
                        .allowedMethods("GET", "POST", "PUT")
                        .allowedHeaders("*");
            }
        };
    }

}