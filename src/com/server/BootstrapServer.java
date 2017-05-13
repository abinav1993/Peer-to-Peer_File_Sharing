package com.server;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class BootstrapServer {
	private static LinkedList<Socket> requestQueue = new LinkedList<Socket>();
	private static LinkedList<Peer> peersList = new LinkedList<Peer>();
	private static LinkedList<RFC> rfcList = new LinkedList<RFC>();
	private static HashMap<String,Integer> hostToPort = new HashMap<String, Integer>();
	private static ServerSocket socket;
	private static int instances = 5;

	public static LinkedList<Peer> getPeersList() {
		return peersList;
	}

	public static LinkedList<RFC> getRfcList() {
		return rfcList;
	}
	
	public static HashMap<String,Integer> getHostToPort(){
		return hostToPort;
	}

	public static void main(String[] args) {
		
		if(args.length != 1){
			System.out.println("Format: <Server Port>");
			return;
		}
		
		int serverPort = 0;
		try{
			serverPort = Integer.parseInt(args[0]);
			socket = new ServerSocket(serverPort);
			System.out.println("Bootstrap Server is running. To exit press 'Ctrl + C or Ctrl + Z' ");
			Thread listen = new Thread("listenToRequests"){
				public void run(){
					try{
						listenToConnections();
					}catch(Exception e){
						System.out.println(e.getMessage());
					}
				}
			};
			listen.start();
			
			Thread serveClients = new Thread("serveClients"){
				public void run(){
					try{
						actAsServer();
					}catch(Exception e){
						System.out.println(e.getMessage());
					}
				}
			};
			serveClients.start();
		}catch(Exception e){
			System.out.println("Error:"+e.getMessage());
		}
	}
	
	public static void listenToConnections(){
		while(true){
			try{
				Socket handlerSocket = socket.accept();
				synchronized(requestQueue){
					requestQueue.add(handlerSocket);
				}
			}catch(Exception e){
				System.out.println(e.getMessage());
			}
		}
	}
	
	public static void actAsServer(){
		while(true){
			try{
				if(requestQueue.peek() != null){
					ExecutorService exe = Executors.newFixedThreadPool(instances);
					synchronized(requestQueue){
						Socket tempsocket = requestQueue.removeFirst();
						exe.execute(new RequestHandler(tempsocket));
					}
					
				}else{
					Thread.sleep(100);
				}
			}catch(Exception e){
				System.out.println(e.getMessage());
			}
		}
	}
}

class Peer{
	private String hostname;
	private int uploadPort;
	
	Peer(String hostname, int port){
		this.hostname = hostname;
		uploadPort = port;
	}
	public String toString(){
		return (hostname + ":" + uploadPort);
	}
	public int getPortNum(){
		return this.uploadPort;
	}
	public String getHostname(){
		return this.hostname;
	}
}

