package com.zhou.jvm;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author zhouchao
 * @since 2025/5/19 14:30
 */
public class DynamicProxyCase {

    interface IHello {
        void sayHello();
    }

    public static class Hello implements IHello {
        @Override
        public void sayHello() {
            System.out.println("hello world");
        }
    }

    public static class DynamicProxy implements java.lang.reflect.InvocationHandler {

        private Object original;

        Object bind(Object original) {
            this.original = original;
            return Proxy.newProxyInstance(original.getClass().getClassLoader(), original.getClass()
                    .getInterfaces(), this);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.println("welcome to my world");
            return method.invoke(original, args);
        }
    }

    public static void main(String[] args) {
        System.getProperties().put("sun.misc.ProxyGenerator.saveGeneratedFiles", "true");

        IHello iHello = (IHello) new DynamicProxy().bind(new Hello());

        iHello.sayHello();
    }
}
