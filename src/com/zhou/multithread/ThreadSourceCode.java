package com.zhou.multithread;

import java.security.AccessControlContext;

/**
 * 线程有哪些状态 {@link java.lang.Thread.State}
 * <p>
 * 线程在初始化时，优先使用传入的 ThreadGroup，次选系统的SecurityManager的 ThreadGroup，最后取父线程的 ThreadGroup
 * <p>
 * 如何保证线程的 tid 唯一性（synchronized 加锁）：{@link java.lang.Thread#nextThreadID()}
 * <p>
 * 线程启动 {@link java.lang.Thread#start()}
 *
 * @author zhouchao
 * @see java.lang.Thread#init(ThreadGroup, Runnable, String, long, AccessControlContext, boolean)
 * @since 2023/12/1 15:42
 */
public class ThreadSourceCode {

    public static void main(String[] args) {
        Thread thread = new Thread(() -> {
        });
        System.out.println(thread.getState());
        thread.start();
    }

}
