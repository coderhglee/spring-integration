package com.coderhglee.batch.config;

import java.util.concurrent.ThreadFactory;

public class CustomThreadsFactory implements ThreadFactory {

    private int count = 0;
    private String name;

    public CustomThreadsFactory(String name) {
        this.name = name;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, name +"-thread-"+ ++count); //Mythread-x 형태로 쓰레드 이름 설정
    }
}
