package two.tree;


public abstract class BinaryTree {
	private Node root; //���������
	
	private class Node{
		private int key; //ֵ
		private Node left, right; //�����ӽڵ�
		
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
