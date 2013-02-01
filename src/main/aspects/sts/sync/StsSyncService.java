package sts.sync;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;

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
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.util.CharsetUtil;

import sts.SimpleLogger;
import sts.SyncMessageDecoder;
import sts.SyncMessageEncoder;
import sts.sync.message.StateChange;
import sts.sync.message.SyncMessage;

public class StsSyncService {
    protected static SimpleLogger log = SimpleLogger.getLogger(StsSyncService.class);
	
	volatile Channel channel = null;
	Queue<SyncMessage> pendingMessages = new ConcurrentLinkedQueue<SyncMessage>();
	Set<SyncMessage> inflightMessages = Collections.newSetFromMap(new ConcurrentHashMap<SyncMessage, Boolean>());

	private enum Mode { SERVER, CLIENT };
	private String host;
	private Mode mode;
	
	private int port;
	
	public StsSyncService(String spec) {		
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
		log.info("created sync service: "+spec + ", mode="+mode);
	}
	
	class SyncPipelineFactory implements ChannelPipelineFactory {

		@Override
		public ChannelPipeline getPipeline() throws Exception {
			ChannelPipeline pipeline = Channels.pipeline();
			pipeline.addLast("frameDecoder", new DelimiterBasedFrameDecoder(1024, Delimiters.lineDelimiter()));
			pipeline.addLast("stringDecoder", new StringDecoder(CharsetUtil.UTF_8));			 
			pipeline.addLast("sync-decode", new SyncMessageDecoder());
			pipeline.addLast("sync-receive", new SyncReceiveHandler());			
			pipeline.addLast("object-encode", new SyncMessageEncoder());
			return pipeline;
		}
	}
	
	class SyncReceiveHandler extends SimpleChannelUpstreamHandler {
		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
				throws Exception {
			SyncMessage message = (SyncMessage) e.getMessage();
			SyncMessage reply = message.execute();
			if(reply != null)
				enqueue(reply);
		}
		
		@Override
		public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
				throws Exception {
			log.info("Channel closed to {}", channel.getRemoteAddress());
			channel = null;
		}

		@Override
		public void channelConnected(ChannelHandlerContext ctx,
				ChannelStateEvent e) throws Exception {
			channel = e.getChannel();
			log.info("Channel connected  to {}", channel.getRemoteAddress());
			send();
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
				throws Exception {
			log.error("Exception caught in netty pipeline: "+e.getCause().getMessage(), e.getCause());
		}
		
	}
	
	private void send() {
		SyncMessage polledMessage;
		while ((polledMessage = pendingMessages.poll()) != null) {
			final SyncMessage message = polledMessage;
			if(inflightMessages.contains(message)) {
				continue;
			}
			Channel c = channel;
			if (c == null)
				return;
			ChannelFuture write = c.write(message);

			write.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future)
						throws Exception {
					if (future.isSuccess()) {
						pendingMessages.remove(message);
					} 		
					inflightMessages.remove(message);
				}
			});
		}
	}
	
	public void enqueue(SyncMessage m) {
		pendingMessages.add(m);
		send();
	}
	
	public void connect() {
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
	
	public void beforeStatusChange(String state, String value) {
		// TODO Auto-generated method stub		
	}

	public void afterStatusChange(String name, String value) {
		enqueue(new StateChange(name, value));
	}

}
