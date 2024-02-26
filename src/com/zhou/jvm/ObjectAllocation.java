package com.zhou.jvm;

/**
 * @author zhouchao
 * @since 2024/2/26 9:41
 */
public class ObjectAllocation {

    private static final int _1MB = 1024 * 1024;

    public static void main(String[] args) {
        // 1.对象优先分配在Eden区
        // testAllocateToEdenFirstly();

        // 2.大对象直接进入老年代
        // testPreTenureSizeThreshold();

        // 3.长期存活的对象将进入老年代
        testPreTenureThreshold();
    }

    /**
     * 执行参数：-verbose:gc -Xms20M -Xmx20M -Xmn10M -XX:+PrintGCDetails -XX:SurvivorRatio=8
     * <p>
     * * Heap
     * *  PSYoungGen      total 9216K, used 8192K [0x00000000ff600000, 0x0000000100000000, 0x0000000100000000)
     * *   eden space 8192K, 100% used [0x00000000ff600000,0x00000000ffe00000,0x00000000ffe00000)
     * *   from space 1024K, 0% used [0x00000000fff00000,0x00000000fff00000,0x0000000100000000)
     * *   to   space 1024K, 0% used [0x00000000ffe00000,0x00000000ffe00000,0x00000000fff00000)
     * *  ParOldGen       total 10240K, used 4096K [0x00000000fec00000, 0x00000000ff600000, 0x00000000ff600000)
     * *   object space 10240K, 40% used [0x00000000fec00000,0x00000000ff000010,0x00000000ff600000)
     * *  Metaspace       used 3326K, capacity 4496K, committed 4864K, reserved 1056768K
     * *   class space    used 363K, capacity 388K, committed 512K, reserved 1048576K
     * </p>
     * <p>
     * 指定垃圾回收器： -XX:+UseSerialGC
     * * [GC (Allocation Failure) [DefNew: 8167K->637K(9216K), 0.0031418 secs] 8167K->6781K(19456K), 0.0031867 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
     * * Heap
     * *  def new generation   total 9216K, used 4972K [0x00000000fec00000, 0x00000000ff600000, 0x00000000ff600000)
     * *   eden space 8192K,  52% used [0x00000000fec00000, 0x00000000ff03bd30, 0x00000000ff400000)
     * *   from space 1024K,  62% used [0x00000000ff500000, 0x00000000ff59f4b0, 0x00000000ff600000)
     * *   to   space 1024K,   0% used [0x00000000ff400000, 0x00000000ff400000, 0x00000000ff500000)
     * *  tenured generation   total 10240K, used 6144K [0x00000000ff600000, 0x0000000100000000, 0x0000000100000000)
     * *    the space 10240K,  60% used [0x00000000ff600000, 0x00000000ffc00030, 0x00000000ffc00200, 0x0000000100000000)
     * *  Metaspace       used 3327K, capacity 4496K, committed 4864K, reserved 1056768K
     * *   class space    used 363K, capacity 388K, committed 512K, reserved 1048576K
     */
    private static void testAllocateToEdenFirstly() {
        byte[] allocation1, allocation2, allocation3, allocation4;
        allocation1 = new byte[2 * _1MB];
        allocation2 = new byte[2 * _1MB];
        allocation3 = new byte[2 * _1MB];
        allocation4 = new byte[4 * _1MB]; // minor gc
    }

    /**
     * 执行参数：-verbose:gc -Xms20M -Xmx20M -Xmn10M -XX:+PrintGCDetails -XX:SurvivorRatio=8 -XX:+UseSerialGC -XX:PretenureSizeThreshold=3145728
     */
    private static void testPreTenureSizeThreshold() {
        byte[] allocation = new byte[4 * _1MB];
    }

    /**
     * 执行参数：-verbose:gc -Xms20M -Xmx20M -Xmn10M -XX:+PrintGCDetails -XX:SurvivorRatio=8 -XX:+UseSerialGC -XX:+PrintTenuringDistribution -XX:MaxTenuringThreshold=1
     * <p>
     * -XX:MaxTenuringThreshold=1
     * <p>
     * * [GC (Allocation Failure) [DefNew
     * * Desired survivor size 524288 bytes, new threshold 1 (max 1)
     * * - age   1:     914624 bytes,     914624 total
     * * : 6375K->893K(9216K), 0.0023969 secs] 6375K->4989K(19456K), 0.0024380 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
     * * [GC (Allocation Failure) [DefNew
     * * Desired survivor size 524288 bytes, new threshold 1 (max 1)
     * * : 4989K->0K(9216K), 0.0006254 secs] 9085K->4985K(19456K), 0.0006396 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
     * * Heap
     * *  def new generation   total 9216K, used 4333K [0x00000000fec00000, 0x00000000ff600000, 0x00000000ff600000)
     * *   eden space 8192K,  52% used [0x00000000fec00000, 0x00000000ff03b578, 0x00000000ff400000)
     * *   from space 1024K,   0% used [0x00000000ff400000, 0x00000000ff400000, 0x00000000ff500000)
     * *   to   space 1024K,   0% used [0x00000000ff500000, 0x00000000ff500000, 0x00000000ff600000)
     * *  tenured generation   total 10240K, used 4985K [0x00000000ff600000, 0x0000000100000000, 0x0000000100000000)
     * *    the space 10240K,  48% used [0x00000000ff600000, 0x00000000ffade650, 0x00000000ffade800, 0x0000000100000000)
     * *  Metaspace       used 3327K, capacity 4496K, committed 4864K, reserved 1056768K
     * *   class space    used 363K, capacity 388K, committed 512K, reserved 1048576K
     * <p>
     * -XX:MaxTenuringThreshold=15
     * <p>
     * * [GC (Allocation Failure) [DefNew
     * * Desired survivor size 524288 bytes, new threshold 1 (max 15)
     * * - age   1:     914624 bytes,     914624 total
     * * : 6375K->893K(9216K), 0.0025215 secs] 6375K->4989K(19456K), 0.0025548 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
     * * [GC (Allocation Failure) [DefNew
     * * Desired survivor size 524288 bytes, new threshold 15 (max 15)
     * * : 4989K->0K(9216K), 0.0006698 secs] 9085K->4985K(19456K), 0.0006855 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
     * * Heap
     * *  def new generation   total 9216K, used 4333K [0x00000000fec00000, 0x00000000ff600000, 0x00000000ff600000)
     * *   eden space 8192K,  52% used [0x00000000fec00000, 0x00000000ff03b578, 0x00000000ff400000)
     * *   from space 1024K,   0% used [0x00000000ff400000, 0x00000000ff400000, 0x00000000ff500000)
     * *   to   space 1024K,   0% used [0x00000000ff500000, 0x00000000ff500000, 0x00000000ff600000)
     * *  tenured generation   total 10240K, used 4985K [0x00000000ff600000, 0x0000000100000000, 0x0000000100000000)
     * *    the space 10240K,  48% used [0x00000000ff600000, 0x00000000ffade650, 0x00000000ffade800, 0x0000000100000000)
     * *  Metaspace       used 3327K, capacity 4496K, committed 4864K, reserved 1056768K
     * *   class space    used 363K, capacity 388K, committed 512K, reserved 1048576K
     */
    private static void testPreTenureThreshold() {
        byte[] allocation1, allocation2, allocation3, allocation4;
        allocation1 = new byte[_1MB / 4]; // 进入老年代的时间受参数MaxTenuringThreshold影响
        allocation2 = new byte[4 * _1MB];
        allocation3 = new byte[4 * _1MB];
        allocation3 = null;
        allocation4 = new byte[4 * _1MB];
    }

}
