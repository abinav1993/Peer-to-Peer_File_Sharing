package com.util;

import java.util.*;
import com.util.ResultSet;

public class Parser {

	public static String createAddRequest(int rfc_num, String hostname, int port, String title){
		String request = "";
		request += "ADD RFC "+rfc_num+" P2P-CI/1.0\r\n";
		request += "Host: "+ hostname + "\r\n";
		request += "Port: " + port + "\r\n";
		request += "Title: "+title+ "\r\n";
		request += "\r\n";
		return request;
	}
	
	public static String createDeleteRequest(String hostname, int port){
		String request = "";
		request += "DELETE P2P-CI/1.0\r\n";
		request += "Host: "+ hostname + "\r\n";
		request += "Port: " + port + "\r\n";
		request += "\r\n";
		return request;
	}
	
	public static String createAddResponse(int status_code, String phrase, int rfc_num, String title, String hostname, int port){
		String response = "";
		response += "P2P-CI/1.0 "+ status_code + " " + phrase +"\r\n";
		response += "\r\n";
		response += rfc_num +" " + title + " " + hostname + " " + port + "\r\n";
		response += "\r\n";
		return response;
	}

	public static String parse(String request){
		String temp[] = request.split("\r\n");
		String line[] = temp[0].split(" ");
		String requestType = line[0];
		int rfc_num = 0, port = 0;
		String hostname = null, title = null, OS = null;
		
		try{
			if(requestType.equals("ADD") || requestType.equals("LOOKUP") || requestType.equals("GET")){
				rfc_num = Integer.parseInt(line[2]);
			}
//			System.out.println(request.trim().split("\n")[0].split(" ")[0]);
			for(String s:temp){
				if(s.indexOf("Host") != -1)
					hostname = s.substring(s.indexOf(":")+1,s.length()).trim();
				else if(s.indexOf("Port") != -1)
					port = Integer.parseInt(s.substring(s.indexOf(":")+1,s.length()).trim());
				else if(s.indexOf("Title") != -1)
					title = s.substring(s.indexOf(":")+1,s.length()).trim();
				else if(s.indexOf("OS") != -1)
					OS = s.substring(s.indexOf(":")+1,s.length()).trim();
			}
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
		
		String result = requestType + "$" + rfc_num +"$" + port + "$" + hostname + "$" + title + "$" + OS;
//		System.out.println(result.split(Pattern.quote("$"))[0]);
		return result;
	}
	
	public static String createServerResponse(int status_code, String phrase){
		String response = "";
		response += "P2P-CI/1.0 "+ status_code + " " + phrase +"\r\n";
		response += "\r\n";
		return response;
	}
	
	public static void main(String args[]){
		
//		parse(createLookUpRequest(1209,"google.com",1234,"MACWORK"));
	}
	
	public static String createLookUpRequest(int rfc_num, String hostname, int port){
		String request = "";
		request += "LOOKUP RFC "+rfc_num+ " P2P-CI/1.0\r\n";
		request += "Host: "+ hostname + "\r\n";
		request += "Port: " + port +"\r\n";
//		request += "Title: "+title+"\r\n";
		request += "\r\n";
		return request;
	}
	
	public static String createListRequest(String hostname, int port){
		String request = "";
		request += "LIST ALL P2P-CI/1.0\r\n";
		request += "Host: "+ hostname + "\r\n";
		request += "Port: " + port +"\r\n";
		request += "\r\n";
		return request;
	}
	
	public static String createGetRequest(int rfc_num, String hostname){
		String request = "";
		request += "GET RFC "+rfc_num +" P2P-CI/1.0\r\n";
		request += "Host: "+ hostname + "\r\n";
		request += "OS: "+System.getProperty("os.name") + "\r\n";
		request += "\r\n";
		return request;
	}
	
	public static String createListResponse(int status_code, String phrase, ArrayList<ResultSet> result){
		String response = "";
		response += "P2P-CI/1.0 "+ status_code + " " + phrase +"\r\n";
		response += "\r\n";
		if(result != null){
			for(ResultSet res : result){
				response += res.getRFC().getRFCnum() +" " + res.getRFC().getRFCname() + " " + res.getRFC().getHostname() + " " + res.getUploadPort() + "\r\n";
			}
		}else{
			return null;
		}
		response += "\r\n";
		return response;
	}
	
	public static String createGetResponse(int status_code, String phrase, Date date, Date last_modified, int content_length, String content_type){
		String response = "";
		response += "P2P-CI/1.0 "+ status_code + " " + phrase + "\r\n";
		response += "Date: " + date.toString() + "\r\n";
		response += "OS: " + System.getProperty("os.name") + "\r\n";
		response += "Last-Modified: "+ last_modified.toString() + "\r\n";
		response += "Content-Length: " + content_length + "\r\n";
		response += "Content-Type: " + content_type + "\r\n";
		response += "\r\n";
		return response;
	}
}
