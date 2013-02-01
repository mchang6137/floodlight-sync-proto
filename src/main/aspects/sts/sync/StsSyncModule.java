package sts.sync;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import sts.STSLogAspect;
import sts.STSStateChangeAspect;
import sts.SimpleLogger;

public class StsSyncModule implements IFloodlightModule {
    protected static SimpleLogger log = SimpleLogger.getLogger(StsSyncModule.class);
	private StsSyncService sync;

	/** IFloodLightModule implementation */
	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		return Collections.emptyList();
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		return Collections.emptyMap();
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		return Collections.emptyList();
	}

	@Override
	public void init(FloodlightModuleContext context)
			throws FloodlightModuleException {
		log.info("Initializing StsSyncModule");
		Map<String, String> configParams = context.getConfigParams(this);
		String spec  = configParams.get("sync");
		
		sync = new StsSyncService(spec);
		
		STSStateChangeAspect.aspectOf().setSync(sync);
		STSLogAspect.aspectOf().setSync(sync);		
	}

	@Override
	public void startUp(FloodlightModuleContext context) {
		sync.connect();
	}

}
