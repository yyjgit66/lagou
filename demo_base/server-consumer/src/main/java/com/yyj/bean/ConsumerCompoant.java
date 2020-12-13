package com.yyj.bean;

import com.yyj.server.HelloServer;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Component;

@Component
public class ConsumerCompoant {

    @Reference
    private HelloServer helloServer;

    public String sayHello(String name, int timeToWait) {
        return helloServer.sayHello(name, timeToWait);
    }
    public String sayHello2(String name, int timeToWait) {
        return helloServer.sayHello2(name, timeToWait);
    }
    public String sayHello3(String name, int timeToWait) {
        return helloServer.sayHello3(name, timeToWait);
    }
}
