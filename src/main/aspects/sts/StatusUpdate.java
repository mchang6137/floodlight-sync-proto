package sts;

public class StatusUpdate {
	
	public static enum UpdateType { PRE, POST }

	private final MicroSecondTime time;
	private final UpdateType type;
	private final String fingerprint;
	private final String data;
	
	public static MicrosecondTimeSource timeSource = HorribleMicroSecondTimeSource.getInstance();
	
	public StatusUpdate(UpdateType type, String fingerprint, String data) {
		this.type = type;
		this.fingerprint = fingerprint;
		this.data = data;
		this.time = timeSource.getMicroSecondTime();	
	}

	public MicroSecondTime getTime() {
		return time;
	}

	public UpdateType getType() {
		return type;
	}

	public String getFingerprint() {
		return fingerprint;
	}

	public String getData() {
		return data;
	}
	
	
}
