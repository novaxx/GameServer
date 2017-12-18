package nova.common.game.wsk;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import nova.common.game.wsk.data.CardData;
import nova.common.game.wsk.data.PlayerData;
import nova.common.game.wsk.util.CardConstant;


public class CardManager {
	private HashMap<Integer, ArrayList<CardData>> mCardDatas = new HashMap<Integer, ArrayList<CardData>>();
	private HashMap<Integer, PlayerData> mPlayerDatas = new HashMap<Integer, PlayerData>();
	private ArrayList<CardData> mLastCardDatas = new ArrayList<CardData>();
	
	public PlayerData getPlayerData(int playerId) {
		return mPlayerDatas.get(playerId);
	}
	
	public HashMap<Integer, PlayerData> getPlayerDatas() {
		return mPlayerDatas;
	}
	
	public void removeCardDatas(int playerId, ArrayList<CardData> datas) {
		// mCardDatas.get(playerId).removeAll(datas);
		for (int i = 0; i < datas.size(); i++) {
			for (CardData data : mCardDatas.get(playerId)) {
				if (data.getIndex() == datas.get(i).getIndex()) {
					mCardDatas.get(playerId).remove(data);
					break;
				}
			}
		}
	}
	
	public void removeOutCardsFromPlayerData(int playerId, ArrayList<CardData> datas) {
		getPlayerData(playerId).removeOutCardFromTotalCard(datas);
	}
	
	public void initCardDatas(int trumpFace, int trumpColor) {
		mCardDatas.clear();
		mPlayerDatas.clear();
		mLastCardDatas.removeAll(mLastCardDatas);
		getRandomCardList(trumpFace, trumpColor);
		sortCard();
		initPlayerData();
	}
	
	public void updateCardDatasForTrump(int trumpFace, int trumpColor) {
		updateCardLevel(trumpFace, trumpColor);
		sortCard();
		initPlayerData();
	}
	
	public void sendLastCardDatas(int playerId, boolean isTrumped) {
		sendLastCard(playerId, isTrumped);
		sortCard();
		initPlayerData();
	}
	
	public void obtainTributeCardDatas(int playerId, ArrayList<CardData> datas) {
		mCardDatas.get(playerId).addAll(datas);
		sortCardList(mCardDatas.get(playerId));
		mPlayerDatas.put(playerId, new PlayerData(getPlayerName(playerId), mCardDatas.get(playerId)));
	}
	
	public ArrayList<CardData> getLastCardDatas() {
		return mLastCardDatas;
	}
	
	public ArrayList<CardData> getOutLastCardDatas(int playerId) {
		ArrayList<CardData> datas = new ArrayList<CardData>();
		datas = mPlayerDatas.get(playerId).getOutLastCards();
		return datas;
	}
	
	private void updateCardLevel(int trumpFace, int trumpColor) {
		for (int i = 0; i < mCardDatas.size(); i++) {
			for (CardData data : mCardDatas.get(i)) {
				data.updateLevel(trumpFace, trumpColor);
			}
		}
	}
	
	private void getRandomCardList(int trumpFace, int trumpColor) {
		if (CardConstant.DEBUG) {
			// 测试程序
			getRandomCardListForDebug();
			return;
		}
		
		Random random = new Random();
		int[] tempData = new int[CardConstant.CARD_ELEMENTS.length];
		System.arraycopy(CardConstant.CARD_ELEMENTS, 0, tempData, 0, CardConstant.CARD_ELEMENTS.length);
		int count = 0;
		int position = 0;
		int order = 0;
		do {
			position = random.nextInt(CardConstant.CARD_ELEMENTS.length - count);
			int index = tempData[position];
			
			if (count >= CardConstant.CARD_ELEMENTS.length - 8) {
				mLastCardDatas.add(new CardData(index, trumpFace, trumpColor));
			} else {
				if (mCardDatas.get(order) == null) {
					mCardDatas.put(order, new ArrayList<CardData>());
				}
				mCardDatas.get(order).add(new CardData(index, trumpFace, trumpColor));
				order = order >= 3 ? 0 : order + 1;
			}
			
			count++;
			tempData[position] = tempData[CardConstant.CARD_ELEMENTS.length - count];
		} while (count < CardConstant.CARD_ELEMENTS.length);
	}
	
	private void getRandomCardListForDebug() {
		if (2 == CardConstant.Debug_Type) {
			fillCardListForDebug(CardConstant.CARD_ELEMENTS_DEBUG_2);
		} else {
			fillCardListForDebug(CardConstant.CARD_ELEMENTS_DEBUG_1);
		}
	}
	
	private void fillCardListForDebug(int[][] cardElements) {
		for (int i = 0; i < cardElements.length; i++) {
			for (int j = 0; j < cardElements[i].length; j++) {
				if (4 == i) {
					mLastCardDatas.add(new CardData(cardElements[i][j], 5, -1));
				} else {
					if (mCardDatas.get(i) == null) {
						mCardDatas.put(i, new ArrayList<CardData>());
					}
					mCardDatas.get(i).add(new CardData(cardElements[i][j], 5, -1));
				}
			}
		}
	}
	
	private void sendLastCard(int playerId, boolean isTrumped) {
		if (isTrumped) {
			mCardDatas.get(playerId).addAll(mLastCardDatas);
		} else {
			int player = playerId;
			for (CardData data : mLastCardDatas) {
				mCardDatas.get(player).add(data);
				player = player >= 3 ? 0 : player + 1;
			}
		}
		mLastCardDatas.removeAll(mLastCardDatas);
	}
	
	private void sortCard() {
		for (int i = 0; i < mCardDatas.size(); i++) {
			sortCardList(mCardDatas.get(i));
		}
	}
	
	private void initPlayerData() {
		for (int i = 0; i < mCardDatas.size(); i++) {
			mPlayerDatas.put(i, new PlayerData(getPlayerName(i), mCardDatas.get(i)));
		}
	}
	
	private void sortCardList(ArrayList<CardData> cardData) {
		for (int i = 0; i < cardData.size(); i++) {
			// 从第i+1为开始循环数组
			for (int j = i + 1; j < cardData.size(); j++) {
				// 如果前一位比后一位小，那么就将两个数字调换
				// 这里是按降序排列
				// 如果你想按升序排列只要改变符号即可
				if (compareCard(cardData.get(i), cardData.get(j))) {
					CardData tem = cardData.get(i);
					cardData.set(i, cardData.get(j));
					cardData.set(j, tem);
				}
			}
		}
	}
	
	private boolean compareCard(CardData card1, CardData card2) {
		if (card2.getLevel() != card1.getLevel()) {
			return card2.getLevel() > card1.getLevel();
		}
		
		if (card2.getColor() != card1.getColor()) {
			return card2.getIndex() > card1.getIndex();
		}
		
		return card2.getFace() > card1.getFace();
	}
	
	private String getPlayerName(int playerId) {
		return "player " + playerId;
	}
}
