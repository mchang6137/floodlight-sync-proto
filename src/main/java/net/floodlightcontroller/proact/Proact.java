/**
*    Copyright 2011, Big Switch Networks, Inc. 
*    Originally created by David Erickson, Stanford University
* 
*    Licensed under the Apache License, Version 2.0 (the "License"); you may
*    not use this file except in compliance with the License. You may obtain
*    a copy of the License at
*
*         http://www.apache.org/licenses/LICENSE-2.0
*
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
*    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
*    License for the specific language governing permissions and limitations
*    under the License.
**/

package net.floodlightcontroller.proact;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IFloodlightProviderService.Role;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.storage.IResultSet;
import net.floodlightcontroller.storage.IStorageSourceService;
import net.floodlightcontroller.storage.OperatorPredicate;
import net.floodlightcontroller.storage.OperatorPredicate.Operator;
import net.floodlightcontroller.storage.RowOrdering;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPacketOut;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFPortStatus;
import org.openflow.protocol.OFType;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.util.U16;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author David Erickson (daviderickson@cs.stanford.edu) - 04/04/10
 */
public class Proact implements IFloodlightModule, IOFMessageListener, IOFSwitchListener {
    protected static Logger log = LoggerFactory.getLogger(Proact.class);

    protected IFloodlightProviderService floodlightProvider;

	private IStorageSourceService storage;

	public static Proact lastInstance;
	
	public Proact() {
	}	
	
    /**
     * @param floodlightProvider the floodlightProvider to set
     */
    public void setFloodlightProvider(IFloodlightProviderService floodlightProvider) {
        this.floodlightProvider = floodlightProvider;
    }

    @Override
    public String getName() {
        return Proact.class.getPackage().getName();
    }

    public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
    	if(msg instanceof OFPortStatus) {
    		handlePortStatus((OFPortStatus) msg);
    	} else if(msg instanceof OFPacketIn) {
    		OFPacketIn pi = (OFPacketIn) msg;
    		log.info("Ignoring packetin {} sw: {}, in: {}", new Object[] { msg, sw, pi.getInPort()});
    	}
    	
        return Command.CONTINUE;
    }

    private void handlePortStatus(OFPortStatus msg) {
		// TODO Auto-generated method stub		
	}

	@Override
    public boolean isCallbackOrderingPrereq(OFType type, String name) {
        return false;
    }

    @Override
    public boolean isCallbackOrderingPostreq(OFType type, String name) {
        return false;
    }

    // IFloodlightModule
    
    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        // We don't provide any services, return null
        return null;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService>
            getServiceImpls() {
        // We don't provide any services, return null
        return null;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>>
            getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l = 
                new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IFloodlightProviderService.class);
        l.add(IStorageSourceService.class);

        return l;
    }

    @Override
    public void init(FloodlightModuleContext context)
            throws FloodlightModuleException {
        log.info("Proact init start");

        floodlightProvider =
                context.getServiceImpl(IFloodlightProviderService.class);
        storage = context.getServiceImpl(IStorageSourceService.class);
        log.info("Got storage: "+storage);
        HashSet<String> set = new HashSet<String>();
        set.add("id");
        storage.createTable("rules", set);        
        
        storage.insertRow("rules", map(
        		"id", 1,
        		"dpid", 1,
        		"match", match(1),
        		"action", output(2)
        		));
        
        storage.insertRow("rules", map(
        		"id", 2,
        		"dpid", 1,
        		"match", match(2),
        		"action", output(1)
        		));
        
        storage.insertRow("rules", map(
        		"id", 3,
        		"dpid", 2,
        		"match", match(1),
        		"action", output(2)
        		));    
        
        storage.insertRow("rules", map(
        		"id", 4,
        		"dpid", 2,
        		"match", match(2),
        		"action", output(1)
        		));
        floodlightProvider.addOFSwitchListener(this);
        log.info("Proact OK");
    }

    OFMatch match(int inport){
    	OFMatch m = new OFMatch();
    	m.setInputPort((short) inport);
    	m.setWildcards(OFMatch.OFPFW_ALL & ~OFMatch.OFPFW_IN_PORT);
    	return m;
    }
    
    OFActionOutput output(int port) {
    	OFActionOutput o = new OFActionOutput((short) port, (short) 0);
    	return o;
    }
    
    Map<String, Object> map(Object...objects) {
    	Map<String, Object> res = new LinkedHashMap<String, Object>();
    	if(objects.length % 2 > 0) {
    		throw new IllegalArgumentException("must have even arg count");
    	}
    	for(int i=0; i < objects.length; i+=2) {
    		res.put((String) objects[i], objects[i+1]);
    	}
    	return res;
    }
    
    @Override
    public void startUp(FloodlightModuleContext context) {
        floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
		lastInstance = this;
    }
    
    class NOMSnapshot {
    	private List<NOMSnapshotSwitch> switches;
    	private List<Object> hosts;
    	private List<Object> links;
    	
    	public NOMSnapshot() {
    		switches = new ArrayList<NOMSnapshotSwitch>();
    		hosts = new ArrayList<Object>();
    		links = new ArrayList<Object>();    		
		}

		public NOMSnapshotSwitch switchForDpid(long dpid) {
			for(NOMSnapshotSwitch s : switches) {
				if(s.getDpid() == dpid)
					return s;
			}
			NOMSnapshotSwitch s = new NOMSnapshotSwitch(dpid);
			switches.add(s);
			return s;
		}

		public List<NOMSnapshotSwitch> getSwitches() {
			return switches;
		}

		public List<Object> getHosts() {
			return hosts;
		}

		public List<Object> getLinks() {
			return links;
		}
				
    }
    
    class NOMSnapshotSwitch {
		private long dpid;
		private NOMSnapshotFlowtable flowTable;

		public NOMSnapshotSwitch(long dpid) {
			this.dpid = dpid;
			this.flowTable = new NOMSnapshotFlowtable();
		}

		public long getDpid() {
			return dpid;
		}

		public NOMSnapshotFlowtable getFlowTable() {
			return flowTable;
		}	
    }
    
    class NOMSnapshotFlowtable {
    	private List<TableEntry> entries;
    	
    	public NOMSnapshotFlowtable() {
    		entries = new ArrayList<TableEntry>();
    	}
    	
		public void addEntry(OFMatch ofMatch, OFAction ofAction) {
			entries.add(new TableEntry(ofMatch, ofAction));			
		}

		public List<TableEntry> getEntries() {
			return entries;
		}
		
    }
    
    class TableEntry {
    	private Map<String, Object> match;
		private List<OFAction> actions;
    	
    	public TableEntry(OFMatch ofMatch, OFAction ofAction) {
        	match = new HashMap<String, Object>();
        	
    		match.put("in_port", ofMatch.getInputPort());
    		match.put("wildcards", ofMatch.getWildcards());
    		
    		actions = new ArrayList<OFAction>();
    		actions.add(ofAction);
    	}

		public Map<String, Object> getMatch() {
			return match;
		}

		public List<OFAction> getActions() {
			return actions;
		}
    }
    

	public NOMSnapshot getNomSnapShot() {    	
    	IResultSet result = storage.executeQuery("rules", new String[] {"dpid", "match", "action" }, null, new RowOrdering());

    	NOMSnapshot res = new NOMSnapshot();
    	
    	Map<Long, NOMSnapshotSwitch> switchMap = new LinkedHashMap<Long, NOMSnapshotSwitch>();
    	
    	while(result.next()) {
    		long dpid = result.getLong("dpid");
    		
    		NOMSnapshotSwitch sw = res.switchForDpid(dpid);
    		Map<String, Object> row = result.getRow();
    		sw.getFlowTable().addEntry(((OFMatch) row.get("match")), ((OFAction) row.get("action")));
    	}
    	
    	return res;  	    	
    }

    /**
     * Get summary counters registered by all modules
     * @author shudongz
     */
    public static class ProactResource extends ServerResource {
        @Get("json")
        public List<Map<String, Object>> retrieve() {
            IStorageSourceService storage = (IStorageSourceService)getContext().
                    getAttributes().get(IStorageSourceService.class.getCanonicalName());
        	
			IResultSet result = storage.executeQuery("rules", new String[] {"dpid", "match", "action" }, null, new RowOrdering());
			List<Map<String, Object>> l = new ArrayList<Map<String, Object>>();
 			while(result.next()) {
 				l.add(result.getRow());
 			}
 			return l;
        }
    }
        
	@Override
	public void addedSwitch(IOFSwitch sw) {
		if(floodlightProvider.getRole() != Role.SLAVE) {
			List<OFMessage> messages = new ArrayList<OFMessage>();
			
			IResultSet result = storage.executeQuery("rules", new String[] {"match", "action" }, new OperatorPredicate("dpid", Operator.EQ, sw.getId()), new RowOrdering());
			while(result.next()) {
				OFFlowMod flowMod = new OFFlowMod();
				flowMod.setXid(sw.getNextTransactionId());
				flowMod.setMatch((OFMatch) result.getRow().get("match"));
				ArrayList<OFAction> list = new ArrayList<OFAction>();
				list.add((OFAction) result.getRow().get("action"));
				flowMod.setActions(list);
				messages.add(flowMod);
			}
			try {
				sw.write(messages, null);
			} catch (IOException e) {
				throw new IllegalArgumentException(e);
			}
		}
	}

	@Override
	public void removedSwitch(IOFSwitch sw) {
	}

}
