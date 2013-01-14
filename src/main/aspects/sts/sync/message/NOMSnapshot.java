package sts.sync.message;

import net.floodlightcontroller.proact.Proact;
import sts.MicroSecondTime;


public class NOMSnapshot extends SyncMessage {

	private final Object value;
	
	public NOMSnapshot(Type type, long xid, MicroSecondTime time) {
		super(type, xid, time);
		value = null;
	}

	public NOMSnapshot(Type type, long xid, Object value) {
		super(type, xid);
		this.value = value;
	}
	
	public static NOMSnapshot request(long xid, MicroSecondTime time) {
		return new NOMSnapshot(Type.REQUEST, xid, time);
	}

	public Object getValue() {
		return value;
	}
	
	public SyncMessage execute() {
		return new NOMSnapshot(Type.RESPONSE, getXid(), Proact.lastInstance.getNomSnapShot());
	}
	
}
