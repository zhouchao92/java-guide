package com.zhou;

import java.util.UUID;

/**
 * ThreadLocal
 * 正常情况下，子线程无法从 ThreadLocal 拿到父线程的数据，通过 InheritableThreadLocal 可以实现
 *
 * @author 周超
 * @since 2022/9/22 19:51
 */
public class ThreadLocalInheritable {
    public static void main(String[] args) {
        java.lang.ThreadLocal<String> inheritableThreadLocal = new InheritableThreadLocal<>();
        java.lang.ThreadLocal<String> threadLocal = new java.lang.ThreadLocal<>();

        inheritableThreadLocal.set(UUID.randomUUID().toString());
        threadLocal.set(UUID.randomUUID().toString());

        Thread thread = new Thread(() -> {
            System.out.println(String.format("inheritableThreadLocal ==> %s", inheritableThreadLocal.get()));
            System.out.println(String.format("threadLocal ==> %s", threadLocal.get()));
        });

        thread.start();


        System.out.println(String.format("ThreadId ==> %s, threadLocal ==> %s", Thread.currentThread().getId(), threadLocal.get()));

    }
}
