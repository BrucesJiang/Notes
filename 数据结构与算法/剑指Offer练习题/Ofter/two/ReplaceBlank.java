package two;

import util.TestHelper;

public class ReplaceBlank {
	
	/**
	 * 最基本的算法
	 * O(n^2)算法
	 * @param str
	 * @param rStr
	 * @return
	 */
	public static String replaceBlank01(String str, String rStr) {
		return null;
	}
	
	public static String replaceBlack02(String str, String rStr) {
		if(null == str) return str;
		
		String[] strs = str.split(" ");
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < strs.length - 1; i ++) {
			sb.append(strs[i] + "%20");
		}
		sb.append(strs[strs.length-1]);
		return sb.toString(); 
	}
	
	
	public static void main(String[] args) {
		String str = "I'd like a beautiful girl";
		TestHelper.handleReplaceBlank(ReplaceBlank.class, "replaceBlack02", str, "%20");
	}
	
}
