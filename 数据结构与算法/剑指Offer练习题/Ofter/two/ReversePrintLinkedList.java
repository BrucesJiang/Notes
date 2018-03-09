package two;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

import util.TestHelper;

/**
 * 题目要求： 输入一个链表的头节点，从尾到头反过来打印每个节点的值
 * @author Bruce Jiang
 *
 */
public class ReversePrintLinkedList {
	
	/**
	 *  Stack
	 * @param ll
	 */
	public static void reversePrintLinkedListWithStack(LinkedList<Integer> ll) {
		if(ll.isEmpty()) return ;
		Iterator<Integer> iter = ll.iterator();
		Stack<Integer> stack  = new Stack<>();
		while(iter.hasNext()) {
			Integer i = iter.next();
			stack.add(i);
			System.out.print(stack.peek() + " ");
		}
		
		System.out.println();
		while(!stack.isEmpty()) {
			System.out.print(stack.peek()+" ");
			stack.pop();
		}
		System.out.println();
	}
	
	/**
	 * 使用递归的方式
	 * @param ll
	 */
	public static void reversePrintLinkedLIstThroughRecursive(LinkedList<Integer> ll) {
		
		if(ll.isEmpty()) return ;
		else {
			Integer item = ll.peekFirst();
			ll.remove();
			reversePrintLinkedLIstThroughRecursive(ll);
			System.out.print(item + " ");
		}
	}
	
	
	public static void main(String[] args){
		LinkedList<Integer> ll = TestHelper.generateLinkedList(15);
		TestHelper.handle(ReversePrintLinkedList.class, "reversePrintLinkedListWithStack", ll);
		TestHelper.handle(ReversePrintLinkedList.class, "reversePrintLinkedLIstThroughRecursive", ll);
	}
}
