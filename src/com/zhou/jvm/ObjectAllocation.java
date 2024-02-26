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
        // testPreTenureThreshold();

        // 4.动态对象年龄判定
        // testDynamicAgeJudge();

        // 5.空间分配担保
        testHandlePromotion();
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

    /**
     * 执行参数：-verbose:gc -Xms20M -Xmx20M -Xmn10M -XX:+PrintGCDetails -XX:SurvivorRatio=8 -XX:+UseSerialGC -XX:+PrintTenuringDistribution -XX:MaxTenuringThreshold=15
     * <p>
     * <p>
     * * [GC (Allocation Failure) [DefNew
     * * Desired survivor size 524288 bytes, new threshold 1 (max 15)
     * * - age   1:    1048576 bytes,    1048576 total
     * * : 6631K->1024K(9216K), 0.0026655 secs] 6631K->5245K(19456K), 0.0026983 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
     * * [GC (Allocation Failure) [DefNew
     * * Desired survivor size 524288 bytes, new threshold 15 (max 15)
     * * : 5120K->0K(9216K), 0.0007276 secs] 9341K->5245K(19456K), 0.0007413 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
     * * Heap
     * *  def new generation   total 9216K, used 4333K [0x00000000fec00000, 0x00000000ff600000, 0x00000000ff600000)
     * *   eden space 8192K,  52% used [0x00000000fec00000, 0x00000000ff03b6d0, 0x00000000ff400000)
     * *   from space 1024K,   0% used [0x00000000ff400000, 0x00000000ff400000, 0x00000000ff500000)
     * *   to   space 1024K,   0% used [0x00000000ff500000, 0x00000000ff500000, 0x00000000ff600000)
     * *  tenured generation   total 10240K, used 5245K [0x00000000ff600000, 0x0000000100000000, 0x0000000100000000)
     * *    the space 10240K,  51% used [0x00000000ff600000, 0x00000000ffb1f4c8, 0x00000000ffb1f600, 0x0000000100000000)
     * *  Metaspace       used 3327K, capacity 4496K, committed 4864K, reserved 1056768K
     * *   class space    used 363K, capacity 388K, committed 512K, reserved 1048576K
     * <p>
     * 注释 allocation1 = new byte[_1MB / 4]; 或 allocation2 = new byte[_1MB / 4];
     * * [GC (Allocation Failure) [DefNew
     * * Desired survivor size 524288 bytes, new threshold 1 (max 15)
     * * - age   1:     914624 bytes,     914624 total
     * * : 6375K->893K(9216K), 0.0024829 secs] 6375K->4989K(19456K), 0.0025164 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
     * * [GC (Allocation Failure) [DefNew
     * * Desired survivor size 524288 bytes, new threshold 15 (max 15)
     * * : 4989K->0K(9216K), 0.0006593 secs] 9085K->4985K(19456K), 0.0006740 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
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
    private static void testDynamicAgeJudge() {
        byte[] allocation1, allocation2, allocation3, allocation4;
        allocation1 = new byte[_1MB / 4]; // allocation1 + allocation2 > survivor 区的一半
        allocation2 = new byte[_1MB / 4];
        allocation3 = new byte[4 * _1MB];
        allocation4 = new byte[4 * _1MB];
        allocation4 = null; // minor gc
        allocation4 = new byte[4 * _1MB];
    }

    /**
     * 在发生Minor GC之前，虚拟机必须先检查老年代最大可用的连续空间是否大于新生代所有对象总空间，
     * 如果这个条件成立，那这一次Minor GC可以确保是安全的。
     * 如果不成立，则虚拟机会先查看-XX：HandlePromotionFailure参数的设置值是否允许担保失败（Handle Promotion Failure）；
     * 如果允许，那会继续检查老年代最大可用的连续空间是否大于历次晋升到老年代对象的平均大小，
     * 如果大于，将尝试进行一次Minor GC，尽管这次Minor GC是有风险的；
     * 如果小于，或者-XX：HandlePromotionFailure设置不允许冒险，那这时就要改为进行一次Full GC。
     * <p>
     * 在JDK 6 Update 24之后，-XX：HandlePromotionFailure参数不会再影响到虚拟机的空间分配担保策略
     * 执行参数：-verbose:gc -Xms20M -Xmx20M -Xmn10M -XX:+PrintGCDetails -XX:SurvivorRatio=8 -XX:+UseSerialGC
     * * [GC (Allocation Failure) [DefNew: 8167K->637K(9216K), 0.0021066 secs] 8167K->4733K(19456K), 0.0021396 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
     * * [GC (Allocation Failure) [DefNew: 6938K->0K(9216K), 0.0005343 secs] 11034K->4729K(19456K), 0.0005469 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
     * * Heap
     * *  def new generation   total 9216K, used 2270K [0x00000000fec00000, 0x00000000ff600000, 0x00000000ff600000)
     * *   eden space 8192K,  27% used [0x00000000fec00000, 0x00000000fee37860, 0x00000000ff400000)
     * *   from space 1024K,   0% used [0x00000000ff400000, 0x00000000ff400000, 0x00000000ff500000)
     * *   to   space 1024K,   0% used [0x00000000ff500000, 0x00000000ff500000, 0x00000000ff600000)
     * *  tenured generation   total 10240K, used 4729K [0x00000000ff600000, 0x0000000100000000, 0x0000000100000000)
     * *    the space 10240K,  46% used [0x00000000ff600000, 0x00000000ffa9e650, 0x00000000ffa9e800, 0x0000000100000000)
     * *  Metaspace       used 3328K, capacity 4500K, committed 4864K, reserved 1056768K
     * *   class space    used 363K, capacity 388K, committed 512K, reserved 1048576K
     */
    private static void testHandlePromotion() {
        byte[] allocation1, allocation2, allocation3, allocation4, allocation5, allocation6, allocation7;
        allocation1 = new byte[2 * _1MB];
        allocation2 = new byte[2 * _1MB];
        allocation3 = new byte[2 * _1MB];
        allocation1 = null;
        allocation4 = new byte[2 * _1MB];
        allocation5 = new byte[2 * _1MB];
        allocation6 = new byte[2 * _1MB];
        allocation4 = null;
        allocation5 = null;
        allocation6 = null;
        allocation7 = new byte[2 * _1MB];
    }

}
