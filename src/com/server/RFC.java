package com.server;

public class RFC{
	private int rfc_number;
	private String rfc_name;
	private String hostname;
	
	RFC(int rfc_number, String rfc_name, String hostname){
		this.rfc_number = rfc_number;
		this.rfc_name = rfc_name;
		this.hostname = hostname;
	}
	public String toString(){
		return (rfc_number+":"+rfc_name+":"+hostname);
	}
	public String getRFCname() {
		return rfc_name;
	}
	public String getHostname(){
		return this.hostname;
	}
	public int getRFCnum(){
		return this.rfc_number;
	}
}
