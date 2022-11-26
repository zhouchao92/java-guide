package com.zhou;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * FilterRepeatNumber
 *
 * @author 周超
 * @since 2022/10/11 19:21
 */
public class FilterRepeatNumber {

    public static void main(String[] args) {
        int[] array = new int[10];

        Set<Integer> set = new HashSet<>(array.length);

        boolean flag = false;
        for (int i : array) {
            if (set.contains(i)) {
                flag = true;
                break;
            }
            set.add(i);
        }
    }

}
