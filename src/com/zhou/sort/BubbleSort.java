package com.zhou.sort;

import java.util.Arrays;

/**
 * BubbleSort
 *
 * @author 周超
 * @since 2022/10/12 10:56
 */
public class BubbleSort {
    public static void main(String[] args) {
        int[] arr = new int[]{5, 1, 2, 3, 4, 8};
        bubbleSort(arr);
        System.out.println(Arrays.toString(arr));
    }

    private static void bubbleSort(int[] arr) {
        int temp;
        for (int i = 0; i < arr.length - 1; i++) {
            for (int j = 0; j < arr.length - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
    }
}
