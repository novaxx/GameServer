package com.nova.server.dispatcher;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nova.server.domain.GameRequest;
import com.nova.server.domain.GameResponse;
import com.nova.server.domain.MessageQueue;
import com.nova.server.handler.GameInitHandler;
import com.nova.server.utils.ExceptionUtils;

import io.netty.channel.Channel;

/**
 * @project: WSK
 * @Title: HandlerDispatcher.java
 * @Package: com.nova.server.dispatcher
 * @author: zhangxx
 * @email: 395295759@qq.com
 * @date: 2015年8月20日 下午2:15:04
 * @description:
 * @version:
 */
public class HandlerDispatcher implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(HandlerDispatcher.class);
	private final Map<Integer, MessageQueue> sessionMsgQ;
	private Executor messageExecutor;
	private Map<Integer, GameInitHandler> handlerMap;
	private boolean running;
	private long sleepTime;

	public HandlerDispatcher() {
		this.sessionMsgQ = new ConcurrentHashMap<Integer, MessageQueue>();

		this.running = true;
		this.sleepTime = 200L;
	}

	public void setHandlerMap(Map<Integer, GameInitHandler> handlerMap) {
		this.handlerMap = handlerMap;
	}

	public void setMessageExecutor(Executor messageExecutor) {
		this.messageExecutor = messageExecutor;
	}

	public void setSleepTime(long sleepTime) {
		this.sleepTime = sleepTime;
	}

	public void addMessageQueue(Integer channelId, MessageQueue messageQueue) {
		this.sessionMsgQ.put(channelId, messageQueue);
	}

	public void removeMessageQueue(Channel channel) {
		MessageQueue queue = (MessageQueue) this.sessionMsgQ.remove(channel);
		if (queue != null)
			queue.clear();
	}

	public void addMessage(GameRequest request) {
		try {
			MessageQueue messageQueue = (MessageQueue) this.sessionMsgQ
					.get(Integer.valueOf(request.getChannel().hashCode()));

			if (messageQueue == null) {
				messageQueue = new MessageQueue(new ConcurrentLinkedQueue<GameRequest>());

				this.sessionMsgQ.put(Integer.valueOf(request.getChannel().hashCode()), messageQueue);
				messageQueue.add(request);
			} else {
				messageQueue.add(request);
			}
		} catch (Exception e) {
			HandlerDispatcher.logger.error(ExceptionUtils.getStackTrace(e));
		}
	}

	public void run() {
		while (this.running) {
			try {
				for (MessageQueue messageQueue : sessionMsgQ.values())
					if ((messageQueue != null) && (messageQueue.size() > 0) && (!messageQueue.isRunning())) {
						MessageWorker messageWorker = new MessageWorker(messageQueue);

						this.messageExecutor.execute(messageWorker);
					}
			} catch (Exception e) {
				HandlerDispatcher.logger.error(ExceptionUtils.getStackTrace(e));
			}
			try {
				Thread.sleep(this.sleepTime);
			} catch (InterruptedException e) {
				HandlerDispatcher.logger.error(ExceptionUtils.getStackTrace(e));
			}
		}
	}

	public void stop() {
		this.running = false;
	}

	public MessageQueue getUserMessageQueue(Channel channel) {
		return (MessageQueue) this.sessionMsgQ.get(channel);
	}

	private final class MessageWorker implements Runnable {
		private final MessageQueue messageQueue;
		private GameRequest request;

		private MessageWorker(MessageQueue messageQueue) {
			messageQueue.setRunning(true);
			this.messageQueue = messageQueue;
			this.request = ((GameRequest) messageQueue.getRequestQueue().poll());
		}

		public void run() {
			try {
				handMessageQueue();
			} catch (Exception e) {
				HandlerDispatcher.logger.error(ExceptionUtils.getStackTrace(e));
			} finally {
				this.messageQueue.setRunning(false);
			}
		}

		private void handMessageQueue() {
			int commandId = this.request.getCommandId();
			GameResponse response = new GameResponse(this.request.getChannel(), this.request.getCommand(),
					this.request.getRequestType());
			GameInitHandler handler = (GameInitHandler) HandlerDispatcher.this.handlerMap.get(Integer.valueOf(commandId));
			if (handler != null)
				handler.execute(this.request, response);
			else {
				HandlerDispatcher.logger.warn("指令 [{}]找不到", commandId);
			}

			/**
			switch (request.getRequestType()) {
			case HTTP:
				HttpUtils.sendHttpResponse(this.request.getCtx(), (FullHttpRequest) this.request.getMsg(),
						response.getResp());
				break;
			case WEBSOCKET_TEXT:
				this.request.getCtx().channel().write(new TextWebSocketFrame(response.getWebSocketRespone()));
				break;
			case SOCKET:
			case WEBSOCKET_BINARY:
				response.getChannel().writeAndFlush((ByteBuf) response.getRtMessage());
				break;
			}
			**/
		}
	}
}
