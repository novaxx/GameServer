package nova.common.game.wsk.handler;

public class FileLogRecorderManager {

	private static FileLogRecorderManager mInstance;
	private FileLogRecorderRunnable mRunnable;
	
	
	private FileLogRecorderManager() {
		mRunnable = new FileLogRecorderRunnable();
	}
	
	public static FileLogRecorderManager getInstance() {
		if (mInstance == null) {
			mInstance = new FileLogRecorderManager();
		}
		
		return mInstance;
	}
	
	public void startRecord() {
		new Thread(mRunnable).start();
	}
	
	public void addMessage(int roomId, String time, String message) {
		mRunnable.addMessage(roomId, time, message);
	}
}
