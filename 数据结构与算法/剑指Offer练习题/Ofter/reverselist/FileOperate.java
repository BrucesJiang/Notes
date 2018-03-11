package reverselist;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


public class FileOperate {
	
	public String readFile(String pathName) {
		StringBuilder sb = new StringBuilder();
		try {
			String str = null;
			BufferedReader br = new BufferedReader(new FileReader(pathName));
			while((str = br.readLine()) != null){
				sb.append(str + " ");
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	public void outFile(String pathName, String result){
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(pathName));
			bw.write(result);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		String str = "Symbol table ok haode";
		String[] strs = str.split(" ");
		List<String> list = Arrays.asList(strs);
		ReverseList<String> rl = new ReverseList<String>(list);
		Iterator<String> iter = rl.revered().iterator();
		StringBuilder sb = new StringBuilder();
		while(iter.hasNext()) {
			sb.append(iter.next());
		}
		System.out.println(sb.toString());
	}
}
