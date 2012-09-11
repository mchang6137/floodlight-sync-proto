package sts.sync.message;

import java.util.concurrent.atomic.AtomicLong;

import sts.MicroSecondTime;
import sts.MicrosecondTimeSource;

public class SyncMessage {
	static public enum Type { REQUEST, RESPONSE, ASYNC }
	static AtomicLong xidGenerator = new AtomicLong(1);
	
	private final Type type;
	private final MicroSecondTime time;
	private final long xid;
	
	public SyncMessage(Type type, long xid) {
		if(type.equals(Type.RESPONSE) && xid <= 0 ) {
			throw new IllegalArgumentException("XID must be specified for response messages");
		} else if(!type.equals(Type.RESPONSE) && xid > 0 ) {
			throw new IllegalArgumentException("XID must not be specified for other messages");
		}
		
		this.type = type;
		if(xid>0)
			this.xid = xid;
		else
			this.xid = xidGenerator.getAndIncrement();
		time = MicrosecondTimeSource.DEFAULT.getMicroSecondTime();
	}
	
	public SyncMessage(Type type) {
		this(type, -1);
	}

	public Type getType() {
		return type;
	}

	public MicroSecondTime getTime() {
		return time;
	}

	public long getXid() {
		return xid;
	}
}
