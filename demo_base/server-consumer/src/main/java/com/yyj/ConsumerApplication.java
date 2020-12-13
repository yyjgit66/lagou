package com.yyj;

import com.yyj.bean.ConsumerCompoant;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConsumerApplication {

    public static void main(String[] args) throws InterruptedException {
        AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(ConsumerConfiguration.class);
        annotationConfigApplicationContext.start();
        ConsumerCompoant bean = annotationConfigApplicationContext.getBean(ConsumerCompoant.class);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        while (true){
            executorService.submit(()-> System.out.println(bean.sayHello("world",100)));
            executorService.submit(() ->System.out.println(bean.sayHello2("world",100)));
            executorService.submit(() ->System.out.println(bean.sayHello3("world",100)));
            TimeUnit.MILLISECONDS.sleep(20);
        }
    }
    @Configuration
    @PropertySource("classpath:/dubbo-consumer.properties")
    @ComponentScan(basePackages = "com.yyj.bean")
    @EnableDubbo
    static class ConsumerConfiguration{

    }
}
