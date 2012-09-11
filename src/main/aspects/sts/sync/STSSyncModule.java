package sts.sync;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import sts.STSStateChangeAspect;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;

public class STSSyncModule implements IFloodlightModule {
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
		Map<String, String> configParams = context.getConfigParams(this);
		String spec  = configParams.get("sync");
		STSSync sync = new STSSync(spec);
		
		STSStateChangeAspect.aspectOf().setSync(sync);		
	}

	@Override
	public void startUp(FloodlightModuleContext context) {
	}

}
