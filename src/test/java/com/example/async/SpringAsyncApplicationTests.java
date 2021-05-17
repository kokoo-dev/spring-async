package com.example.async;

import com.example.async.test.service.TestService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@SpringBootTest
class SpringAsyncApplicationTests {

	Logger logger = LoggerFactory.getLogger(SpringAsyncApplicationTests.class);

	@Autowired
	TestService testService;

	@Test
	void testAsync() {
		logger.info("Async Start!!");

		for(int i=0; i<100; i++){
			testService.asyncMethod(i);
		}

		for(int i=0; i<100; i++){
			testService.syncMethod(i);
		}

		for(int i=0; i<100; i++){
			testService.completableFutureMethod(i);
		}

		logger.info("Async End!!");
	}

	@Test
	void testAsyncReturn() throws ExecutionException, InterruptedException {
		Future<String> future = testService.asyncReturnFuture();
		logger.info(future.get());

		ListenableFuture<String> listenableFuture = testService.asyncReturnListenableFuture();

		listenableFuture.addCallback((result) ->{
			logger.info("callBack :: " + result);
		}, (e) ->{
			logger.error("error :: " + e.getMessage());
		});

		//N개 비동기 결과 일괄 처리
		CompletableFuture completableFuture1 = testService.asyncReturnCompletableFuture(1);
		CompletableFuture completableFuture2 = testService.asyncReturnCompletableFuture(2);
		CompletableFuture completableFuture3 = testService.asyncReturnCompletableFuture(3);
		CompletableFuture completableFuture4 = testService.asyncReturnCompletableFuture(4);
		CompletableFuture completableFuture5 = testService.asyncReturnCompletableFuture(5);
		CompletableFuture completableFuture6 = testService.asyncReturnCompletableFuture(6);

		CompletableFuture combineFuture = CompletableFuture.allOf(completableFuture1, completableFuture2, completableFuture3, completableFuture4, completableFuture5, completableFuture6);

//		allOf get()은 null을 리턴
//		logger.info("result :: " + combineFuture.get());

		//allOf는 Stream 사용하여 결과 처리
		Stream.of(completableFuture1, completableFuture2, completableFuture3, completableFuture4, completableFuture5, completableFuture6)
				.map(CompletableFuture::join)
				.forEach(result -> {
					logger.info("result:: " + result);
				});

	}

}
