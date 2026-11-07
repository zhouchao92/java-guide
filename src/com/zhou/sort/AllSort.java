package com.zhou.sort;

import java.util.ArrayList;
import java.util.List;

/**
 * 全排列
 * 对于一个长度为 n 的数组（假设元素互不重复），其排列方案数共有：
 * <p>
 * n×(n−1)×(n−2)…×2×1
 * 排列方案的生成：
 * <p>
 * 根据数组排列的特点，考虑深度优先搜索所有排列方案。即通过元素交换，先固定第 1 位元素（ n 种情况）、再固定第 2 位元素（ n−1 种情况）、... 、最后固定第 n 位元素（ 1 种情况）。
 *
 * @author zhouchao
 * @since 2026/10/26 17:47
 */
public class AllSort {

    private static List<Integer> numsList;
    private static List<List<Integer>> resultList;

    static List<List<Integer>> allSort(int[] nums) {
        numsList = new ArrayList<>();
        for (int num : nums) {
            numsList.add(num);
        }
        resultList = new ArrayList<>();

        dfs(0);

        return resultList;
    }

    private static void dfs(int i) {
        if (i == numsList.size() - 1) {
            resultList.add(new ArrayList<>(numsList));
            return;
        }
        for (int j = i; j < numsList.size(); j++) {
            swap(i, j);
            dfs(i + 1);
            swap(i, j);
        }
    }

    private static void swap(int i, int j) {
        int temp = numsList.get(i);
        numsList.set(i, numsList.get(j));
        numsList.set(j, temp);
    }

    public static void main(String[] args) {
        int[] nums = {1, 2, 3, 4, 5};
        allSort(nums);
        resultList.forEach(System.out::println);
    }

}
