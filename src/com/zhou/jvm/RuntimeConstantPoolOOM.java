package com.zhou.jvm;

/**
 * RuntimeConstantPoolOOM
 * intern()方法实现就不需要再拷贝字符串的实例到永久代了，
 * 既然字符串常量池已经移到Java堆中，那只需要在常量池里记录一下首次出现的实例引用即可，
 * 因此intern()返回的引用和由StringBuilder创建的那个字符串实例就是同一个。
 * <p>
 * 字符串"java"是在加载sun.misc.Version这个类的时候进入常量池的
 * {@link sun.misc.Version}
 *
 * @author 周超
 * @since 2022/11/22 11:41
 */
public class RuntimeConstantPoolOOM {
    public static void main(String[] args) {
        String str1 = new StringBuilder("计算机").append("软件").toString();
        System.out.println(str1.intern() == str1); // true

        String str2 = new StringBuilder("ja").append("va").toString();
        System.out.println(str2.intern() == str2); // false
    }
}

