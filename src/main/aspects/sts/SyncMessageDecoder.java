package sts;

import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sts.sync.StsSyncModule;
import sts.sync.message.SyncMessage;
import sts.sync.message.SyncMessageBuilder;

public class SyncMessageDecoder extends OneToOneDecoder {
    protected static Logger log = LoggerFactory.getLogger(SyncMessageDecoder.class);

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
