package com.zhou.sort;

/**
 * @author zhouchao
 * @since 2026/10/7 17:16
 */
public class ReserveLinkedList {

    /**
     * 递归法反转链表
     *
     * @param head 头节点
     * @return 反转后的头节点
     */
    public static ListNode reserveLinkedList1(ListNode head) {
        if (head == null || head.next == null) {
            return head;
        }
        ListNode last = reserveLinkedList1(head.next);
        head.next.next = head;
        head.next = null;
        return last;
    }

    /**
     * 迭代法
     *
     * @param head 头节点
     * @return 反转后的头节点
     */
    public static ListNode reserveLinkedList2(ListNode head) {
        if (head == null || head.next == null) {
            return head;
        }
        ListNode prev = null;
        ListNode curr = head;
        while (curr != null) {
            ListNode next = curr.next;
            curr.next = prev;
            prev = curr;
            curr = next;
        }
        return prev;
    }


    public static void main(String[] args) {
        ListNode head = new ListNode(1);
        head.next = new ListNode(2);
        head.next.next = new ListNode(3);
        head.next.next.next = new ListNode(4);
        head.next.next.next.next = new ListNode(5);
        // ListNode listNode = reserveLinkedList1(head);
        ListNode listNode = reserveLinkedList2(head);
        while (listNode != null) {
            System.out.print(listNode.val + " ");
            listNode = listNode.next;
        }
    }

    /**
     * @author zhouchao
     * @since 2026/10/7 17:17
     */
    public static class ListNode {
        int val;
        ListNode next;

        ListNode(int x) {
            val = x;
        }
    }
}
