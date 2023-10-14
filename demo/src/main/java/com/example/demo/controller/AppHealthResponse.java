package com.example.demo.controller;

public class AppHealthResponse {
	
	private String responseStatus;
	private String responseMsg;
	public AppHealthResponse(){}
	
	public AppHealthResponse(String responseStatus, String responseMsg) {
		this.responseStatus = responseStatus;
		this.responseMsg =responseMsg;
	}
	public String getResponseStatus() {
		return responseStatus;
	}
	public String getResponseMsg() {
		return responseMsg;
	}
	public void setResponseStatus(String responseStatus) {
		this.responseStatus = responseStatus;
	}
public void setResponseMsg(String responseMsg) {
	this.responseMsg = responseMsg;
		
	}
}
