package sts;

import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import sts.sync.message.SyncMessage;

public class SyncMessageEncoder extends OneToOneEncoder {
	private ObjectMapper objectMapper;

	private byte[] DELIMITER = { '\n' };
	
	public SyncMessageEncoder() {
		objectMapper = new ObjectMapper();
	}
	
	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel,
			Object msg) throws Exception {
        if (!(  msg instanceof SyncMessage))
            return msg;
       
        return ChannelBuffers.wrappedBuffer(objectMapper.writeValueAsBytes(msg), DELIMITER);        
	}

}
