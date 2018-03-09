package two.tree;


public abstract class BinaryTree {
	private Node root; //二叉查找树
	
	private class Node{
		private int key; //值
		private Node left, right; //左右子节点
		
		public Node(int key, Node left, Node right) {
			this.key = key;
			this.left = left;
			this.right = right;
		}
	}
	
	public int put(int key){
		return 0;
	}
	
	public void inorderTraverse(){
	}
	
	public static void preorderTraverse(){
		
	}
	
	public static void postorderTraverse(){
		
	}
	
	public static void sequenceTraversal(){
		
	}
}
