package nova.common.game.wsk.handler;

public class GameLogger {
	
	public interface LoggerHandler {
		public void d(String tag, String msg);
		public void e(String tag, String msg);
	}
	
	private static GameLogger mLogger;
	private static LoggerHandler mHandler;
	
	private GameLogger() {
	}
	
	public static void create(LoggerHandler handler) {
		if (mLogger == null) {
			mLogger = new GameLogger();
			mHandler = handler;
		}
	}
	
	public static GameLogger getInstance() {
		if (mLogger == null) {
			create(null);
		}
		return mLogger;
	}
	
    public void d(String tag, String msg) {
    	if (mHandler != null) {
    		mHandler.d(tag, msg);
    	}
    }
    
    public void e(String tag, String msg) {
    	if (mHandler != null) {
    		mHandler.e(tag, msg);
    	}
    }
}
