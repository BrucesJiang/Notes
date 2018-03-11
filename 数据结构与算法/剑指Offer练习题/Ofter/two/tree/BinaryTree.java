package two.tree;

import java.util.LinkedList;

public class BinaryTree {
	private Node root; // 二叉查找树

	private class Node {
		private int key; // 值
		private Node left, right; // 左右子节点

		public Node(int key, Node left, Node right) {
			this.key = key;
			this.left = left;
			this.right = right;
		}
	}
	
	public BinaryTree(int key) {
		root = new Node(key, null, null);
	}
	
	public int put(int key) {
		Node node = new Node(key, null, null);
		Node r = root;
		while(true) {
			if(r.key >= key) {
				if(r.left != null) {
					r = r.left;
				}else {
					r.left = node;
					break;
				}
			}else if(r.key < key) {
				if(r.right != null) {
					r = r.right;
				}else {
					r.right = node;
					break;
				}
			}
		}
		return key;
	}

	
	public void inorderTraverse() {
		System.out.println("Start Inoder Traverse");
		inorderTraverse(root);
		System.out.println("End Inoder Traverse");
	}

	public void preorderTraverse() {
		System.out.println("Start Preoder Traverse");
		preorderTraverse(root);
		System.out.println("End Preoder Traverse");
	}

	public void postorderTraverse() {
		System.out.println("Start postoder Traverse");
		postorderTraverse(root);
		System.out.println("End postoder Traverse");
	}

	public void sequenceTraversal() {
		System.out.println("Start Sequence Traverse");
		sequenceTraverse(root);
		System.out.println("End Sequence Traverse");
	}

	private static void inorderTraverse(Node root) {
		if (root == null) {
			return;
		}
		inorderTraverse(root.left);
		System.out.print("Key = " + root.key + " ");
		inorderTraverse(root.right);
	}
	
	private static void preorderTraverse(Node root) {
		if(root == null) {
			return ;
		}
		System.out.print("Key = " + root.key + " ");
		preorderTraverse(root.left);
		preorderTraverse(root.right);
	}
	
	private static void postorderTraverse(Node root){
		if(root == null) return ;
		postorderTraverse(root.left);
		postorderTraverse(root.right);
		System.out.print("Key = " + root.key + " ");
	}
	
	private static void sequenceTraverse(Node root){
		LinkedList<Node> ll = new LinkedList<Node>();
		ll.add(root);
		while(!ll.isEmpty()) {
			Node node = ll.getFirst();
			ll.removeFirst();
			System.out.print("Key = " + node.key + " ");
			if(node.left != null) {
				ll.addLast(node.left);
			}
			if(node.right != null) {
				ll.addLast(node.right);
			}
		}
	}
}
