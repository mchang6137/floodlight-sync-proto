package sts;

import sts.sync.STSSync;
import net.floodlightcontroller.core.IFloodlightProviderService.Role;
import net.floodlightcontroller.core.IFloodlightProviderService;
// import net.floodlightcontroller.core.internal.Controller;

public aspect STSStateChangeAspect {
	private STSSync sync;
	
	public STSStateChangeAspect() {
	}

	public void setSync(STSSync sync) {
		this.sync = sync;
	}
	
	pointcut setRoleCut(Role role) : call(void setRole(Role)) && target(IFloodlightProviderService) && args(role);	

	before(Role role): setRoleCut(role) {
		if(sync != null)
			sync.beforeStatusChange("role", role.toString());
		System.err.println("XXXXXX Setting role to "+role);
	}
	
	after(Role role): setRoleCut(role) {
		if(sync != null)
			sync.afterStatusChange("role", role.toString());
		System.err.println("XXXXXX Setting role to "+role);
	}
	
}
