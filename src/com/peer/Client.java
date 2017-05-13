package com.peer;

import java.io.*;
import java.net.*;
import java.util.*;

public class Client {

	public static void main(String[] args) throws Exception {
		
		if(args.length != 4){
			System.out.println("Format: <Client Port> <Dir> <Server Address> <Server Port>");
			return;
		}
		
		ArrayList<String> ipList = new ArrayList<String>();
		Enumeration<NetworkInterface> enum1 = NetworkInterface.getNetworkInterfaces();
		System.out.println("Available IP addresses on client machine are:");
		while(enum1.hasMoreElements())
		{
		    NetworkInterface n = (NetworkInterface) enum1.nextElement();
		    if(!n.isPointToPoint()){
			    Enumeration<InetAddress> enum2 = n.getInetAddresses();
			    while (enum2.hasMoreElements())
			    {
			        InetAddress i = (InetAddress) enum2.nextElement();
			        if(!i.isLinkLocalAddress() && !i.isMulticastAddress() && !i.isLoopbackAddress()){
			        	System.out.println(i.getHostAddress());
			        	ipList.add(i.getHostAddress().toString());
			        }
			    }
		    }
		}
		System.out.println("From the above list, choose an IP address for the Client by entering from 0 to "+(ipList.size() - 1) + ":");
		Scanner s = new Scanner(System.in);
		int index = 0;
		try{
			
			 index = s.nextInt();
			 System.out.println("Selected Client IP address is: " + ipList.get(index));
		}catch(Exception ex){
			System.out.println("Select a number from 0 & "+(ipList.size() - 1));
			return;
		}
		String clientName = ipList.get(index); //InetAddress.getLocalHost().getHostAddress().toString();
		String dir = args[1];
		String serverAddress = args[2]; 
		int serverPort = 0, clientPort = 0;
		try{
			serverPort = Integer.parseInt(args[3]);
			clientPort = Integer.parseInt(args[0]);
		}catch(Exception e){
			System.out.println("Enter valid port numbers.");
		}
		
		File f = new File(dir);
		if(!f.isDirectory()){
			System.out.println("Enter valid directory");
			return;
		}
		Map<Integer, String> fileNames = Utilities.getFileNames(f);
		
		Socket server = null;
		try{
			server = new Socket(serverAddress, serverPort);
			
			PeerProcess p = new PeerProcess(fileNames, dir, clientPort, clientName, server);
			p.informServer();
			
			Thread listen = new Thread("listenToRequests"){
				public void run(){
					try{
						p.listenToConnections();
					}catch(Exception e){
						System.out.println(e.getMessage());
					}
				}
			};
			listen.start();
			
			Thread serveClients = new Thread("serveClients"){
				public void run(){
					try{
						p.actAsServer();
					}catch(Exception e){
						System.out.println(e.getMessage());
					}
				}
			};
			serveClients.start();
			
			Scanner reader = new Scanner(System.in);
			while(true){
				System.out.println("******MENU******");
				System.out.println("1. Give the RFC Number to download the file");
				System.out.println("2. Fetch the RFC list from server");
				System.out.println("3. Leave the system");
				System.out.println("Enter an option: ");
				int input = reader.nextInt();
				if(input == 1){
					int rfc_num = 0;
					int source = 0;
					try{
						System.out.println("Enter the RFC Number of the file");
						rfc_num = reader.nextInt();
						if(fileNames.containsKey(rfc_num)){
							System.out.println("\nThe specified RFC is already present in the local system \n");
						}else{
							String response = p.findFile(rfc_num);
							String address[] = response.split("\r\n");
							if(address[0].indexOf("404") != -1){
								System.out.println("\nNone of the peers have the given RFC\n");
							}
							else{
								System.out.println();
								System.out.println("Selected file is available with following peers:");
								System.out.println(response);
								System.out.println("From which source do you want to download the RFC?");
								source = reader.nextInt();
								if(source > address.length){
									System.out.println("Enter valid source number");
								}
								System.out.println("Selected Source: " + address[source + 1]);
								String details[] = address[source + 1].split(" ");
								String hostname = details[details.length - 2];
								int uploadPort = Integer.parseInt(details[details.length - 1]);
								int rfc = Integer.parseInt(details[0]);
								p.get(hostname,uploadPort,rfc);
								fileNames = Utilities.getFileNames(f);
								
							}
						}
						
					}catch(Exception e){
						System.out.println(e);
					}
				}else if(input == 2){
					System.out.println();
					System.out.println("Response from the Server:");
					System.out.println(p.listRequest());
				}else if(input == 3){
					System.out.println("Closing down...");
					p.close();
					break;
				}else{
					System.out.println("\nEnter a valid number!\n");
				}
			}
			reader.close();
			System.exit(0);
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
	}

}
