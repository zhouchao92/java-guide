package com.zhou.clazz;

/**
 * 类中不同元素的加载顺序？代码块，静态代码块，静态成员变量，普通成员变量，构造函数？
 *
 * @author zhouchao
 * @since 2025/8/4 14:36
 */
public class ClassLoadOrder {

    private int a = 1;

    private static int b = 2;

    {
        System.out.println("i was loaded, code block...");
    }

    static {
        System.out.println("i was loaded, static code block...");
    }

    public ClassLoadOrder() {
        System.out.println("i was loaded, constructor...");
    }

}
