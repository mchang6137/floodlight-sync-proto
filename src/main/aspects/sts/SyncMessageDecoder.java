package sts;

import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

import sts.sync.message.SyncMessageBuilder;

public class SyncMessageDecoder extends OneToOneDecoder {
    protected static SimpleLogger log = SimpleLogger.getLogger(SyncMessageDecoder.class);

    private ObjectMapper objectMapper;

	public SyncMessageDecoder() {
		objectMapper = new ObjectMapper();
	}
	
	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel,
			Object msg) throws Exception {

		if(log.isDebugEnabled())
			log.debug("SyncMessage decode: "+msg);

		if (!(  msg instanceof String))
            return msg;

        SyncMessageBuilder builder = objectMapper.readValue((String) msg, SyncMessageBuilder.class);    
		if(log.isDebugEnabled())
			log.debug("SyncMessage decode result: "+builder);
		return builder.getMessage();
	}
}
