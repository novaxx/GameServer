package nova.common.game.wsk.util;


public class GameTimer implements Runnable {
	
	private TimerCallback mCallback;
	private boolean mRunning = false;
	
	public GameTimer(TimerCallback callback) {
		mCallback = callback;
	}
	
	@Override
	public void run() {
		while (mRunning) {
			mCallback.handleMessage();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
	}
	
	public boolean isRunning() {
		return mRunning;
	}
	
	public void setHandlerCallback(TimerCallback callback) {
		mCallback = callback;
	}
	
	public void start() {
		mRunning = true;
		new Thread(this).start();
	}
	
	public void stop() {
		mRunning = false;
	}
}
