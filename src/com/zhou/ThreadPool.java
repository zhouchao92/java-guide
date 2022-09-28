package com.zhou;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ThreadPool
 *
 * @author 周超
 * @since 2022/9/22 17:05
 */
public class ThreadPool {
    public static void main(String[] args) {
        // 核心线程数
        // 最大线程数
        // 最大存活时间
        // 单位
        // 阻塞队列
        // 线程池工厂
        // 拒绝策略
        ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 100, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10));


        /*
            public static ExecutorService newFixedThreadPool(int nThreads) {
                return new ThreadPoolExecutor(nThreads, nThreads,
                                      0L, TimeUnit.MILLISECONDS,
                                      new LinkedBlockingQueue<Runnable>());
            }
         */
        ExecutorService executorService = Executors.newFixedThreadPool(1);

    }
}
