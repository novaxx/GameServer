package com.nova.server.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nova.server.domain.GameRequest;
import com.nova.server.domain.GameResponse;
import com.nova.server.utils.HttpUtils;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * @project: WSK
 * @Title: InitHandler.java
 * @Package: com.nova.server.handler
 * @author: zhangxx
 * @email: 395295759@qq.com
 * @date: 2015年8月20日 下午2:27:11
 * @description:
 * @version:
 */
public class InitHandler implements GameInitHandler {
	protected Logger logger = LoggerFactory.getLogger(getClass());

	public void execute(GameRequest request, GameResponse response) {
		this.logger.error(request.readString());
		response.write("I am ok!");
		
		switch (request.getRequestType()) {
		case HTTP:
			HttpUtils.sendHttpResponse(request.getCtx(), (FullHttpRequest) request.getMsg(),
					response.getResp());
			break;
		case WEBSOCKET_TEXT:
			request.getCtx().channel().write(new TextWebSocketFrame(response.getWebSocketRespone()));
			break;
		case SOCKET:
		case WEBSOCKET_BINARY:
			response.getChannel().writeAndFlush((ByteBuf) response.getRtMessage());
			break;
		}
	}
}
