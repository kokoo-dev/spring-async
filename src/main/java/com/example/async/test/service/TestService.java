package com.example.async.test.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Service
public class TestService {
    Logger logger = LoggerFactory.getLogger(TestService.class);

    @Autowired
    AsyncService asyncService;

    public void syncMethod(int num){
        asyncService.run(() -> asyncMethod(num));
    }

    public void completableFutureMethod(int num){
        CompletableFuture.runAsync(() -> asyncMethod(num));
    }

    @Async
    public void asyncMethod(int num){
        logger.info("num :: " + num);
    }

    @Async
    public Future<String> asyncReturnFuture(){
        String result = "Async Done";
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ie){
            result = "Async Fail";
        }

        return new AsyncResult<>(result);
    }

    @Async
    public ListenableFuture<String> asyncReturnListenableFuture() throws InterruptedException {
        String result = "Async Done";
        logger.info("call asyncReturnListenableFuture!");

        return new AsyncResult<>(result);
    }

    @Async
    public CompletableFuture<String> asyncReturnCompletableFuture(int num) throws InterruptedException {
        String result = "Async Done" + num;
        logger.info("call_" + num);
        Thread.sleep(1000);

        return CompletableFuture.completedFuture(result);
    }
}
