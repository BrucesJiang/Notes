package two;

import java.util.Random;

import util.TestHelper;

public class FindInArray {
	static int M = 10;
	static int N = 10;
	static int T;
	static Random random = new Random();
	/**
	 * 从二维数组中查找元素
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
	   * 算法思路详解：
	   * 对于输入的数组，横轴上从左到右为递增序列
	   *                纵轴上从上到下为递增序列
	   * 如果从二维数组的左上角或右下角开始搜索会面临两个问题：T 大于当前位置的值时，向哪个方向走的问题，同样也会有搜索的重叠区
	   * 因此，我们从左下角或右上角开始搜索，具体代码如下
	   */
		//初始化索引数据, 从右上角搜索
		int M = m.length;
		int x = 0, y = m[0].length - 1;
		
		while(x < M && y >= 0) { //边界条件
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
