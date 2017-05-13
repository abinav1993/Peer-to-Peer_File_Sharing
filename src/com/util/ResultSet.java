package com.util;

import com.server.RFC;

public class ResultSet{
	private RFC r;
	private int uploadPort;
	public ResultSet(RFC r, int uploadPort){
		this.r = r;
		this.uploadPort = uploadPort;
	}
	public RFC getRFC() {
		return r;
	}
	public int getUploadPort() {
		return uploadPort;
	}
}
