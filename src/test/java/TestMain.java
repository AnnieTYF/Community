import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import jdk.nashorn.internal.runtime.regexp.joni.Regex;
import org.junit.Test;

import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Pattern;

public class TestMain {

    static  class ListNode {
        int val;
        ListNode next = null;

        ListNode(int val) {
            this.val = val;
        }
    }


    //删除单链表中的重复节点
    public static ListNode deleteDuplication(ListNode pHead) {
        if (pHead == null)
            return pHead;
        ListNode first = new ListNode(0);
        first.next = pHead;
        ListNode p = pHead;
        ListNode pre = first;
        while (p != null && p.next != null && p.next.next != null) {
            if (p.val == p.next.val && p.next.val == p.next.next.val) {
                int val = p.val;

                //只需把下面的while改为注释的while，即可由情形1得到2
                while ( p.next.val == val ) {
                    p.next = p.next.next;
                }

                pre.next = p;
            } else {
                pre = p;
                p = p.next;
            }
        }
        while (first.next != null) {
            System.out.print(first.next.val + " ");
            first = first.next;
        }
        return first.next;
    }

    public static void main(String[] args) {
        //-构造单链表start----
        Scanner x=new Scanner(System.in);
        int m= x.nextInt();
        int[] num= new int[m];
        for(int i=0;i<m;i++){
                num[i]=x.nextInt();
            System.out.println(num[i]);
        }

        ListNode head = new ListNode(num[0]);
        ListNode pre = head;
        for (int i = 1; i < num.length; i++) {
            ListNode node = new ListNode(num[i]);
            pre.next = node;
            pre = node;
        }
        //-构造单链表end-----
        deleteDuplication(head);
    }
}



