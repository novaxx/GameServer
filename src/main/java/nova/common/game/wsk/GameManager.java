package nova.common.game.wsk;

import java.util.ArrayList;
import java.util.HashMap;
import nova.common.game.wsk.GameStage.StageCallBack;
import nova.common.game.wsk.data.CardData;
import nova.common.game.wsk.data.CardInfo;
import nova.common.game.wsk.data.GameInfo;
import nova.common.game.wsk.data.GameRoundInfo;
import nova.common.game.wsk.data.OutCardData;
import nova.common.game.wsk.data.TrumpData;
import nova.common.game.wsk.handler.GameDispatcher;
import nova.common.game.wsk.handler.GameHandler;
import nova.common.game.wsk.handler.GameLogger;
import nova.common.game.wsk.room.RoomController;
import nova.common.game.wsk.util.CardConstant;
import nova.common.game.wsk.util.CardUtil;

public class GameManager implements StageCallBack, GameDispatcher {
	
	private class BackTrumpData {
		private int mPlayerId;
		private ArrayList<CardData> mBackTrumpData;
		
		public BackTrumpData(int playerId, ArrayList<CardData> trump) {
			mPlayerId = playerId;
			mBackTrumpData = trump;
		}
		
		public int getPlayerId() {
			return mPlayerId;
		}
		
		public ArrayList<CardData> getBackTrumpData() {
			return mBackTrumpData;
		}
	}
	
	private static final String TAG = "GameManager";
	private static final int TRUMP_INIT = 0;
	private static final int TRUMP_CALL = 1;
	private static final int TRUMP_NOT = 2;
	
	private CardManager mCardManager;
	private GameStage mStage;
	
	private HashMap<Integer, ArrayList<CardData>> mOutCardDatas = new HashMap<Integer, ArrayList<CardData>>();
	private HashMap<Integer, Integer> mTrumpFlags = new HashMap<Integer, Integer>();
	private ArrayList<BackTrumpData> mBackTrumpList = new ArrayList<GameManager.BackTrumpData>();
	private ArrayList<CardData> mOutLastCardDatas = new ArrayList<CardData>();
	private OutCardData mLargeOutCardData;
	private TrumpData mLargeTrumpData;
	private GameLogger mLogger = GameLogger.getInstance();
	private GameHandler mHandler;
	private GameInfo mGameInfo;
	private GameRoundInfo mGameRoundInfo;
	private int mRoomId;
	
	public GameManager(int roomId) {
		mRoomId = roomId;
		mGameRoundInfo = new GameRoundInfo();
		mCardManager = new CardManager();
		mStage = new GameStage(mRoomId);
		mStage.setStageHandler(this);
	}
	
	public void setLogger(GameLogger logger) {
		mLogger = logger;
	}
	
	public void setHandler(GameHandler handler) {
		mHandler = handler;
	}
	
	@Override
	public void onStageEnd(int stage) {
		switch(stage) {
		case GameStage.SEND_CARD_WAIT:
			break;
		case GameStage.CALL_TRUMP_WAIT:
			autoTrumpCard();
			break;
		case GameStage.OUT_CARD_WAIT:
			autoOutCard();
			break;
		case GameStage.BACK_TRUMP_WAIT:
			autoBackTrumpCard();
			break;
		case GameStage.PAY_TRIBUTE_END:
			autoPayTributeCard(mGameRoundInfo.getCurrent());
			break;
		case GameStage.PAY_TRIBUTE_SEC_END:
			autoPayTributeCard(mGameRoundInfo.getSecurrent());
			break;
		case GameStage.BACK_TRIBUTE_END:
			autoBackTributeCard(mGameRoundInfo.getCurrent());
			break;
		case GameStage.BACK_TRIBUTE_SEC_END:
			autoBackTributeCard(mGameRoundInfo.getSecurrent());
			break;
		case GameStage.ROUND_CALLTRUMP_END:
			if (mBackTrumpList.size() > 1) {
				mStage.setStage(GameStage.BACK_TRUMP_WAIT);
				mBackTrumpList.remove(mBackTrumpList.size() - 1);
			} else {
				mStage.setStageAfterCallTrump(mGameInfo.isTrumped(), mGameInfo.getTributeCount());
			}
			break;
		case GameStage.ROUND_BACKTRUMP_END:
			clearOutCardDatas();
			mStage.setStageAfterCallTrump(true, mGameInfo.getTributeCount());
			break;
		case GameStage.ROUND_OUTCARD_END:
			clearOutCardDatas();
			break;
		case GameStage.OUT_LAST_CARD_END:
			autoOutLastCard();
			break;
		case GameStage.SEND_LAST_CARD_END:
			if (mGameInfo.isTrumped()) {
				mCardManager.sendLastCardDatas(mGameInfo.getBankerId(), true);
			} else {
				mCardManager.sendLastCardDatas(0, false);
			}
			break;
		case GameStage.GAME_END:
			handleGameResult();
			resumeGame();
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onStageChange(int stage) {
		mGameRoundInfo.setStage(stage);
		mGameRoundInfo.setInfo(getCardInfos(mOutCardDatas));
		if (mHandler != null) {
			mHandler.onGameInfoChange(mRoomId, mGameInfo, mGameRoundInfo, mCardManager.getPlayerDatas(), 
					mOutCardDatas, mCardManager.getLastCardDatas(), mOutLastCardDatas);
		}
	}
	
	@Override
	public void onSecStageChange(int secStage) {
		mGameRoundInfo.setSecStage(secStage);
		mGameRoundInfo.setInfo(getCardInfos(mOutCardDatas));
		if (mHandler != null) {
			mHandler.onGameInfoChange(mRoomId, mGameInfo, mGameRoundInfo, mCardManager.getPlayerDatas(), 
					mOutCardDatas, mCardManager.getLastCardDatas(), mOutLastCardDatas);
		}
	}
	
	public int getCurrentPlayer() {
		return mGameRoundInfo.getCurrent();
	}
	
	public int getCurrentSecPlayer() {
		return mGameRoundInfo.getSecurrent();
	}
	
	private ArrayList<CardInfo> getCardInfos(HashMap<Integer, ArrayList<CardData>> datas) {
		ArrayList<CardInfo> group = new ArrayList<CardInfo>();
		for (int i = 0; i < 4; i++) {
			ArrayList<CardData> data = datas.get(i);
			if (data != null) {
				CardInfo out = new CardInfo();
				out.setId(i);
				out.setData(data);
				group.add(out);
			}
		}
		
		return group;
	}
	
	private boolean isFriendLarge(int playerId) {
		return Math.abs(mGameRoundInfo.getLarger() - playerId) % 2 == 0;
	}
	
	private int getRoundScore() {
		int score = 0;
		for (int i = 0; i < 4; i++) {
			ArrayList<CardData> datas = mOutCardDatas.get(i);
			for (CardData data : datas) {
				score += data.getScore();
			}
		}
		return score;
	}
	
	private int getOutLastCardScore() {
		int score = 0;
		for (CardData data : mOutLastCardDatas) {
			score += data.getScore();
		}
		
		return score;
	}
	
	private boolean isOutCardStage() {
		return mStage.getStage() == GameStage.OUT_CARD_WAIT;
	}
	
	private boolean isCallTrumpStage() {
		return mStage.getStage() == GameStage.CALL_TRUMP_WAIT;
	}
	
	private boolean isBackTrumpStage() {
		return mStage.getStage() == GameStage.BACK_TRUMP_WAIT;
	}
	
	public void startGame() {
		mGameInfo = RoomController.getInstance().getRoomManager(mRoomId).getGameInfo();
		initGameData();
		mCardManager.initCardDatas(mGameInfo.getTrumpFace(), mGameInfo.getTrumpColor());
		if (mHandler != null) {
			mHandler.onGameInfoChange(mRoomId, mGameInfo, mGameRoundInfo, mCardManager.getPlayerDatas(), 
					mOutCardDatas, mCardManager.getLastCardDatas(), mOutLastCardDatas);
		}
		mStage.start();
	};
	
	private void resumeGame() {
		initGameData();
		mCardManager.initCardDatas(mGameInfo.getTrumpFace(), mGameInfo.getTrumpColor());
		if (mHandler != null) {
			mHandler.onGameInfoChange(mRoomId, mGameInfo, mGameRoundInfo, mCardManager.getPlayerDatas(), 
					mOutCardDatas, mCardManager.getLastCardDatas(), mOutLastCardDatas);
		}
		mStage.cleanStageTime();
	};
	
	public void stopGame() {
		mStage.stop();
	}
	
	@Override
	public void notOutCard(int playerId) {
		handleNextResult(playerId);
	}
	
	@Override
	public int activeOutLastCard(int playerId, ArrayList<CardData> datas) {
		if (datas.size() == 8) {
			mCardManager.removeOutCardsFromPlayerData(playerId, datas);
			GiveOutLastCard(playerId, datas);
		}
		
		return 0;
	}
	
	@Override
	public int activeOutCard(int playerId, ArrayList<CardData> datas) {
		boolean isRight;
		
		if (isCallTrumpStage()) {
			isRight = isTrumpRight(playerId, datas);
		} else if (mStage.getStage() == GameStage.PAY_TRIBUTE_WAIT 
				|| mStage.getSecStage() == GameStage.PAY_TRIBUTE_SEC_WAIT 
				|| mStage.getStage() == GameStage.BACK_TRIBUTE_WAIT
				|| mStage.getSecStage() == GameStage.BACK_TRIBUTE_SEC_WAIT) {
			isRight = (datas.size() == getTributeCountByPlayerId(playerId));
			updateTributeStage(playerId);
		} else {
			isRight = isOutCardRight(playerId, datas);
		}
		
		if (isRight) {
			mCardManager.removeCardDatas(playerId, datas);
			mCardManager.removeOutCardsFromPlayerData(playerId, datas);
			setOutCard(playerId, datas);
			mStage.cleanStageTime();
		}
		
		return 0;
	}
	
	private void updateTributeStage(int playerId) {
		if (mGameRoundInfo.getCurrent() == playerId) {
			if (mStage.getStage() == GameStage.PAY_TRIBUTE_WAIT) {
				mStage.setStage(GameStage.PAY_TRIBUTE_END);	
			} else if (mStage.getStage() == GameStage.BACK_TRIBUTE_WAIT) {
				mStage.setStage(GameStage.BACK_TRIBUTE_END);
			} else {
				mLogger.e(TAG, "updateTributeStage, current player stage error! stage is " + mStage.getStage());
			}
		} else if (mGameRoundInfo.getSecurrent() == playerId) {
			if (mStage.getSecStage() == GameStage.PAY_TRIBUTE_SEC_WAIT) {
				mStage.setSecStage(GameStage.PAY_TRIBUTE_SEC_END);
			} else if (mStage.getSecStage() == GameStage.BACK_TRIBUTE_SEC_WAIT) {
				mStage.setSecStage(GameStage.BACK_TRIBUTE_SEC_END);
			} else {
				mLogger.e(TAG, "updateTributeStage, securrent player stage error! secstage is " + mStage.getSecStage());
			}
		} else {
			mLogger.e(TAG, "updateTributeStage, player " + playerId + " error! current:" + mGameRoundInfo.getCurrent() + ", securrent:" + mGameRoundInfo.getSecurrent());
		}
	}
	
	private boolean mIsTributeEnd = false;
	private boolean mIsSecTributeEnd = false;
	
	private void setOutCard(int playerId, ArrayList<CardData> datas) {
		mOutCardDatas.put(playerId, datas);
		
		if (isCallTrumpStage()) {
			updateTrumpData(playerId, datas);
		} else if (isOutCardStage()) {
			updateOutCardData(playerId, datas);
		}
		
		if (mStage.getStage() == GameStage.PAY_TRIBUTE_END 
				|| mStage.getStage() == GameStage.BACK_TRIBUTE_END
				|| mStage.getSecStage() == GameStage.PAY_TRIBUTE_SEC_END 
				|| mStage.getSecStage() == GameStage.BACK_TRIBUTE_SEC_END) {
			if (mGameRoundInfo.getCurrent() == playerId) {
				handleTributeNextResult(mStage.getStage(), playerId, datas);
			} else if (mGameRoundInfo.getSecurrent() == playerId) {
				handleTributeNextResult(mStage.getSecStage(), playerId, datas);
			} else {
				mLogger.e(TAG, "setOutCard, player " + playerId + " error! current:" + mGameRoundInfo.getCurrent() + ", securrent:" + mGameRoundInfo.getSecurrent());
			}
		} else {
			mCardManager.removeCardDatas(playerId, datas);
			handleNextResult(playerId);
		}
	}
	
	private void setBackTrumpCard(int playerId, ArrayList<CardData> datas) {
		mCardManager.removeCardDatas(playerId, datas);
		mOutCardDatas.put(playerId, datas);
		mCardManager.obtainTributeCardDatas(playerId, mOutCardDatas.get(mBackTrumpList.get(mBackTrumpList.size() - 1).getPlayerId()));
		mCardManager.obtainTributeCardDatas(mBackTrumpList.get(mBackTrumpList.size() - 1).getPlayerId(), datas);
		handleNextResult(playerId);
	}
	
	private void GiveOutLastCard(int playerId, ArrayList<CardData> datas) {
		mCardManager.removeCardDatas(playerId, datas);
		
		mOutLastCardDatas.addAll(datas);
		
		mStage.setStage(GameStage.OUT_LAST_CARD_END);
		mStage.cleanStageTime();
	}
	
	private void initGameData() {
		initTrumpFlags();
		mGameInfo.setTrumpColor(-1);
		mGameInfo.setScore(0);
		mStage.cleanStageTime();
		mStage.setStage(0);
		mGameRoundInfo.setSecurrent(-1);
		mGameRoundInfo.setFirst(mGameInfo.getBankerId());
		mGameRoundInfo.setCurrent(mGameInfo.getBankerId());
		mGameRoundInfo.setLarger(mGameInfo.getBankerId());
		
		mOutLastCardDatas.removeAll(mOutLastCardDatas);
		clearOutCardDatas();
		mBackTrumpList.clear();
		mLargeOutCardData = null;
		mLargeTrumpData = new TrumpData(mGameInfo.getTrumpFace());
	}
	
	private void initTrumpFlags() {
		mTrumpFlags.clear();
		for (int i = 0; i < 4; i++) {
			mTrumpFlags.put(i, TRUMP_INIT);
		}
	}
	
	private void autoTrumpCard() {
		setOutCard(mGameRoundInfo.getCurrent(), getTrumpCard(mGameRoundInfo.getCurrent()));
	}
	
	private ArrayList<CardData> getTrumpCard(int playerId) {
		return mCardManager.getPlayerDatas().get(playerId).getTrumpCards(mLargeTrumpData);
	}
	
	private void autoOutCard() {
		setOutCard(mGameRoundInfo.getCurrent(), getOutCard(mGameRoundInfo.getCurrent()));
	}
	
	private ArrayList<CardData> getOutCard(int playerId) {
		if (playerId == mGameRoundInfo.getFirst()) {
			return mCardManager.getPlayerDatas().get(playerId).getFirstOutCardData();
		} else {
			if (mLargeOutCardData == null || mLargeOutCardData.getFirstCard() == null) {
				if (mLargeOutCardData != null) {
					mLogger.e(TAG, "large=true,first=" + (mLargeOutCardData.getFirstCard() != null)
							+ ",current=" + playerId + ",first=" + mGameRoundInfo.getFirst());
				} else {
					mLogger.e(TAG, "large=false,first=false,current=" + playerId + ",first=" + mGameRoundInfo.getFirst());
				}
			}
			return mCardManager.getPlayerDatas().get(playerId).getOutCardDatas(mLargeOutCardData, isFriendLarge(playerId));
		}
	}
	
	private void autoOutLastCard() {
		GiveOutLastCard(mGameInfo.getBankerId(), mCardManager.getOutLastCardDatas(mGameInfo.getBankerId()));
	}
	
	private void autoBackTrumpCard() {
		setBackTrumpCard(mGameRoundInfo.getCurrent(), getBackTrumpCard(mGameRoundInfo.getCurrent(), 
				mBackTrumpList.get(mBackTrumpList.size() - 1).getBackTrumpData()));
	}
	
	private ArrayList<CardData> getBackTrumpCard(int playerId, ArrayList<CardData> datas) {
		TrumpData previousTrumpData = new TrumpData(mGameInfo.getTrumpFace());
		previousTrumpData.updateData(datas);
		return mCardManager.getPlayerDatas().get(playerId).getBackTrumpCards(previousTrumpData, mGameInfo.getTrumpColor());
	}
	
	private void autoPayTributeCard(int playerId) {
		setOutCard(playerId, getPayTributeCard(playerId));
	}
	
	private ArrayList<CardData> getPayTributeCard(int playerId) {
		return mCardManager.getPlayerDatas().get(playerId).getPayTributeCards(getTributeCountByPlayerId(playerId));
	}
	
	private void autoBackTributeCard(int playerId) {
		setOutCard(playerId, getBackTributeCard(playerId));
	}
	
	private ArrayList<CardData> getBackTributeCard(int playerId) {
		return mCardManager.getPlayerDatas().get(playerId).getBackTributeCards(getTributeCountByPlayerId(playerId));
	}
	
	private int getTributeCountByPlayerId(int playerId) {
		int nextPlayerId = (playerId >= 3 ? 0 : playerId + 1);
		if (mGameInfo.getBankerId() == playerId || mGameInfo.getBankerId() == nextPlayerId) {
			return mGameInfo.getTributeCount() / 2 + mGameInfo.getTributeCount() % 2;
		} else {
			return mGameInfo.getTributeCount() / 2;
		}
	}
	
	private void clearOutCardDatas() {
		mOutCardDatas.clear();
	}
	
	private boolean isOutCardRight(int playerId, ArrayList<CardData> datas) {
		if (playerId == mGameRoundInfo.getFirst()) {
			boolean isSameCard = false;
			int index = -1;
			for (int i = 0; i < datas.size(); i++) {
				CardData card =datas.get(i);
				if (index == -1) {
					index = card.getIndex();
				}
				
				if (index != card.getIndex()) {
					isSameCard = false;
					break;
				}
				
				isSameCard = true;
			}
			
			return isSameCard;
		} else {
			if (datas.size() != mLargeOutCardData.getCount()) {
				return false;
			}
			int color = mLargeOutCardData.getFirstCard().getColor();
			int level = mLargeOutCardData.getFirstCard().getLevel();
			int count = 0;
			for (int i = 0; i < mCardManager.getPlayerData(playerId).getCardList().size(); i++) {
				if (level > 0) {
					if (mCardManager.getPlayerData(playerId).getCardList().get(i).getLevel() > 0) {
						count++;
					}
				} else if (mCardManager.getPlayerData(playerId).getCardList().get(i).getColor() == color 
						&& mCardManager.getPlayerData(playerId).getCardList().get(i).getLevel() == 0) {
					count++;
				}
			}

			if (count > datas.size()) {
				count = datas.size();
			}

			int count2 = 0;
			for (int i = 0; i < datas.size(); i++) {
				if (level > 0) {
					if (datas.get(i).getLevel() > 0) {
						count2++;
					}
				} else if (datas.get(i).getColor() == color && datas.get(i).getLevel() == 0) {
					count2++;
				}
			}

			if (count2 < count) {
				return false;
			}
			
			return true;
		}
	}
	
	private boolean isTrumpRight(int playerId, ArrayList<CardData> datas) {
		if (datas.size() <= 0) {
			// 不叫主视为一次正确操作
			return true;
		}
		
		if (mGameRoundInfo.getFirst() != playerId && datas.size() <= mLargeTrumpData.getCount()) {
			return false;
		}
		
		HashMap<Integer, Integer> trumpInfo = new HashMap<Integer, Integer>();
		for (CardData data : datas) {
			int index = data.getIndex();
			int size = 0;
			if (trumpInfo.get(index) != null) {
				size = trumpInfo.get(index);
			}
			size++;
			trumpInfo.put(index, size);
		}
		
		
		int trumpFace = mGameInfo.getTrumpFace();
		if (trumpInfo.size() == 3 && trumpInfo.get(CardConstant.CARD_KING_BEGIN) != null) {
			if (trumpInfo.get(CardConstant.CARD_FANG_J) != null && 
					trumpInfo.get(CardConstant.CARD_FANG_A + trumpFace - 1) != null &&
					trumpInfo.get(CardConstant.CARD_FANG_A + trumpFace - 1) > 1) {
				return true;
			}
			
			if (trumpInfo.get(CardConstant.CARD_MEI_J) != null && 
					trumpInfo.get(CardConstant.CARD_MEI_A + trumpFace - 1) != null &&
					trumpInfo.get(CardConstant.CARD_MEI_A + trumpFace - 1) > 1) {
				return true;
			}
			
			if (trumpInfo.get(CardConstant.CARD_TAO_J) != null && 
					trumpInfo.get(CardConstant.CARD_TAO_A + trumpFace - 1) != null &&
					trumpInfo.get(CardConstant.CARD_TAO_A + trumpFace - 1) > 1) {
				return true;
			}
			
			if (trumpInfo.get(CardConstant.CARD_XIN_J) != null && 
					trumpInfo.get(CardConstant.CARD_XIN_A + trumpFace - 1) != null &&
					trumpInfo.get(CardConstant.CARD_XIN_A + trumpFace - 1) > 1) {
				return true;
			}
		}
		return false;
	}
	
	private void updateTrumpData(int playerId, ArrayList<CardData> datas) {
		if (datas == null || datas.size() <= 0) {
			mTrumpFlags.put(playerId, TRUMP_NOT);
			return;
		}
		
		mTrumpFlags.put(playerId, TRUMP_CALL);
		
		if (playerId == mGameRoundInfo.getFirst() || 
				datas.size() > mLargeTrumpData.getCount()) {
			mGameRoundInfo.setLarger(playerId);
			mLargeTrumpData.updateData(datas);
			// 更新backtrumplist
			mBackTrumpList.add(new BackTrumpData(playerId, datas));
		}
	}
	
	private void updateOutCardData(int playerId, ArrayList<CardData> datas) {
		if (playerId == mGameRoundInfo.getFirst()) {
			mGameRoundInfo.setLarger(playerId);
			OutCardData data = new OutCardData(datas.get(0), datas.size());
			mLargeOutCardData = data;
		    return;
		}
		
		if (isLarge(mLargeOutCardData, datas)) {
			mGameRoundInfo.setLarger(playerId);
			mLargeOutCardData.setLargeCard(datas.get(0));
		}
	}
	
	private boolean isLarge(OutCardData currentData, ArrayList<CardData> datas) {
		if (!isSameCard(datas) || datas.size() <= 0) {
			return false;
		}
		
		return CardUtil.isLarge(currentData.getLargeCard(), datas.get(0));
	}
	
	private boolean isSameCard(ArrayList<CardData> datas) {
		if (datas.size() == 1) {
			return true;
		}
		
		for (int i = 1; i < datas.size(); i++) {
			if (datas.get(0).getIndex() != datas.get(i).getIndex()) {
				return false;
			}
		}
		return true;
	}
	
	private void updateTributedState(int tributeCount) {
		if (tributeCount > 1) {
			mIsTributeEnd = false;
			mIsSecTributeEnd = false;
		} else if (tributeCount > 0) {
			mIsTributeEnd = false;
			mIsSecTributeEnd = true;
		} else {
			mIsTributeEnd = true;
			mIsSecTributeEnd = true;
		}
	}
	
	private void handleTributeNextResult(int stage, int playerId, ArrayList<CardData> datas) {
		if (stage == GameStage.PAY_TRIBUTE_END) {
			mCardManager.removeCardDatas(playerId, datas);
			mGameRoundInfo.setCurrent(mGameRoundInfo.getCurrent() + 1);
			mCardManager.obtainTributeCardDatas(mGameRoundInfo.getCurrent(), datas);
			mStage.setStage(GameStage.BACK_TRIBUTE_WAIT);
		} else if (stage == GameStage.PAY_TRIBUTE_SEC_END) {
			mCardManager.removeCardDatas(playerId, datas);
			mGameRoundInfo.setSecurrent(mGameRoundInfo.getSecurrent() + 1);
			mCardManager.obtainTributeCardDatas(mGameRoundInfo.getSecurrent(), datas);
			mStage.setSecStage(GameStage.BACK_TRIBUTE_SEC_WAIT);
		} else if (stage == GameStage.BACK_TRIBUTE_END) {
			mCardManager.removeCardDatas(playerId, datas);
			mIsTributeEnd = true;
			mCardManager.obtainTributeCardDatas(mGameRoundInfo.getCurrent() > 0 ? mGameRoundInfo.getCurrent() - 1 : 3, datas);
		} else if (stage == GameStage.BACK_TRIBUTE_SEC_END) {
			mCardManager.removeCardDatas(playerId, datas);
			mIsSecTributeEnd = true;
			mCardManager.obtainTributeCardDatas(mGameRoundInfo.getSecurrent() > 0 ? mGameRoundInfo.getSecurrent() - 1 : 3, datas);
		}
		
		if (mIsTributeEnd && mIsSecTributeEnd) {
			mGameRoundInfo.setSecurrent(-1);
			mGameRoundInfo.setFirst(mGameInfo.getBankerId());
			mGameRoundInfo.setCurrent(mGameInfo.getBankerId());
			mIsTributeEnd = false;
			mIsSecTributeEnd = false;
			mGameInfo.setTributeCount(0);
			mStage.setStage(GameStage.ROUND_CALLTRUMP_END);
		}
	}
	
	/**
	 * 是否叫主结束
	 * 不叫的玩家超过三家视为结束
	 */
	private boolean isCallTrumpRoundEnd() {
		int notTrumpCount = 0;
		for (int i = 0; i < 4; i++) {
			if (mTrumpFlags.get(i) == TRUMP_NOT) {
				notTrumpCount++;
			}
		}
		
		return notTrumpCount >= 3;
	}
	
	/**
	 * 获得下一个叫主的玩家
	 * 
	 */
	private int getNextCallTrumpPlayerId(int playerId) {
		if (playerId == mGameRoundInfo.getFirst() && !mGameInfo.isFirstGame()) {
			/**
			 * 第二局开始，赢的一方先叫主
			 */
			int nextPlayerId = playerId + 2;
			if (nextPlayerId >= 4) {
				nextPlayerId -= 4;
			}
			return nextPlayerId;
		}
		
		int count = 0;
		while (count <= 3) {
			count++;
			int nextPlayerId = playerId + count;
			if (nextPlayerId >= 4) {
				nextPlayerId -= 4;
			}
			
			if (mTrumpFlags.get(nextPlayerId) != TRUMP_NOT) {
				return nextPlayerId;
			}
		}
		
		return playerId;
	}
	
	private void handleNextResult(int playerId) {
		int lastPlayer = mGameRoundInfo.getFirst() == 0 ? 3 : mGameRoundInfo.getFirst() - 1;
		boolean isRoundEnd = (playerId == lastPlayer);
		if (isCallTrumpStage()) {
			// isRoundEnd = isCallTrumpRoundEnd();
		} else if (isBackTrumpStage()) {
			isRoundEnd = (mBackTrumpList == null || mBackTrumpList.size() <= 1);
		}
		
		if (/*playerId == lastPlayer*/isRoundEnd) {
			handleRoundResult();
			return;
		}
		
		mGameRoundInfo.setCurrent((playerId + 1) > 3 ? 0 : (playerId + 1));
		if (isCallTrumpStage()) {
			mGameRoundInfo.setCurrent(getNextCallTrumpPlayerId(playerId));
		} else if (isBackTrumpStage()) {
			mGameRoundInfo.setCurrent(mBackTrumpList.get(mBackTrumpList.size() - 1).getPlayerId());
		}
		
		if (isCallTrumpStage()) {
			mStage.setStage(GameStage.CALL_TRUMP_WAIT);
		} else if (isBackTrumpStage()) {
			mStage.setStage(GameStage.BACK_TRUMP_WAIT);
		} else if (isOutCardStage()) {
			mStage.setStage(GameStage.OUT_CARD_WAIT);
		}
	}
	
	private void handleGameResult() {
		mGameInfo.handlerResult();
		updateTributedState(mGameInfo.getTributeCount());
	}
	
	private void handleRoundResult() {
		if (mCardManager.getPlayerData(0).getCardList().size() <= 0) {
			// 扣底开始
			if (!isFriendLarge(mGameInfo.getBankerId())) {
				int score = getRoundScore() + getOutLastCardScore();
				int multi = mOutCardDatas.get(0).size() > 2 ? (mOutCardDatas.get(0).size() - 1) : 1;
				mGameInfo.addScore(score * multi);
			}
			// 扣底结束
			mStage.setStage(GameStage.GAME_END);
			return;
		}
		
		if (isOutCardStage()) {
			if (!isFriendLarge(mGameInfo.getBankerId())) {
				mGameInfo.addScore(getRoundScore());
			}
		}
		
		if (isCallTrumpStage()) {
			mGameInfo.setTrumpColor(mLargeTrumpData.getTrumpColor());
			if (mBackTrumpList.size() > 0) {
				mCardManager.obtainTributeCardDatas(mBackTrumpList.get(mBackTrumpList.size() - 1).getPlayerId(), mOutCardDatas.get(mBackTrumpList.get(mBackTrumpList.size() - 1).getPlayerId()));
			}
			// mOutCardDatas.remove(mGameRoundInfo.getCurrent());
			mCardManager.updateCardDatasForTrump(mGameInfo.getTrumpFace(), mGameInfo.getTrumpColor());
			if (mBackTrumpList.size() <= 1) {
				updateGameInfoForRoundResult();
			} else {
				mGameRoundInfo.setFirst(mBackTrumpList.get(mBackTrumpList.size() - 1).getPlayerId());
				mGameRoundInfo.setCurrent(mBackTrumpList.get(mBackTrumpList.size() - 1).getPlayerId());
			}
			mStage.setStage(GameStage.ROUND_CALLTRUMP_END);
			return;
		} else if (isBackTrumpStage()) {
			updateGameInfoForRoundResult();
			mStage.setStage(GameStage.ROUND_BACKTRUMP_END);
			return;
		}
		
		mGameRoundInfo.setFirst(mGameRoundInfo.getLarger());
		mGameRoundInfo.setCurrent(mGameRoundInfo.getLarger());
		mStage.setStage(GameStage.ROUND_OUTCARD_END);
	}
	
	private void updateGameInfoForRoundResult() {
		if (mGameInfo.isFirstGame()) {
			mGameInfo.setFirstGame(false);
			mGameInfo.setBankerId(mGameRoundInfo.getLarger());
			mGameRoundInfo.setFirst(mGameRoundInfo.getLarger());
			mGameRoundInfo.setCurrent(mGameRoundInfo.getLarger());
		} else {
			if (mGameInfo.getTributeCount() > 0) {
				mGameRoundInfo.setFirst(mGameInfo.getBankerId());
				mGameRoundInfo.setCurrent(mGameInfo.getBankerId() > 0 ? mGameInfo.getBankerId() - 1 : 3);
				if (mGameInfo.getTributeCount() > 1) {
					mGameRoundInfo.setSecurrent(mGameRoundInfo.getCurrent() + 2);
				}
			} else {
				mGameRoundInfo.setFirst(mGameInfo.getBankerId());
				mGameRoundInfo.setCurrent(mGameInfo.getBankerId());
			}
		}
		
		// 测试进贡
		boolean test = false;
		if (test) {
			if (mGameInfo.getTrumpColor() < 0) {
				mGameInfo.setTrumpColor(1);
			}
			mGameInfo.setTributeCount(2);
			mGameRoundInfo.setCurrent(1);
			mGameRoundInfo.setSecurrent(3);
			updateTributedState(mGameInfo.getTributeCount());
		}
	}
}
