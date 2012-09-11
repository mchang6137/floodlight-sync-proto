package sts;

public interface MicrosecondTimeSource {
	public static MicrosecondTimeSource DEFAULT = HorribleMicroSecondTimeSource.getInstance();
	
	MicroSecondTime getMicroSecondTime();
}
