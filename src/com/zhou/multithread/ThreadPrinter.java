package com.zhou.multithread;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.TimeUnit;

/**
 * [6] Monitor Ctrl-Break   IDEA通过反射的方式开启用于监听jvm进程的开启和关闭的监听器
 * [5] Attach Listener      附加监听器，jvm进程之间通信的工具（jstack、jmap...）
 * [4] Signal Dispatcher    信号分发器，1.启动方式-XXAttachListener，2.延迟开启(java -version)，jvm适时开启
 * [3] Finalizer            GC线程
 * [2] Reference Handler    引用处理线程
 * [1] main
 *
 * @author zhouchao
 * @since 2023/11/27 19:35
 */
public class ThreadPrinter {

    public static void main(String[] args) throws InterruptedException {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(false, false);
        for (ThreadInfo threadInfo : threadInfos) {
            System.out.println("[" + threadInfo.getThreadId() + "] " + threadInfo.getThreadName());
        }
        TimeUnit.HOURS.sleep(1);
    }

}
