package sts.sync.message;

import java.util.Arrays;

import sts.MicroSecondTime;

public class SyncMessageBuilder {
	// {"type": "REQUEST", "messageClass": "NOMSnapshot", "time": [1358118825, 729971], "xid": 1, "name": "", "value": null, "fingerPrint": null}
	private String type;
	private String messageClass;
	private long[] time;
	private long xid;
	private String name;
	private String value;
	private String fingerPrint;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getMessageClass() {
		return messageClass;
	}
	public void setMessageClass(String messageClass) {
		this.messageClass = messageClass;
	}
	public long[] getTime() {
		return time;
	}
	public void setTime(long[] time) {
		this.time = time;
	}
	public long getXid() {
		return xid;
	}
	public void setXid(long xid) {
		this.xid = xid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String values) {
		this.value = values;
	}
	public String getFingerPrint() {
		return fingerPrint;
	}
	public void setFingerPrint(String fingerPrint) {
		this.fingerPrint = fingerPrint;
	}
	
	public SyncMessage getMessage() {
		SyncMessage.Type type = SyncMessage.Type.valueOf(this.type);
		if(getMessageClass().equals("NOMSnapshot")) {
			return new NOMSnapshot(type, xid, new MicroSecondTime(time[0], time[1]));
		} else {
			throw new RuntimeException("unknown message class: "+getMessageClass() + " in " + this);
		}
	}
	@Override
	public String toString() {
		return "SyncMessageBuilder [type=" + type + ", messageClass="
				+ messageClass + ", time=" + Arrays.toString(time) + ", xid="
				+ xid + ", name=" + name + ", value=" + value
				+ ", fingerPrint=" + fingerPrint + "]";
	}
}
