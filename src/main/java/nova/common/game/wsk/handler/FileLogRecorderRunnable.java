package nova.common.game.wsk.handler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

public class FileLogRecorderRunnable implements Runnable {
	private GameLogger mLogger = GameLogger.getInstance();
	private static final String TAG = "FileLogRecorderRunnable";
	private final Map<Integer, MessageQueue> mSessionMsgQ;
	private Executor messageExecutor;
	private boolean mIsRunning;
	private long mSleepTime;

	public FileLogRecorderRunnable() {
		this.mSessionMsgQ = new ConcurrentHashMap<Integer, MessageQueue>();
        this.messageExecutor = new Executor() {
			
			@Override
			public void execute(Runnable command) {
				command.run();
			}
		};
		this.mIsRunning = true;
		this.mSleepTime = 200L;
	}

	public void setMessageExecutor(Executor messageExecutor) {
		this.messageExecutor = messageExecutor;
	}

	public void setSleepTime(long sleepTime) {
		this.mSleepTime = sleepTime;
	}

	public void addMessageQueue(Integer roomId, MessageQueue messageQueue) {
		this.mSessionMsgQ.put(roomId, messageQueue);
	}

	public void removeMessageQueue(Integer roomId) {
		MessageQueue queue = (MessageQueue) this.mSessionMsgQ.remove(roomId);
		if (queue != null)
			queue.clear();
	}

	public void addMessage(Integer roomId, String time, String message) {
		try {
			MessageQueue messageQueue = (MessageQueue) this.mSessionMsgQ.get(roomId);
			if (messageQueue == null) {
				messageQueue = new MessageQueue(new ConcurrentLinkedQueue<MessageRequest>());
				this.mSessionMsgQ.put(roomId, messageQueue);
			}
			
			messageQueue.add(new MessageRequest(roomId, time, message));
		} catch (Exception e) {
			mLogger.e(TAG, e.toString());
		}
	}

	public void run() {
		while (this.mIsRunning) {
			try {
				for (MessageQueue messageQueue : mSessionMsgQ.values()) {
					if ((messageQueue != null) && (messageQueue.size() > 0) && (!messageQueue.isRunning())) {
						MessageWorker messageWorker = new MessageWorker(messageQueue);
						
						this.messageExecutor.execute(messageWorker);
					}
				}
			} catch (Exception e) {
				mLogger.e(TAG, e.toString());
			}
			try {
				Thread.sleep(this.mSleepTime);
			} catch (InterruptedException e) {
				mLogger.e(TAG, e.toString());
			}
		}
	}

	public void stop() {
		this.mIsRunning = false;
	}

	public MessageQueue getUserMessageQueue(Integer roomId) {
		return (MessageQueue) this.mSessionMsgQ.get(roomId);
	}

	private final class MessageWorker implements Runnable {
		private final MessageQueue messageQueue;
		private MessageRequest message;

		private MessageWorker(MessageQueue messageQueue) {
			messageQueue.setRunning(true);
			this.messageQueue = messageQueue;
			this.message = ((MessageRequest) messageQueue.getRequestQueue().poll());
		}

		public void run() {
			try {
				handMessageQueue();
			} catch (Exception e) {
				mLogger.e(TAG, e.toString());
			} finally {
				this.messageQueue.setRunning(false);
			}
		}

		private void handMessageQueue() {
			String body = this.message.getBody();
			messageQueue.getPrint().append(body);
			messageQueue.getPrint().append("\n");
		}
	}
}
