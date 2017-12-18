package nova.common.game.wsk.room;

import nova.common.game.wsk.GameManager;
import nova.common.game.wsk.data.GameInfo;
import nova.common.game.wsk.data.RoomInfo;
import nova.common.game.wsk.handler.GameHandler;

public class RoomManager {

	private int mRoomId;
	private RoomInfo mRoomInfo;
	private GameInfo mGameInfo;
	private GameHandler mGameHandler;
	private GameManager mGameManager;
    private int mGameStartDelay = 5;
	private boolean mIsRunning = false;
	
	private Runnable mGameRunnable = new Runnable() {
		public void run(){
			mIsRunning = true;
			int count = 0;
			while (count <= mGameStartDelay) {
				count++;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (mRoomInfo.isPlayerFilled()) {
					break;
				}
				
				if (mGameHandler != null) {
					mGameHandler.onRoomInfoChange(mRoomId, mRoomInfo.getPlayers());
				}
			}
			
			if (!mRoomInfo.isPlayerFilled()) {
				mRoomInfo.fillAutoPlayer();
			}
			if (mGameHandler != null) {
				mGameHandler.onRoomInfoChange(mRoomId, mRoomInfo.getPlayers());
			}
			mGameManager.startGame();
			mRoomInfo.setRunning(true);
		}
	};
	
	public RoomManager(int roomId) {
		mRoomId = roomId;
		mRoomInfo = new RoomInfo();
		mGameInfo = new GameInfo();
		mGameManager = new GameManager(roomId);
	}
	
	public void setTestGameDelay(int delay) {
		mGameStartDelay = delay;
	}
	
	public void setRoomInfo(RoomInfo info) {
		mRoomInfo = info;
	}
	
	public RoomInfo getRoomInfo() {
		return mRoomInfo;
	}
	
	public void setGameInfo(GameInfo info) {
		mGameInfo = info;
	}
	
	public GameInfo getGameInfo() {
		return mGameInfo;
	}
	
	public GameManager getGameManager() {
		return mGameManager;
	}
	
	public void setHandler(GameHandler handler) {
		mGameHandler = handler;
		mGameManager.setHandler(handler);
	}
	
	public void startGame() {
		if (!mIsRunning) {
			new Thread(mGameRunnable).start();
		}
	}
	
	public void stopGame() {
		mIsRunning = false;
		mRoomInfo.setRunning(false);
		mRoomInfo.removeAllPlayer();
		mGameManager.stopGame();
	}
	
	public boolean isPlayerFilled() {
		return mRoomInfo.isPlayerFilled();
	}
}
