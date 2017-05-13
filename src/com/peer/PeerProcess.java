package com.peer;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.util.*;

import java.net.*;

public class PeerProcess {
	private Map<Integer, String> fileNames;
	private String dir;
	private int uploadPort;
	private LinkedList<Connection> requestQueue;
	private String hostname;
	private int instances = 5;
	private Socket serverSocket;
	private ServerSocket peerAsServer;
	private DataOutputStream dos;
	private DataInputStream dis;
	
	PeerProcess(Map<Integer, String> fileNames, String dir, int uploadPort, String hostname, Socket serverSocket){
		this.fileNames = fileNames;
		this.dir = dir;
		this.uploadPort = uploadPort;
		this.hostname = hostname;
		this.serverSocket = serverSocket;
		try{
			this.dos = new DataOutputStream(serverSocket.getOutputStream());
			this.dis = new DataInputStream(serverSocket.getInputStream());
		}catch(Exception e){
			System.out.println(e);
		}
		requestQueue = new LinkedList<Connection>();
	}
	
	void informServer(){
		System.out.println("Communicating with server...");
		try {
				for(Integer s:fileNames.keySet()){
					String request = Parser.createAddRequest(s, hostname, uploadPort, fileNames.get(s));
					dos.writeInt(request.length());
					dos.writeUTF(request);
					int dataLength = dis.readInt();
					String response = "";
					while(dataLength != 0){
						String temp = dis.readUTF();
						response += temp;
						dataLength -= temp.length();
					}
				System.out.println(response);
				}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		System.out.println("Done!");
		
	}

	
	void listenToConnections(){
		try {
			peerAsServer = new ServerSocket(uploadPort);
			
			while(true){
				Socket socket = peerAsServer.accept();
				synchronized(requestQueue){
					requestQueue.add(new Connection(socket,hostname,dir));
				}
			}
		} catch(Exception e){
    		System.out.println(e);
    	}
	}
	
	void actAsServer(){
		ExecutorService exe = Executors.newFixedThreadPool(instances);
		try{
			while(true){
				synchronized(requestQueue){
					if(requestQueue.peek() != null){
						Connection temp = requestQueue.removeFirst();
						exe.execute(new RequestHandler(temp.getSocket(),temp.getHostname(),temp.getDirectory()));
					}else{
						Thread.sleep(100);
					}
				}
			}
		}catch(Exception e){
			System.out.println(e);
		}
	}
	
	void close(){
		System.out.println("Communicating with server...");
		try{
//			for(Integer s:fileNames.keySet()){
				String request = Parser.createDeleteRequest(hostname, uploadPort);
				dos.writeInt(request.length());
				dos.writeUTF(request);
				int dataLength = dis.readInt();
				String response = "";
				while(dataLength != 0){
					String temp = dis.readUTF();
					response += temp;
					dataLength -= temp.length();
				}
			System.out.println(response);
//			}
		}catch(Exception e){
			System.out.println(e);
		}
	}
	
	String findFile(int rfc_num){
		String request = Parser.createLookUpRequest(rfc_num, hostname, uploadPort);
		String response = "";
		try{
			dos.writeInt(request.length());
			dos.writeUTF(request);
			int dataLength = dis.readInt();
			while(dataLength != 0){
				String temp = dis.readUTF();
				response += temp;
				dataLength -= temp.length();
			}
		}catch(Exception e){
			System.out.println(e);
		}
		return response.trim();
	}
	
	
	public void get(String hostname, int uploadPort, int RFC){
		try {
			String filename = "rfc"+RFC+".txt";
			Socket socket = new Socket(InetAddress.getByName(hostname), uploadPort);
			String request = Parser.createGetRequest(RFC, hostname);
			DataOutputStream peerdos = new DataOutputStream(socket.getOutputStream());
			DataInputStream peerdis = new DataInputStream(socket.getInputStream());
			peerdos.writeInt(request.length());
			peerdos.writeUTF(request);
			
			int headerLength = peerdis.readInt();
			System.out.println("Header length:"+headerLength);
			if(headerLength > 0){
				byte[] header = new byte[headerLength];
				peerdis.readFully(header,0,header.length);
				System.out.println(new String(header));
			}
			int dataLength = peerdis.readInt();
			System.out.println("Data Length:"+dataLength);
			if(dataLength > 0){
				byte[] data = new byte[dataLength];
				peerdis.readFully(data, 0, data.length);
				System.out.println(new String(data));
				File f = new File(dir+"/"+filename);
				FileOutputStream fos = new FileOutputStream(f);
				fos.write(data, 0, data.length);
				System.out.println("File "+filename+" has been downloaded from "+ hostname+":"+uploadPort);
				String title = Utilities.getTitle(filename, new File(dir));
				String serverRequest = Parser.createAddRequest(RFC, this.hostname, this.uploadPort, title);
				dos.writeInt(serverRequest.length());
				dos.writeUTF(serverRequest);
				int length = dis.readInt();
				String response = "";
				while(length != 0){
					String temp = dis.readUTF();
					response += temp;
					length -= temp.length();
				}
//				System.out.println(response);
				fileNames.put(RFC, title);	
				fos.close();
				peerdos.close();
				peerdis.close();
				socket.close();
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public String listRequest(){
		String request = Parser.createListRequest(this.hostname, this.uploadPort);
		String response = "";
		try{
			dos.writeInt(request.length());
			dos.writeUTF(request);
			int dataLength = dis.readInt();
			while(dataLength != 0){
				String temp = dis.readUTF();
				response += temp;
				dataLength -= temp.length();
			}
		}catch(Exception e){
			System.out.println(e);
		}
		return response;
	}
}

class Connection{
	private String hostname;
	private String dir;
	private Socket socket;
	
	public Connection(Socket socket, String hostname, String dir){
		this.socket = socket;
		this.hostname = hostname;
		this.dir = dir;
	}
	
	public String getHostname(){
		return hostname;
	}
	public String getDirectory(){
		return dir;
	}
	public Socket getSocket(){
		return socket;
	}
}
