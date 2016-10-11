package com.android.redenvelope;

public class Record {
	private String userName;
	private String receiveTime;
	private float money;
	private long usedTime;
	
	public Record(String userName, float money, long usedTime, String receiveTime) {
		this.userName = userName;
		this.money = money;
		this.usedTime = usedTime;
		this.receiveTime = receiveTime;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getReceiveTime() {
		return receiveTime;
	}
	
	public void setReceiveTime(String receiveTime) {
		this.receiveTime = receiveTime;
	}
	
	public float getMoney() {
		return money;
	}
	
	public void setMoney(float money) {
		this.money = money;
	}
	
	public long getUsedTime() {
		return usedTime;
	}
	
	public void setUsedTime(long usedTime) {
		this.usedTime = usedTime;
	}	
}
