package com.zhou;

import java.util.ArrayList;
import java.util.List;

/**
 * JvmTurning
 *
 * @author 周超
 * @since 2022/9/22 12:10
 */
public class JvmTurning {

    public static void main(String[] args) {
        List<Object> list = new ArrayList<>();
        while (true) {
            list.add(new Object());
        }
    }

}
