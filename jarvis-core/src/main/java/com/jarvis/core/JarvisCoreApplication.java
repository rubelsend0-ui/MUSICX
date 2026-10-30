package com.jarvis.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class JarvisCoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(JarvisCoreApplication.class, args);
    }
}
