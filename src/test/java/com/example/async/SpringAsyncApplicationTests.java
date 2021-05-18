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
import java.util.concurrent.*;
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
	void testFuture() throws ExecutionException, InterruptedException {
		Future<String> future = testService.asyncReturnFuture();
		logger.info(future.get());

	}

	@Test
	void testListenableFuture() throws InterruptedException {
		ListenableFuture<String> listenableFuture = testService.asyncReturnListenableFuture();

		listenableFuture.addCallback((result) ->{
			logger.info("callBack :: " + result);
		}, (e) ->{
			logger.error("error :: " + e.getMessage());
		});
	}

	@Test
	void testAllOf() throws InterruptedException, ExecutionException {
		//N개 비동기 결과 일괄 처리
		CompletableFuture future1 = testService.asyncReturnCompletableFuture(1);
		CompletableFuture future2 = testService.asyncReturnCompletableFuture(2);
		CompletableFuture future3 = testService.asyncReturnCompletableFuture(3);
		CompletableFuture future4 = testService.asyncReturnCompletableFuture(4);
		CompletableFuture future5 = testService.asyncReturnCompletableFuture(5);
		CompletableFuture future6 = testService.asyncReturnCompletableFuture(6);

		CompletableFuture allOfFuture = CompletableFuture.allOf(future1, future2, future3, future4, future5, future6);

//		allOf get()은 null을 리턴
		logger.info("result :: " + allOfFuture.get());

//		allOf는 Stream 사용하여 결과 처리
		Stream.of(future1, future2, future3, future4, future5, future6)
				.map(CompletableFuture::join)
				.forEach(result -> {
					logger.info("result:: " + result);
				});
	}

	@Test
	void testAnyOf() throws InterruptedException, ExecutionException {
		//N개 비동기 중 가장 빨리 처리되는 결과 처리
		CompletableFuture future1 = testService.asyncReturnCompletableFuture(1);
		CompletableFuture future2 = testService.asyncReturnCompletableFuture(2);
		CompletableFuture future3 = testService.asyncReturnCompletableFuture(3);
		CompletableFuture future4 = testService.asyncReturnCompletableFuture(4);
		CompletableFuture future5 = testService.asyncReturnCompletableFuture(5);
		CompletableFuture future6 = testService.asyncReturnCompletableFuture(6);

		CompletableFuture anyOfFuture = CompletableFuture.anyOf(future1, future2, future3, future4, future5, future6);

		logger.info("anyOfFuture :: " + anyOfFuture.get());
	}

	@Test
	void testThenApply() throws InterruptedException, ExecutionException {
		//thenApply : 반환 전 결과 후 처리
		CompletableFuture futureThenApply1 = testService
				.asyncReturnCompletableFuture(10)
				.thenApply(result -> result + "_thenApply");

		logger.info("futureThenApply1 = " + futureThenApply1.get());
		logger.info("futureThenApply1 Done = " + futureThenApply1.isDone());

		//thenApply 연달아 사용 가능
		CompletableFuture futureThenApply2 = testService
				.asyncReturnCompletableFuture(10)
				.thenApply(result -> result + "_thenApply")
				.thenApply(result -> result + "_Add");

		logger.info("futureThenApply2 = " + futureThenApply2.get());
	}

	@Test
	void testThenCompose() throws InterruptedException, ExecutionException {
		//thenCompose : 반환 전 future를 순차적으로 실행
		CompletableFuture futureThenCompose = testService.asyncReturnCompletableFuture(10)
				.thenCompose(result -> CompletableFuture.supplyAsync(() -> result + "_Add1"))
				.thenCompose(result -> CompletableFuture.supplyAsync(() -> result + "_Add2"));

		logger.info((String) futureThenCompose.get());
	}

	@Test
	void testThenCombine() throws InterruptedException, ExecutionException {
		CompletableFuture future1 = testService.asyncReturnCompletableFuture(1);
		CompletableFuture future2 = testService.asyncReturnCompletableFuture(2);
		CompletableFuture future3 = testService.asyncReturnCompletableFuture(3);
		CompletableFuture future4 = testService.asyncReturnCompletableFuture(4);

		CompletableFuture<String> combineFuture = future1
				.thenCombine(future3, (s1, s2) -> s1 + " + " + s2)
				.thenCombine(future2, (s1, s2) -> s1 + " + " + s2)
				.thenCombine(future4, (s1, s2) -> s1 + " + " + s2);
//				.thenAccept((result) -> logger.info("result :: " + result));

		//작업은 병렬로 처리되나 출력은 thenCombine 순서에 따라 처리
		logger.info("future1 :: " + combineFuture.get());

		//Async Done_1 출력
		logger.info("future1 :: " + future1.get());
	}

	@Test
	void testGetException() {
		CompletableFuture future = null;

		try {
			future = testService.asyncReturnCompletableFuture(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		String print = "";

		try {
			print = (String) future.get();
		} catch (InterruptedException e){
			print = e.getMessage();
		} catch (ExecutionException e){
			print = e.getMessage();
		} catch (CancellationException e){
			print = e.getMessage();
		} finally {
			logger.info("finally :: " + print);
		}
	}

}
