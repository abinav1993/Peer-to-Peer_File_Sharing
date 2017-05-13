package com.server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Pattern;
import com.util.*;

public class RequestHandler implements Runnable{
	private Socket socket;
	private DataInputStream dis = null;
	private DataOutputStream dos = null;
	
	
	public RequestHandler(Socket socket) {
		this.socket = socket;
	}
	
	public void run() {		
		try {
	        dis = new DataInputStream(socket.getInputStream());
	        dos = new DataOutputStream(socket.getOutputStream());
	        String values[];
	        LinkedList<Peer> peerList = BootstrapServer.getPeersList();
	        LinkedList<RFC> rfcList = BootstrapServer.getRfcList();
	        HashMap<String,Integer> hostToPort = BootstrapServer.getHostToPort();
	        while(true){
	        	int dataLength = dis.readInt();
	        	String request = "";
	        	while(dataLength != 0){
	        		String temp = dis.readUTF();
	        		request += temp;
	        		dataLength = dataLength - temp.length();
	        	}
	        	if(dataLength == 0){
		        	values = Parser.parse(request).split(Pattern.quote("$"));
		        	if(values[0].equals("ADD")){
		        		addPeer(peerList,new Peer(values[3], Integer.parseInt(values[2])));
		        		RFC r = new RFC(Integer.parseInt(values[1]),values[4],values[3]);
		        		add(rfcList, hostToPort, r,Integer.parseInt(values[2]));
//		        		printList(peerList,rfcList);
		        		String response = Parser.createAddResponse(200,"OK",Integer.parseInt(values[1]),values[4],values[3],Integer.parseInt(values[2]));
		        		dos.writeInt(response.length());
		        		dos.writeUTF(response);
		        	}
		        	else if(values[0].equals("LOOKUP")){
		        		int rfc_num = Integer.parseInt(values[1]);
		        		ArrayList<ResultSet> result = Lookup(rfcList,hostToPort,rfc_num,values[4]);
		        		String response;
		        		if(result.size() != 0)
		        			response = Parser.createListResponse(200, "OK", result);
		        		else{
		        			response = Parser.createServerResponse(404, "File not found");
		        		}
		        		System.out.println(response);
		        		dos.writeInt(response.length());
		        		dos.writeUTF(response);
		        	}
		        	else if(values[0].equals("DELETE")){
		        		String hostname = values[3];
		        		int port = Integer.parseInt(values[2]);
		        		remove(rfcList, hostToPort, hostname);
		        		removePeer(peerList, new Peer(hostname,port));
//		        		printList(peerList,rfcList);
		        		String response = "";
		        		response += Parser.createServerResponse(200, "OK");
		        		response +="Removed RFC's from Server's List!";
		        		dos.writeInt(response.length());
		        		dos.writeUTF(response);
		        	}
		        	else if(values[0].equals("LIST")){
		        		ArrayList<ResultSet> result = List(rfcList, hostToPort);
		        		String response = Parser.createListResponse(200, "OK", result);
		        		dos.writeInt(response.length());
		        		dos.writeUTF(response);
		        	}
	        	}
	        }
	    }catch(Exception e){
//	        	System.out.println(e);
	        }

	}
	
	void addPeer(LinkedList<Peer> peerList, Peer newPeer){
		try{
        	synchronized(peerList){
        		peerList.addFirst(newPeer);
        	}
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	
	void removePeer(LinkedList<Peer> peerList, Peer p){
		try{
        	synchronized(peerList){
        		peerList.remove(p);
        	}
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	
	synchronized void add(LinkedList<RFC> rfcList, HashMap<String,Integer> hostToPort, RFC newRFC,int uploadPort){
		try{
				rfcList.addFirst(newRFC);
				if(!hostToPort.containsKey(newRFC.getHostname()))
					hostToPort.put(newRFC.getHostname(),uploadPort);
			}catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	
	synchronized void remove(LinkedList<RFC> rfcList, HashMap<String,Integer> hostToPort, String hostname){
		ArrayList<RFC> result = new ArrayList<RFC>();
		try{
			for(RFC r:rfcList){
				if(r.getHostname().equals(hostname))
					result.add(r);
			}
			rfcList.removeAll(result);
			hostToPort.remove(hostname);
		}catch(Exception e){
			System.out.println(e);
		}
	}
	
	synchronized ArrayList<ResultSet> Lookup(LinkedList<RFC> rfcList, HashMap<String,Integer> hostToPort, int rfc_num, String title){
		ArrayList<ResultSet> result = new ArrayList<ResultSet>();
		for(RFC r : rfcList){
			if(r.getRFCnum() == rfc_num){ //&& r.getRFCname().equals(title)){
				result.add(new ResultSet(r,hostToPort.get(r.getHostname())));
			}
		}
		return result;
	}
	
	synchronized ArrayList<ResultSet> List(LinkedList<RFC> rfcList, HashMap<String,Integer> hostToPort){
		ArrayList<ResultSet> result = new ArrayList<ResultSet>();
		for(RFC r : rfcList){
				result.add(new ResultSet(r,hostToPort.get(r.getHostname())));
		}
		return result;
	}
	
	synchronized void printList(LinkedList<Peer> peerList, LinkedList<RFC> rfcList){
		for(Peer p:peerList)
			System.out.println(p.toString());
		for(RFC r:rfcList)
			System.out.println(r.toString());
	}
}

