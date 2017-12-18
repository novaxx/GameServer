package nova.common.game.wsk.handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MessageQueue {
	private Queue<MessageRequest> mMessageQueue;
	private boolean mIsRunning = false;
	private PrintStream mPrintStream;

	public MessageQueue(ConcurrentLinkedQueue<MessageRequest> concurrentLinkedQueue) {
		this.mMessageQueue = concurrentLinkedQueue;
	}

	public Queue<MessageRequest> getRequestQueue() {
		return this.mMessageQueue;
	}

	public void setRequestQueue(Queue<MessageRequest> requestQueue) {
		this.mMessageQueue = requestQueue;
	}

	public void clear() {
		this.mMessageQueue.clear();
		this.mMessageQueue = null;
	}

	public int size() {
		return this.mMessageQueue != null ? this.mMessageQueue.size() : 0;
	}

	public boolean add(MessageRequest message) {
		if (mPrintStream == null) {
			try {
				mPrintStream = new PrintStream(new FileOutputStream(new File("/sdcard/wsk/" + message.getRoom() + "_" + message.getTime() + ".txt")));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return this.mMessageQueue.add(message);
	}

	public void setRunning(boolean running) {
		this.mIsRunning = running;
	}

	public boolean isRunning() {
		return this.mIsRunning;
	}
	
	public PrintStream getPrint() {
		return mPrintStream;
	}
}
