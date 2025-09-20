package com.zhou.clazz;

/**
 * 1️⃣	父类静态变量 & 静态代码块（按书写顺序）	类加载时	                仅一次
 * 2️⃣	子类静态变量 & 静态代码块（按书写顺序）	类加载时（子类首次使用）    仅一次
 * 3️⃣	父类实例变量 & 实例代码块（按书写顺序）	每次 new 时	            每次 new 都执行
 * 4️⃣	父类构造函数	                        super() 调用时		    每次 new 都执行
 * 5️⃣	子类实例变量 & 实例代码块（按书写顺序）	构造函数中 super() 后	    每次 new 都执行
 * 6️⃣	子类构造函数	                        构造函数体执行	            每次 new 都执行
 *
 * @author zhouchao
 * @since 2025/8/4 14:42
 */
public class ClassCase {

    public static void main(String[] args) {
        // how to print the load order
        System.out.println("i was loaded, main...");
        new ClassLoadOrder();
    }

}
