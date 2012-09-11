package sts.sync.message;

import org.python.google.common.base.Strings;

public class StateChange extends SyncMessage {

	private final String fingerPrint;
	private final String name;
	private final String value;
	
	public StateChange(String name, String value, String fingerprint) {
		super(SyncMessage.Type.ASYNC);
		this.name = name;
		this.value = value;
		if(Strings.isNullOrEmpty(fingerprint)) {
			this.fingerPrint = generateFingerPrint();
		} else {
			this.fingerPrint = fingerprint;
		}
	}

	public StateChange(String name, String value) {
		this(name, value, null);
	}
	
	private String generateFingerPrint() {
		return name + "=" + value;
	}

	public String getFingerPrint() {
		return fingerPrint;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}
	
}
