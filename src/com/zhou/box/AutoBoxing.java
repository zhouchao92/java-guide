package com.zhou.box;

/**
 * 缓存池，-128~127
 * <p>
 * 最大值可用系统参数调整，最小为127
 * String integerCacheHighPropValue =
 * VM.getSavedProperty("java.lang.Integer.IntegerCache.high");
 *
 * @author zhouchao
 * @since 2026/10/27 11:24
 */
public class AutoBoxing {

    public static void main(String[] args) {
        Integer a = 127;
        Integer b = 127;
        System.out.println(a == b); // 自动装箱，调用 Integer.valueOf(int)

        Integer c = new Integer(127);
        System.out.println(a == c);

        Integer d = Integer.valueOf(127);
        System.out.println(a == d);

        Integer e = Integer.parseInt("127"); // 先返回 int 基本数据类型，再自动装箱
        System.out.println(a == e);
    }
}
