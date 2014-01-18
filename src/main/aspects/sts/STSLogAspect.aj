package sts;

import sts.sync.StsSyncService;
import org.slf4j.Logger;
// import net.floodlightcontroller.core.internal.Controller;

public aspect STSLogAspect {
	SimpleLogger log = SimpleLogger.getLogger(STSLogAspect.class);
	
	private StsSyncService sync;
	
	public STSLogAspect() {
	}

	public void setSync(StsSyncService sync) {
		this.sync = sync;
	}
	
	pointcut logInfo(Logger logger, String msg) : target(logger) && args(msg, ..) &&
				( call(void org.slf4j.Logger.trace(String, ..)) || 
				  call(void org.slf4j.Logger.debug(String, ..)) || 
				  call(void org.slf4j.Logger.info(String, ..)) || 
				  call(void org.slf4j.Logger.warn(String, ..)) || 
				  call(void org.slf4j.Logger.error(String, ..)) || 
				  call(void org.slf4j.Logger.fatal(String, ..)) 
				  );

	after(Logger logger, String msg): logInfo(logger, msg) {
		//log.debug("Log Aspect: "+msg);
		if(sync != null)
			sync.afterStatusChange("log", "{'logger'"+logger.getName() + ", 'msg': "+msg+ "}");
	}		
}
