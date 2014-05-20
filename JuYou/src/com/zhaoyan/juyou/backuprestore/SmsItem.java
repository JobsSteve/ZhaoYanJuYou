package com.zhaoyan.juyou.backuprestore;

public class SmsItem implements Comparable<SmsItem> {
	String address;  //收件人
    String person;  //
    String date;  //收发件日期
    String readByte;  //已读，未读
    String boxType;  //1：收件箱 2：发件箱
    String body;  //短信内容
    String locked;  //
    String seen;//已读，未读
    String sc;
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getPerson() {
		return person;
	}
	public void setPerson(String person) {
		this.person = person;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getReadByte() {
		return readByte == null ? "READ" : readByte;
	}
	public void setReadByte(String read) {
		this.readByte = read;
	}
	public String getBoxType() {
		return boxType;
	}
	public void setBoxType(String type) {
		this.boxType = type;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getLocked() {
		return locked;
	}
	public void setLocked(String locked) {
		this.locked = locked;
	}
	public String getSeen() {
		return seen == null ? "1" : seen;
	}
	public void setSeen(String seen) {
		this.seen = seen;
	}  
    
	public String getSC(){
		return sc;
	}
	
	public void setSC(String sc){
		this.sc = sc;
	}
	
	@Override
	public int compareTo(SmsItem another) {
		return this.date.compareTo(another.date);
	}
    
}
