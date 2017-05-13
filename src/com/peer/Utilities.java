package com.peer;
import java.io.*;
import java.util.*;

public class Utilities {
	
	public static Map<Integer,String> getFileNames(File dir){
		HashMap<Integer,String> result = new HashMap<Integer,String>();
		String[] files = dir.list();
		for(String f:files){
			if(f.startsWith("rfc") && f.endsWith(".txt")){
				int rfc_num = Integer.valueOf(f.split("rfc")[1].replace(".txt", ""));
				String rfc_name = getTitle(f,dir);
				result.put(rfc_num,rfc_name);
			}
		}
		return result;
	}
	
	public static String getTitle(String filename, File dir){
		int rfc_num = Integer.valueOf(filename.split("rfc")[1].replace(".txt", ""));
		String rfc_name = "";
		try {
			String line;
            FileReader fileReader = new FileReader(dir.getAbsolutePath()+ "/" + filename);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
            	if(line.contains("RFC "+rfc_num)){
					String arr[] = line.split("\\s+");
					int size = arr.length - 3;
					for(int i=2;i<=size; i++){
						if(i < size)
							rfc_name = rfc_name + arr[i] + " ";
						else 
							rfc_name = rfc_name + arr[i];
					}
					//System.out.println(rfc_name);
					break;
		        }
            }
            bufferedReader.close();
            fileReader.close();
		}catch(Exception e){
            System.out.println(e.getMessage());
		}
		return rfc_name;
	}
}
