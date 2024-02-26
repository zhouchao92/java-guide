package com.zhou.sort;

import java.util.Arrays;

/**
 * QuickSort
 *
 * @author 周超
 * @since 2022/10/12 10:47
 */
public class QuickSort {

    public static void main(String[] args) {
        int[] arr = new int[]{5, 1, 2, 3, 4, 8};
        quickSort(arr);
        System.out.println(Arrays.toString(arr));
    }

    private static void quickSort(int[] arr) {
        int start = 0;
        quickSort(arr, start, arr.length - 1);
    }

    private static void quickSort(int[] arr, int start, int end) {
        if (start >= end)
            return;
        int left = start;
        int right = end;
        int base = arr[start];
        while (left < right) {
            while (right > left) {
                if (arr[right] < base) {
                    arr[left] = arr[right];
                    left++;
                    break;
                }
                right--;
            }
            while (left < right) {
                if (arr[left] > base) {
                    arr[right] = arr[left];
                    right--;
                    break;
                }
                left++;
            }
        }
        arr[left] = base;
        quickSort(arr, start, left - 1);
        quickSort(arr, right + 1, end);
    }
}
