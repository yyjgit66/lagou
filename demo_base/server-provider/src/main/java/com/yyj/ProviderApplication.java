package com.yyj;

import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.io.IOException;

public class ProviderApplication {
    public static void main(String[] args) throws IOException {
        AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(ProviderConfiguration.class);
        annotationConfigApplicationContext.start();
        System.in.read();
    }

    @Configuration
    @EnableDubbo(scanBasePackages = "com.yyj.server.impl")
    @PropertySource("classpath:/dubbo-provider.properties")
    static class ProviderConfiguration{
        @Bean
        public RegistryConfig registryConfig(){
            RegistryConfig registryConfig = new RegistryConfig();
            registryConfig.setAddress("zookeeper://192.168.1.6:2181?timeout=10000");
            return  registryConfig;
        }
    }
}
