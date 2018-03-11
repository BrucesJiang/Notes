package two;



/**
 * �ؽ�������
 *   ǰ���У������������ͨ�������������е���Ͽ������¹�����
 *   > ǰ��+����
 *   > ����+����
 *   �����ǰ��+���� �����ǽ���֪��������Ϣ�� ���������ڵ��λ�ã������޷�������������ж������������������ڵ��λ��
 *   ����ʵ�ֵ��㷨Ҫ�� �����в��ܳ����ظ�Ԫ�ء���������
 * @author Bruce Jiang
 *
 */
public class ReconstructBinaryTree{
	private Node root; //���ڵ�
	//�ڵ�Ԫ�صĶ���
	private static class Node {
		private int key; // ֵ
		private Node left, right; // �����ӽڵ�

		public Node(int key, Node left, Node right) {
			this.key = key;
			this.left = left;
			this.right = right;
		}
	}

	/**
	 * ���ö�������ǰ��������к�������������ؽ������� ��Ҫע������ڸ÷����б���������û���ظ�Ԫ��
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
	 * ÿһ�εݹ���̣�����ҪΪһ���ڵ�Ѱ��key, ���������ڵ�����������ڵ�
	 * @param preorder
	 * @param preStart
	 * @param preEnd
	 * @param inorder
	 * @param inStart
	 * @param inEnd
	 * @return ���ڵ�
	 */
	private static Node constructCore(int[] preorder, int preStart, int preEnd,
			int[] inorder, int inStart, int inEnd) {
		// �ҵ����ڵ�(ǰ������ĵ�һ��Ԫ�ؾ��Ǹ��ڵ�)
		int rootValue = preorder[preStart]; //��ǰ���ڵ�
		Node node = new Node(rootValue, null, null);
		//��������������и��ݸ��ڵ�λ���ҵ��������������Լ�������������
		
		if(preEnd == preStart) return node; //˵���ýڵ�û����������
		
		//Ѱ�Ҹýڵ����������
		
		//���������������Ѱ�ҵ�ǰ���ڵ������λ��
		int nodeIndex = inStart;
		while(nodeIndex <= inEnd && rootValue != inorder[nodeIndex]) ++nodeIndex;
		
		if(nodeIndex > inEnd) {
			System.out.println("��������ʽ�������");
			//�����׳�����
		}
		
		//������Ԫ�ظ���
		int numLeft = nodeIndex - inStart;
		//������Ԫ�ظ���
		int numRight = inEnd - nodeIndex;
		
		if(numLeft > 0) {
			//����������
			node.left = constructCore(preorder, preStart + 1, preStart + numLeft, inorder, inStart, inStart + numLeft-1);
		}
		
		if(numRight > 0) {
			//����������
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
