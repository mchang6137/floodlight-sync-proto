package net.floodlightcontroller.storage.distmem;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IFloodlightProviderService.Role;
import net.floodlightcontroller.core.internal.Controller;
import net.floodlightcontroller.storage.distmem.Update.UpdateType;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Update implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static enum UpdateType {
		NEW, INSERT, REMOVE, CREATE, UPDATE
	}

	UpdateType type;
	Object key;
	Map<String, Object> rowValue;
	final String table;

	public Update(UpdateType type, String table, Object key, Map<String, Object> rowValue) {
		super();
		this.type = type;
		this.table = table;
		this.key = key;
		this.rowValue = rowValue;
	}

	public static Update deleteRow(String tableName, Object rowKey) {
		return new Update(UpdateType.REMOVE, tableName, rowKey, null);
	}

	public static Update insertRow(String tableName, Object rowKey,	Map<String, Object> row) {
		return new Update(UpdateType.INSERT, tableName, rowKey, row);
	}

	public static Update updateRow(String tableName, Object rowKey,	Map<String, Object> row) {
		return new Update(UpdateType.UPDATE, tableName, rowKey, row);
	}
}

public class DistSyncService {
    protected static Logger log = LoggerFactory.getLogger(Controller.class);
	
	class SyncReceiveHandler extends SimpleChannelUpstreamHandler {
		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
				throws Exception {
			Update u = (Update) e.getMessage();
			if (u.type == UpdateType.NEW) {
				tableSource.getTable(u.table).newRow(u.key);
			} else if (u.type == UpdateType.INSERT || u.type == UpdateType.UPDATE) {
				MemoryTable table = tableSource.getTable(u.table);
				synchronized(table) {
					Map<String, Object> row = table.getRow(u.key);
					if(row == null) {
						row = table.newRow(u.key);
					}
	                for (Map.Entry<String,Object> entry: u.rowValue.entrySet()) {
	                    row.put(entry.getKey(), entry.getValue());
	                }		
				}
			} else if (u.type == UpdateType.REMOVE) {
				MemoryTable table = tableSource.getTable(u.table);
				synchronized(table) {
					table.deleteRow(u.key);
				}
			} else if(u.type == UpdateType.CREATE) {
				tableSource.createTable(u.table);
			}
		}
		
		@Override
		public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
				throws Exception {
			log.info("Channel closed (mode "+mode+") to {}", channel.getRemoteAddress());
			channel = null;
			if(mode == Mode.CLIENT) {
				controller.setRole(Role.MASTER);
			}
		}

		@Override
		public void channelConnected(ChannelHandlerContext ctx,
				ChannelStateEvent e) throws Exception {
			channel = e.getChannel();
			log.info("Channel connected (mode "+mode+") to {}", channel.getRemoteAddress());
			if(mode == Mode.CLIENT) {
				controller.setRole(Role.SLAVE);
			}
			send();
		}
	}

	public class SyncPipelineFactory implements ChannelPipelineFactory {
		@Override
		public ChannelPipeline getPipeline() throws Exception {
			ChannelPipeline pipeline = Channels.pipeline();
			pipeline.addLast("object-decode", new ObjectDecoder());
			pipeline.addLast("sync-receive", new SyncReceiveHandler());
			pipeline.addLast("object-encode", new ObjectEncoder());
			return pipeline;
		}

	}

	enum Mode { CLIENT, SERVER };
	private final Mode mode;
	
	volatile Channel channel = null;
	Queue<Update> pendingUpdates = new ConcurrentLinkedQueue<Update>();
	Set<Update> inflightUpdates = Collections.synchronizedSet(new LinkedHashSet<Update>());

	private String host;

	private int port;
	private final TableSource tableSource;

	private final IFloodlightProviderService controller;

	public DistSyncService(String spec, TableSource tableSource, IFloodlightProviderService controller) {
		this.controller = controller;
		log.info("Dist sync service initialize with sync config {}", spec);
		
		this.tableSource = tableSource;
		String[] split = spec.split(":");
		if("tcp".equalsIgnoreCase(split[0])) {
			host = split[1];
			port = Integer.parseInt(split[2]);
			mode = Mode.CLIENT;
		} else if ("ptcp".equalsIgnoreCase(split[0])) {
			mode = Mode.SERVER;
			if(split.length > 2) {
				host = split[1];
				port = Integer.parseInt(split[2]);
			} else {
				host = "0.0.0.0";
				port = Integer.parseInt(split[1]);
			}				
		} else {
			throw new IllegalArgumentException("Unknown schema: "+split[0]);
		}
	}

	private void send() {
		Update u;
		while ((u = pendingUpdates.peek()) != null) {
			sendUpdate(u);
		}
	}

	private void sendUpdate(final Update u) {
		Channel c = channel;
		if (c == null)
			return;
		ChannelFuture write = c.write(u);
		inflightUpdates.add(pendingUpdates.poll());
		write.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future)
					throws Exception {
				if (future.isSuccess()) {
					inflightUpdates.remove(u);
				} else {
					pendingUpdates.add(u);
				}
			}
		});
	}

	public void start() {
		if (mode == Mode.SERVER) {
			// Configure the server.
			ServerBootstrap bootstrap = new ServerBootstrap(
					new NioServerSocketChannelFactory(
							Executors.newCachedThreadPool(),
							Executors.newCachedThreadPool()));

			// Configure the pipeline factory.
			bootstrap.setPipelineFactory(new SyncPipelineFactory());

			// Bind and start to accept incoming connections.
			bootstrap.bind(new InetSocketAddress(host, port));
			log.info("Sync server listening on {} port {}", host, port);
		} else {
			// Configure the server.
			ClientBootstrap bootstrap = new ClientBootstrap(
					new NioClientSocketChannelFactory(
							Executors.newCachedThreadPool(),
							Executors.newCachedThreadPool()));

			// Configure the pipeline factory.
			bootstrap.setPipelineFactory(new SyncPipelineFactory());

			// Bind and start to accept incoming connections.
			ChannelFuture connectFuture = bootstrap.connect(new InetSocketAddress(host, port));
			connectFuture.awaitUninterruptibly();
			
			if(!connectFuture.isSuccess()) {
				throw new RuntimeException("ConnectService failed: ", connectFuture.getCause());
			} else {
				log.info("Sync client connected to server {} port {}", host, port);
			}
		}
	}

	public void createTable(String tableName) {
		sendUpdate(new Update(UpdateType.CREATE, tableName, null, null));
	}

	public void send(Update update) {
		pendingUpdates.add(update);
	}
}