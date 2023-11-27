package com.zhou.multithread;

/**
 * 线程的优先级，只会影响线程获取cpu时间片的长短，不会影响获取cpu时间片的先后顺序
 * <p>
 * hystrix、sential
 *
 * @author zhouchao
 * @since 2023/11/27 20:19
 */
public class ThreadPriority {

    public static void main(String[] args) {
        System.out.println(Thread.currentThread().getName() +
                "(" + Thread.currentThread().getPriority() + ")");

        PriorityThread lowPriorityThread = new PriorityThread("lowPriorityThread");
        PriorityThread highPriorityThread = new PriorityThread("highPriorityThread");
        lowPriorityThread.setPriority(1);
        highPriorityThread.setPriority(10);
        lowPriorityThread.start();
        highPriorityThread.start();
    }

    static class PriorityThread extends Thread {

        PriorityThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            for (int i = 0; i < 5; i++) {
                System.out.println(Thread.currentThread().getName() +
                        "(" + Thread.currentThread().getPriority() + ") loop: " + i);
            }
        }
    }

}
