package com.yyj.server.impl;

import com.yyj.server.HelloServer;
import org.apache.dubbo.config.annotation.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class HelloServerImpl implements HelloServer {
    @Override
    public String sayHello(String name, int timeToWait) {
        int i = new Random().nextInt(timeToWait);
        try {
            TimeUnit.SECONDS.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return "hello"+name;
    }

    @Override
    public String sayHello2(String name, int timeToWait) {
        int i = new Random().nextInt(timeToWait);
        try {
            TimeUnit.SECONDS.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return "hello2"+name;
    }

    @Override
    public String sayHello3(String name, int timeToWait) {
        int i = new Random().nextInt(timeToWait);
        try {
            TimeUnit.SECONDS.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return "hello3"+name;
    }
}
