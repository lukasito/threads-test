package main;

import com.sun.net.httpserver.HttpServer;

import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static java.math.RoundingMode.HALF_DOWN;

public class Main {

    private static final int NUMBER_OF_THREADS = 8;
    private static final Executor computationExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS, new ThreadNamingFactory("computing"));
    private static final Executor serverExecutor = Executors.newCachedThreadPool(new ThreadNamingFactory("server"));
    //    private static final Executor serverExecutor = computationExecutor;
    private static final Executor clientExecutor = Executors.newCachedThreadPool(new ThreadNamingFactory("client"));

    static class ThreadNamingFactory implements ThreadFactory {

        String prefix;
        AtomicInteger counter = new AtomicInteger(0);

        public ThreadNamingFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName(prefix + '-' + counter.getAndIncrement());
            return thread;
        }
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.setExecutor(serverExecutor);
        server.createContext("/", exchange -> {
            var result = compute();
            byte[] bytes = result.toString().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (var responseBody = exchange.getResponseBody()) {
                responseBody.write(bytes);
            }
        });
        server.start();
    }

    static class ComputationResult {
        String io;
        BigDecimal euler;
        long took;

        @Override
        public String toString() {
            return "{" +
                    "\"io\": " + io + ',' +
                    "\"euler\": " + euler.toEngineeringString() + ',' +
                    "\"took\": \"" + took + "ms\"" +
                    '}';
        }
    }

    private static ComputationResult compute() {
        long start = System.currentTimeMillis();
//        var euler = eulerAsync(1500);
        var euler = euler(1500);
        var io = io();
//        var io = ioAsync();

        var result = new ComputationResult();
//        result.euler = euler.join();
//        result.io = io.join();
        result.euler = euler;
        result.io = io;
        long took = System.currentTimeMillis() - start;
        System.out.println("Computation took: " + took + " ms");
        result.took = took;
        return result;
    }

    public static CompletableFuture<BigDecimal> eulerAsync(int iterations) {
        return CompletableFuture.supplyAsync(() -> euler(iterations), computationExecutor);
    }

    public static CompletableFuture<String> ioAsync() {
        return CompletableFuture.supplyAsync(Main::io, serverExecutor);
    }

    // this is very inefficient algorithm can take up to ~1s with 2000 iterations (depending on hw)
    public static BigDecimal euler(int iterations) {
        System.out.println("Euler on :" + Thread.currentThread());
//        monitor("Euler", Thread.currentThread());
        long start = System.currentTimeMillis();
        BigDecimal result = BigDecimal.ZERO;
        for (int i = 0; i < iterations; i++) {
            result = result.add(BigDecimal.ONE.setScale(1000, HALF_DOWN).divide(factorial(i), HALF_DOWN));
        }
        System.out.println("Euler took: " + (System.currentTimeMillis() - start) + " ms");
        return result;
    }

    private static BigDecimal factorial(int number) {
        var result = BigDecimal.ONE;
        if (number == 1 || number == 0) return result;
        for (int i = number; i > 0; i--) {
            result = result.multiply(new BigDecimal(i));
        }
        return result.setScale(1000, HALF_DOWN);
    }

    private static final HttpClient httpClient = HttpClient
            .newBuilder()
            .executor(clientExecutor)
            .build();

    private static String io() {
        System.out.println("IO on :" + Thread.currentThread());
        try {
            var start = System.currentTimeMillis();
            var body = httpClient.send(

                    HttpRequest.newBuilder()
                            .GET()
                            .uri(new URI("http://localhost:9090/wait"))
                            .build(),

                    HttpResponse.BodyHandlers.ofString()
            ).body();
            System.out.println(body);
            System.out.println("IO took: " + (System.currentTimeMillis() - start) + " ms");
            return body;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
