package two.tree;

public class BinaryTreeTest {

	public static void main(String[] args) {
		int[] tree = { 10, 9, 11, 7, 12, 17, 5, 8 };
		BinaryTree bt = new BinaryTree(tree[0]);
		for (int i = 1; i < tree.length; i++) {
			bt.put(tree[i]);
		}

		bt.preorderTraverse();
		bt.postorderTraverse();
		bt.inorderTraverse();
		bt.sequenceTraversal();
	}

}
