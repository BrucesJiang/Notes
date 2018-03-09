package two;

import java.util.Random;

import util.TestHelper;

public class FindInArray {
	static int M = 10;
	static int N = 10;
	static int T;
	static Random random = new Random();
	/**
	 * �Ӷ�ά�����в���Ԫ��
	 * @param m
	 * @param t
	 */
	public static boolean findInArray(int[][] m, int t) {
//		for(int i = 0; i < m.length; i ++) {
//			for(int j = 0; j < m[i].length; j ++) {
//				if(m[i][j] == t) return true;
//			}
//		}
//		return false;
	  if(null == m || m.length == 0) return false;
	  /**
	   * �㷨˼·��⣺
	   * ������������飬�����ϴ�����Ϊ��������
	   *                �����ϴ��ϵ���Ϊ��������
	   * ����Ӷ�ά��������Ͻǻ����½ǿ�ʼ�����������������⣺T ���ڵ�ǰλ�õ�ֵʱ�����ĸ������ߵ����⣬ͬ��Ҳ�����������ص���
	   * ��ˣ����Ǵ����½ǻ����Ͻǿ�ʼ�����������������
	   */
		//��ʼ����������, �����Ͻ�����
		int M = m.length;
		int x = 0, y = m[0].length - 1;
		
		while(x < M && y >= 0) { //�߽�����
			if(m[x][y] == t) return true;
			else if(m[x][y] < t) x++;
			else y--; //m[x][y] > t
		}
		return false;
	}
	
	
	
	
	public static void main(String[] args){
		
		int[][] arr = TestHelper.generateMatrixInOrder(M, N);
		
		T = random.nextInt() %(M*N);
		
		TestHelper.handleFindInArray(FindInArray.class, "findInArray", arr, T);
		
		T = arr[M/2][N/2];
		TestHelper.handleFindInArray(FindInArray.class, "findInArray", arr, T);
	}

}
