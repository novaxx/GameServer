package com.nova.server.domain;

public class Message {
	private int mMsgId;
	
	public Message(String message) {
		
	}
	
	public void setMessageId(int id) {
		mMsgId = id;
	}
	
	public int getMessageId() {
		return mMsgId;
	}
}
