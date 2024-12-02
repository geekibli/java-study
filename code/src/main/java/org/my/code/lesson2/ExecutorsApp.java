package org.my.code.lesson2;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorsApp {

    public static void main(String[] args) {

        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.execute(() -> {
            try {
                Thread.sleep(2000);
                System.out.println("Thread run");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
//        executorService.shutdownNow();
        executorService.shutdown();
        System.out.println("Main run");
    }

}
