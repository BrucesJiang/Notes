package util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Random;

public final class TestHelper {
	static Random random = new Random();
	
	/**
	 * generate an array from Top to Down and from Left to Right
	 * @param M
	 * @param N
	 * @return
	 */
	public static int[][] generateMatrixInOrder(int M, int N) {
		int[][] matrix = new int[M][N];
		
		matrix[0][0] = random.nextInt() % (M*N);
		
		for(int y = 1; y < N; y ++) {
			int r = random.nextInt() % (M*N);
			if(r < matrix[0][y-1]) {
				matrix[0][y] = matrix[0][y-1] + 1; 
			}else {
				matrix[0][y] = r;
			}
		}
		
		for(int x = 1; x < M; x ++) {
			int r = random.nextInt() % (M*N);
			if(r < matrix[x-1][0]) {
				matrix[x][0] = matrix[x-1][0] + 1; 
			}else {
				matrix[x][0] = r;
			}
		}
		
		for(int x = 1; x < M; x ++) {
			for(int y = 1; y < N; y ++) {
				int r = random.nextInt() % (M*N);
				if (r < matrix[x][y-1] || r < matrix[x-1][y]) {
					matrix[x][y] = Math.max(matrix[x-1][y], matrix[x][y-1]) + 1;
				}else {
					matrix[x][y] = r;
				}
			}
		}
		
		return matrix;
	}
	
	
	
	public static void handleFindInArray(Class<?> clazz, String methodName, int[][] matrix, int t) {
			
		
		try {
			Method m = clazz.getMethod(methodName, int[][].class, int.class);
			Object c = m.invoke(clazz, matrix, t);
			System.out.println("Result is = " + c + ", T = " + t);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	
	/***********************字符替代******************************/
	public static void handleReplaceBlank(Class<?> clazz, String methodName, String str, String rStr) {
		try {
			Method method = clazz.getMethod(methodName, new Class<?>[]{String.class, String.class});
			Object s = method.invoke(clazz, str, rStr);
			System.out.println("R = "+str + "\nD = " + s);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	
	/*************************链表反序***********************************/
	public static LinkedList<Integer> generateLinkedList(int N) {
		LinkedList<Integer> ll = new LinkedList<>();
		for(int i = 0; i < N; i ++) {
			ll.add(random.nextInt(N));
		}
		return ll;
	}
	
	
	/**
	 * 在这里变长参数会有问题，如果想要设计一个通用的handle该怎么处理
	 * @param clazz
	 * @param methodName
	 * @param ll
	 */
	public static void handle(Class<?> clazz, String methodName, LinkedList<Integer> ll) {
		
		try {
			Method method = clazz.getDeclaredMethod(methodName, LinkedList.class);
			method.invoke(clazz, ll);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	
}
