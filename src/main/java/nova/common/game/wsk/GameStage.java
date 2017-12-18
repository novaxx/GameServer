package nova.common.game.wsk;

import nova.common.game.wsk.data.RoomInfo;
import nova.common.game.wsk.room.RoomController;
import nova.common.game.wsk.util.GameTimer;
import nova.common.game.wsk.util.TimerCallback;



public class GameStage {
	public interface StageCallBack {
		public void onStageChange(int stage);
		public void onSecStageChange(int stage);
		public void onStageEnd(int stage);
	}
	
	public static final int SEND_CARD_WAIT = 0;
	public static final int SEND_CARD_END = 1;
	public static final int CALL_TRUMP_WAIT = 2;
	public static final int BACK_TRUMP_WAIT = 3;
	public static final int PAY_TRIBUTE_WAIT = 4;
	public static final int PAY_TRIBUTE_END = 5;
	public static final int PAY_TRIBUTE_SEC_WAIT = 6;
	public static final int PAY_TRIBUTE_SEC_END = 7;
	public static final int BACK_TRIBUTE_WAIT = 8;
	public static final int BACK_TRIBUTE_END = 9;
	public static final int BACK_TRIBUTE_SEC_WAIT =10;
	public static final int BACK_TRIBUTE_SEC_END = 11;
	public static final int OUT_LAST_CARD_WAIT = 12;
	public static final int OUT_LAST_CARD_END = 13;
	public static final int SEND_LAST_CARD_WAIT = 14;
	public static final int SEND_LAST_CARD_END = 15;
	public static final int OUT_CARD_WAIT = 16;
	public static final int ROUND_CALLTRUMP_END = 17;
	public static final int ROUND_BACKTRUMP_END = 18;
	public static final int ROUND_OUTCARD_END = 19;
	public static final int GAME_END = 20;
	
	private GameTimer mTimer;
	private StageCallBack mStageHandler;
	private boolean mIsTributeEnd = false;
	private boolean mIsSecTributeEnd = false;
	
	// 倒计时
	private int mDuration = 0;
	private int mSecDuration = 0;

	private int mGameStage = 0;
	private int mSecGameStage = -1;
	
	private int mRoomId;
	
	private TimerCallback mCallback = new TimerCallback() {
		
		@Override
		public void handleMessage() {
			handleSecMessage();
			
			int timeOut = getTimeOutForStage(mGameStage);
			if (mDuration < timeOut) {
				mDuration++;
				return;
			}
			
			switch(mGameStage) {
			case SEND_CARD_WAIT:
				mStageHandler.onStageEnd(mGameStage);
				setStage(SEND_CARD_END);
				break;
			case SEND_CARD_END:
				setStage(CALL_TRUMP_WAIT);
				break;
			case CALL_TRUMP_WAIT:
				mStageHandler.onStageEnd(mGameStage);
				break;
			case BACK_TRUMP_WAIT:
				mStageHandler.onStageEnd(mGameStage);
				break;
			case PAY_TRIBUTE_WAIT:
				setStage(PAY_TRIBUTE_END);
				mStageHandler.onStageEnd(mGameStage);
				break;
			case PAY_TRIBUTE_END:
				setStage(BACK_TRIBUTE_WAIT);
				break;
			case BACK_TRIBUTE_WAIT:
				setStage(BACK_TRIBUTE_END);
				mStageHandler.onStageEnd(mGameStage);
				mIsTributeEnd = true;
				break;
			case BACK_TRIBUTE_END:
				if (mIsTributeEnd && mIsSecTributeEnd) {
					setSecStage(-1);
					mIsTributeEnd = false;
					mIsSecTributeEnd = false;
				}
				break;
			case OUT_LAST_CARD_WAIT:
				setStage(OUT_LAST_CARD_END);
				mStageHandler.onStageEnd(mGameStage);
				break;
			case OUT_LAST_CARD_END:
				setStage(SEND_LAST_CARD_WAIT);
				break;
			case SEND_LAST_CARD_WAIT:
				setStage(SEND_LAST_CARD_END);
				break;
			case SEND_LAST_CARD_END:
				mStageHandler.onStageEnd(mGameStage);
				setStage(OUT_CARD_WAIT);
				break;
			case OUT_CARD_WAIT:
				mStageHandler.onStageEnd(mGameStage);
				break;
			case ROUND_CALLTRUMP_END:
				mStageHandler.onStageEnd(mGameStage);
				break;
			case ROUND_BACKTRUMP_END:
				mStageHandler.onStageEnd(mGameStage);
				break;
			case ROUND_OUTCARD_END:
				mStageHandler.onStageEnd(mGameStage);
				setStage(OUT_CARD_WAIT);
				break;
			case GAME_END:
				mStageHandler.onStageEnd(mGameStage);
				setStage(SEND_CARD_WAIT);
				break;
			default:
				break;
			}
			
			cleanStageTime();
		}
	};
	
    public GameStage(int roomId) {
    	mRoomId = roomId;
		mTimer = new GameTimer(mCallback);
	}
    
    public void setStageHandler(StageCallBack handler) {
    	mStageHandler = handler;
    }
    
    public void start() {
    	if (!mTimer.isRunning()) {
    		cleanStageTime();
    		mTimer.start();
    	}
    }
    
    public void restart() {
    	if (mTimer.isRunning()) {
    		mTimer.stop();
    	}
    	cleanStageTime();
    	mTimer = new GameTimer(mCallback);
    	mTimer.start();
    }
    
    public void stop() {
    	if (mTimer.isRunning()) {
    		mTimer.stop();
    	}
    	cleanStageTime();
    }
    
    public int getStageTime() {
    	return mDuration;
    }
    
    public int getSecStageTime() {
    	return mSecDuration;
    }
    
    public void cleanStageTime() {
    	mDuration = 0;
    }
    
    public void cleanSecStageTime() {
    	mSecDuration = 0;
    }
    
    public int getStage() {
    	return mGameStage;
    }
    
    public int getSecStage() {
    	return mSecGameStage;
    }
    
    public void setStage(int stage) {
    	mGameStage = stage;
    	mStageHandler.onStageChange(mGameStage);
    }
    
    public void setSecStage(int stage) {
    	mSecGameStage = stage;
    	mStageHandler.onSecStageChange(mSecGameStage);
    }
    
    public void setStageAfterCallTrump(boolean isTrump, int tributeCount) {
    	if (!isTrump) {
			setStage(SEND_LAST_CARD_WAIT);
		} else if (tributeCount > 0) {
			setStage(PAY_TRIBUTE_WAIT);
			if (tributeCount > 1) {
				setSecStage(PAY_TRIBUTE_SEC_WAIT);
			} else {
				setSecStage(-1);
			}
		} else {
			setStage(OUT_LAST_CARD_WAIT);
		}
    }
    
    private void handleSecMessage() {
    	if (mSecGameStage == -1) {
    		return;
    	}
    	
    	int timeOut = getTimeOutForStage(mSecGameStage);
		if (mSecDuration < timeOut) {
			mSecDuration++;
			return;
		}
		
		switch(mSecGameStage) {
		case PAY_TRIBUTE_SEC_WAIT:
			setSecStage(PAY_TRIBUTE_SEC_END);
			mStageHandler.onStageEnd(mSecGameStage);
			break;
		case PAY_TRIBUTE_SEC_END:
			setSecStage(BACK_TRIBUTE_SEC_WAIT);
			break;
		case BACK_TRIBUTE_SEC_WAIT:
			setSecStage(BACK_TRIBUTE_SEC_END);
			mStageHandler.onStageEnd(mSecGameStage);
			mIsSecTributeEnd = true;
			break;
		case BACK_TRIBUTE_SEC_END:
			if (mIsTributeEnd && mIsSecTributeEnd) {
				setSecStage(-1);
				mIsTributeEnd = false;
				mIsSecTributeEnd = false;
			}
			break;
		default:
			break;
		}
		
		cleanSecStageTime();
    }
    
	private int getTimeOutForStage(int stage) {
		int timeOut = 9;
		switch(stage) {
		case SEND_CARD_WAIT:
			timeOut = 3;
			break;
		case SEND_LAST_CARD_WAIT:
			timeOut = 3;
			break;
		case CALL_TRUMP_WAIT:
		case BACK_TRUMP_WAIT:
		case OUT_LAST_CARD_WAIT:
		case OUT_CARD_WAIT:
			GameManager manager = RoomController.getInstance().getRoomManager(mRoomId).getGameManager();
			RoomInfo roomInfo = RoomController.getInstance().getRoomManager(mRoomId).getRoomInfo();
			int current = manager.getCurrentPlayer();
			if (roomInfo.isNormalPlayer(current)) {
			    timeOut = 9;
			 } else {
				timeOut = 3;
			 }
			break;
		case PAY_TRIBUTE_WAIT:
		case PAY_TRIBUTE_SEC_WAIT:
		case BACK_TRIBUTE_WAIT:
		case BACK_TRIBUTE_SEC_WAIT:
			timeOut = 5;
			break;
		case SEND_CARD_END:
		case OUT_LAST_CARD_END:
		case SEND_LAST_CARD_END:
		case ROUND_CALLTRUMP_END:
		case ROUND_BACKTRUMP_END:
		case ROUND_OUTCARD_END:
		case PAY_TRIBUTE_END:
		case PAY_TRIBUTE_SEC_END:
		case BACK_TRIBUTE_END:
		case BACK_TRIBUTE_SEC_END:
			timeOut = 1;
			break;
		case GAME_END:
			timeOut = 3;
			break;
		default:
			break;
		}
		
		return timeOut;
	}
}
