package com.zhou.aqs;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;


/**
 * 4种常用AQS实现
 * {@link java.util.concurrent.locks.ReentrantLock}
 * {@link java.util.concurrent.CountDownLatch}
 * {@link java.util.concurrent.CyclicBarrier}
 * {@link java.util.concurrent.Semaphore}
 *
 * @author zhouchao
 * @since 2025/2/24 9:55
 */
public class AQSMain {
    public static void main(String[] args) {
        // AQS，基于 CLH 自旋锁算法实现
        AbstractQueuedSynchronizer aqs = new AbstractQueuedSynchronizer() {
            @Override
            protected boolean tryAcquire(int arg) {
                return super.tryAcquire(arg);
            }

            @Override
            protected boolean tryRelease(int arg) {
                return super.tryRelease(arg);
            }
        };
        /* 4种常用AQS实现 */
        ReentrantLock reentrantLock = new ReentrantLock();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        CyclicBarrier cyclicBarrier = new CyclicBarrier(1);
        Semaphore semaphore = new Semaphore(1);
    }

}
