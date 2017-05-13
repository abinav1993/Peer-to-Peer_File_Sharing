package com.peer;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.net.*;
import com.util.*;

public class RequestHandler implements Runnable{
	private Socket socket;
	private String hostname;
	private String dir;
	
	public RequestHandler(Socket socket, String hostname, String dir){
		this.socket = socket;
		this.hostname = hostname;
		this.dir = dir;
	}
	
	public void run(){
//		System.out.println("Inside Run Method");
		try{
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			String filename;
			byte data[], header[];
			int dataLength = dis.readInt();
			String request = "";
			while(dataLength != 0){
				String temp = dis.readUTF();
				request += temp;
				dataLength -= temp.length();
			}
			String values[] = Parser.parse(request).split(Pattern.quote("$"));
			if(values[0].equals("GET")){
				filename = "rfc"+values[1]+".txt";
				File f = new File(filename);
				FileInputStream fis = new FileInputStream(dir+"/"+filename);
				data = new byte[fis.available()];
				fis.read(data);
				String response = Parser.createGetResponse(200, "OK", new Date(), new Date(f.lastModified()), data.length, "text/plain");
				header = response.getBytes();
				dos.writeInt(header.length);
				dos.write(header);
				dos.writeInt(data.length);
				dos.write(data);
			}
		}catch(Exception e){
			//System.out.println(e);
		}
	}
}