package com.zhou.jvm;

/**
 * @author zhouchao
 * @since 2024/2/27 15:09
 */
public class JHSDBTestCase {
    static class Test {
        static ObjectHolder staticObj = new ObjectHolder();
        ObjectHolder instanceObj = new ObjectHolder();

        void foo() {
            ObjectHolder localObj = new ObjectHolder();
            System.out.println("done"); // 这里设一个断点
        }
    }

    private static class ObjectHolder {
    }

    /***
     * staticObj、instanceObj、localObj 这三个变量本身（而不是它们所指向的对象）存放在哪里？
     * <p>
     * staticObj随着Test的类型信息存放在方法区，
     * instanceObj随着Test的对象实例存放在Java堆，
     * localObject则是存放在foo()方法栈帧的局部变量表中。
     * 运行参数 -Xmx10m -XX:+UseSerialGC -XX:-UseCompressedOops
     */
    public static void main(String[] args) {
        Test test = new JHSDBTestCase.Test();
        test.foo();
    }
}