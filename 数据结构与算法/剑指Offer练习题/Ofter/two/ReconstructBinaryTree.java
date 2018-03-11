package two;



/**
 * 重建二叉树
 *   前，中，后序遍历其中通过以下两种序列的组合可以重新构建树
 *   > 前序+中序
 *   > 后序+中序
 *   如果是前序+后序 ，我们仅仅知道以下信息： 整棵树根节点的位置，但是无法结合两个序列判断左子树和右子树根节点的位置
 *   这里实现的算法要求： 序列中不能出现重复元素。否则会出错。
 * @author Bruce Jiang
 *
 */
public class ReconstructBinaryTree{
	private Node root; //根节点
	//节点元素的定义
	private static class Node {
		private int key; // 值
		private Node left, right; // 左右子节点

		public Node(int key, Node left, Node right) {
			this.key = key;
			this.left = left;
			this.right = right;
		}
	}

	/**
	 * 利用二叉树的前序遍历序列和中序遍历序列重建二叉树 需要注意的是在该方法中遍历序列中没有重复元素
	 * 
	 * @param preorder
	 * @param inorder
	 */
	public void preorderAndInorder(int[] preorder, int[] inorder) {
		if (preorder == null || inorder == null || preorder.length == 0
				|| inorder.length == 0)
			return;
		
		this.root = constructCore(preorder, 0, preorder.length - 1, inorder, 0,
				inorder.length-1);
	}

	/**
	 * 每一次递归过程，都需要为一个节点寻找key, 左子树根节点和右子树根节点
	 * @param preorder
	 * @param preStart
	 * @param preEnd
	 * @param inorder
	 * @param inStart
	 * @param inEnd
	 * @return 根节点
	 */
	private static Node constructCore(int[] preorder, int preStart, int preEnd,
			int[] inorder, int inStart, int inEnd) {
		// 找到根节点(前序遍历的第一个元素就是根节点)
		int rootValue = preorder[preStart]; //当前根节点
		Node node = new Node(rootValue, null, null);
		//从中序遍历序列中根据根节点位置找到左子树子序列以及右子树子序列
		
		if(preEnd == preStart) return node; //说明该节点没有左右子树
		
		//寻找该节点的左右子树
		
		//在中序遍历序列中寻找当前根节点的索引位置
		int nodeIndex = inStart;
		while(nodeIndex <= inEnd && rootValue != inorder[nodeIndex]) ++nodeIndex;
		
		if(nodeIndex > inEnd) {
			System.out.println("输入序列式有问题的");
			//可以抛出例外
		}
		
		//左子树元素个数
		int numLeft = nodeIndex - inStart;
		//右子树元素个数
		int numRight = inEnd - nodeIndex;
		
		if(numLeft > 0) {
			//构建左子树
			node.left = constructCore(preorder, preStart + 1, preStart + numLeft, inorder, inStart, inStart + numLeft-1);
		}
		
		if(numRight > 0) {
			//构建右子树
			node.right = constructCore(preorder, preEnd - numRight + 1, preEnd, inorder, inEnd-numRight + 1, inEnd);
		}

		return node;
	}
	
	
	public static void main(String[] args) {
		int[] preorder = {1, 2, 4, 7, 3, 5, 6, 8};
		int[] inorder = {4, 7, 2, 1, 5, 3, 8, 6};
		
		ReconstructBinaryTree rbt = new ReconstructBinaryTree();
		rbt.preorderAndInorder(preorder, inorder);
		rbt.postorderTraverse();
	}
	
	
	public void postorderTraverse() {
		System.out.println("Start postoder Traverse");
		postorderTraverse(root);
		System.out.println("End postoder Traverse");
	}
	private static void postorderTraverse(Node root){
		if(root == null) return ;
		postorderTraverse(root.left);
		postorderTraverse(root.right);
		System.out.print("Key = " + root.key + " ");
	}

}
