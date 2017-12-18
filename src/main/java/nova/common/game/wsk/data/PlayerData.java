package nova.common.game.wsk.data;


import java.util.ArrayList;
import java.util.HashMap;

import nova.common.game.wsk.util.CardConstant;
import nova.common.game.wsk.util.CardUtil;


public class PlayerData {
    private boolean isDebug = false;
	private ArrayList<CardData> mTotalCardList = new ArrayList<CardData>();
	private ArrayList<CardGroupData> mCardGroups = new ArrayList<CardGroupData>();
	private String mPlayer;
	
	public PlayerData(String player, ArrayList<CardData> cardDatas) {
		mPlayer = player;
		for (int i = 0; i < 5; i++) {
			CardGroupData group = new CardGroupData();
			mCardGroups.add(group);
		}
		
		mTotalCardList.addAll(cardDatas);
		initCardList();
	}
	
	public ArrayList<CardData> getCardList() {
		return mTotalCardList;
	}
	
	public ArrayList<CardData> getOutLastCards() {
		return getOutLastCards(true);
	}
	
	public ArrayList<CardData> getOutLastCards(boolean isOutCard) {
		ArrayList<CardData> datas = new ArrayList<CardData>();
		
		for (int i = 0; i < 5; i++) {
			if (datas.size() >= 8) {
				break;
			}
			
			datas.addAll(mCardGroups.get(i).getLastCard(8 - datas.size()));
		}
		
		// 保证出牌张数符合要求
		if (datas.size() < 8) {
			addOutCardFromTotalCardList(8, datas);
		}
		
		if (isOutCard) {
			removeOutCardFromTotalCard(datas);
		}
		
		return datas;
	}
	
	public ArrayList<CardData> getTrumpCards(TrumpData trumpData) {
		return getTrumpCards(trumpData, true);
	}
	
	public ArrayList<CardData> getTrumpCards(TrumpData trumpData, boolean isOutcard) {
		ArrayList<CardData> datas = mCardGroups.get(4).getCardList();
		ArrayList<CardData> kingData = new ArrayList<CardData>();
		HashMap<Integer, ArrayList<CardData>> jDatas = new HashMap<Integer, ArrayList<CardData>>();
		HashMap<Integer, ArrayList<CardData>> tDatas = new HashMap<Integer, ArrayList<CardData>>();
		for (CardData data : datas) {
			if (data.getIndex() == CardConstant.CARD_KING_BEGIN) {
				kingData.add(data);
			} else if (CardUtil.isJCard(data.getIndex())) {
				int jColor = data.getColor();
				if (jDatas.get(jColor) == null) {
					jDatas.put(jColor, new ArrayList<CardData>());
				}
				jDatas.get(jColor).add(data);
			} else if (CardUtil.isTrumpCard(data.getIndex(), trumpData.getTrumpFace())) {
				int tColor = data.getColor();
				if (tDatas.get(tColor) == null) {
					tDatas.put(tColor, new ArrayList<CardData>());
				}
				tDatas.get(tColor).add(data);
			}
		}
		
		ArrayList<CardData> trumpCards = new ArrayList<CardData>();
		if (kingData.size() != 0) {
			for (int i = 0; i < 4; i++) {
				if (tDatas.get(i) != null && tDatas.get(i).size() >= 2 &&
						jDatas.get(i) != null && jDatas.get(i).size() > 0) {
					int total = kingData.size() + tDatas.get(i).size() + jDatas.get(i).size();
					if (total > trumpData.getCount()) {
						trumpCards.addAll(kingData);
						trumpCards.addAll(jDatas.get(i));
						trumpCards.addAll(tDatas.get(i));
						break;
					}
				}
			}
		}
		
		if (isOutcard) {
			removeOutCardFromTotalCard(trumpCards);
		}
		
		return trumpCards;
	}
	
	public ArrayList<CardData> getBackTrumpCards(TrumpData previousTrumpData, int trumpColor) {
		ArrayList<CardData> datas = new ArrayList<CardData>();
		
		int lastCardCount = previousTrumpData.getCount() - previousTrumpData.getKingCount();
		datas.addAll(mCardGroups.get(previousTrumpData.getTrumpColor()).getBackTrumpCard(lastCardCount));
		
		int currentCardCount = previousTrumpData.getKingCount();
		if (datas.size() < lastCardCount) {
			currentCardCount += (lastCardCount - datas.size());
		}
		datas.addAll(mCardGroups.get(trumpColor).getBackTrumpCard(currentCardCount));
		
		// 出牌不够时，从主牌中取
		if (datas.size() < previousTrumpData.getCount()) {
			datas.addAll(mCardGroups.get(4).getBackTrumpCard(previousTrumpData.getCount() - datas.size()));
		}
		
		// 出牌还不够时，任意牌添够，保证出牌张数符合要求(不可能出现)
		if (datas.size() < previousTrumpData.getCount()) {
			addOutCardFromTotalCardList(previousTrumpData.getCount(), datas);
		}
		
		removeOutCardFromTotalCard(datas);
		
		return datas;
	}
	
	public ArrayList<CardData> getPayTributeCards(int count) {
		return getPayTributeCards(count, true);
	}
	
	public ArrayList<CardData> getPayTributeCards(int count, boolean isOutcard) {
		ArrayList<CardData> cardDatas = new ArrayList<CardData>();
		ArrayList<CardData> datas = mCardGroups.get(4).getCardList();
		// 大小王
		for (CardData data : datas) {
			if (cardDatas.size() >= count || data.getIndex() < CardConstant.CARD_KING_BEGIN) {
				break;
			}
			if (data.getIndex() >= CardConstant.CARD_KING_BEGIN) {
				cardDatas.add(data);
			}
		}
		
		// A
		for (int i = 0; i < 4; i++) {
			ArrayList<CardData> tDatas = mCardGroups.get(i).getCardList();
			if (tDatas != null && tDatas.size() > 0) {
				for (CardData data : tDatas) {
					if (cardDatas.size() >= count) {
						break;
					}
					
					if (CardUtil.isACard(data.getIndex())) {
						cardDatas.add(data);
					}
				}
			}
		}
		
		// Q
		for (int i = 0; i < 4; i++) {
			ArrayList<CardData> tDatas = mCardGroups.get(i).getCardList();
			if (tDatas != null && tDatas.size() > 0) {
				for (CardData data : tDatas) {
					if (cardDatas.size() >= count) {
						break;
					}
					
					if (CardUtil.isQCard(data.getIndex())) {
						cardDatas.add(data);
					}
				}
			}
		}
		
		if (isOutcard) {
			removeOutCardFromTotalCard(cardDatas);
		}
		
		return cardDatas;
	}
	
	public ArrayList<CardData> getBackTributeCards(int count) {
		return getBackTributeCards(count, true);
	}
	
	public ArrayList<CardData> getBackTributeCards(int count, boolean isOutcard) {
		ArrayList<CardData> cardDatas = new ArrayList<CardData>();
		
		for (int i = 0; i < 4; i++) {
			ArrayList<CardData> tDatas = mCardGroups.get(i).getCardList();
			if (tDatas != null && tDatas.size() > 0) {
				for (int j = tDatas.size() - 1; j >= 0; j--) {
					if (cardDatas.size() >= count) {
						break;
					}
					cardDatas.add(tDatas.get(j));
				}
			}
		}
		
		if (isOutcard) {
			removeOutCardFromTotalCard(cardDatas);
		}
		
		return cardDatas;
	}
	
	public ArrayList<CardData> getOutCardDatas(OutCardData outCard, boolean isFriendLarge) {
		return getOutCardDatas(outCard, isFriendLarge, true);
	}
	
	public ArrayList<CardData> getOutCardDatas(OutCardData outCard, boolean isFriendLarge, boolean isOutcard) {
		ArrayList<CardData> cardDatas = new ArrayList<CardData>();
		addOutCardDatas(outCard, cardDatas, isFriendLarge);
		if (isOutcard) {
			removeOutCardFromTotalCard(cardDatas);
		}
		
		return cardDatas;
	}
	
	public ArrayList<CardData> getFirstOutCardData() {
		return getFirstOutCardData(true);
	}
	
	public ArrayList<CardData> getFirstOutCardData(boolean isOutcard) {
		ArrayList<CardData> cardDatas = new ArrayList<CardData>();
		
		for (int i = 0; i < 4; i++) {
			for (int j = 1; j <= 4; j++) {
				ArrayList<Integer> infoList = mCardGroups.get(i).getGroupInfo().get(j);
				if (infoList != null) {
					for (int k = 0; k < infoList.size(); k++) {
						if (CardUtil.isACard(mCardGroups.get(i).getCardList().get(Integer.valueOf(infoList.get(k))).getIndex())) {
							for (int m = 0; m < j; m++) {
								cardDatas.add(mCardGroups.get(i).getCardList().get(Integer.valueOf(infoList.get(k)) - m));
							}
							if (cardDatas.size() > 0) {
								if (isOutcard) {
									removeOutCardFromTotalCard(cardDatas);
								}
								return cardDatas;
							}
						}
					}
				}
			}
			
			ArrayList<Integer> infoList = mCardGroups.get(i).getGroupInfo().get(4);
			if (infoList != null) {
				if (infoList.size() > 0) {
					for (int j = 0; j < 4; j++) {
						cardDatas.add(mCardGroups.get(i).getCardList().get(Integer.valueOf(infoList.get(0)) - j));
					}
					if (cardDatas.size() > 0) {
						if (isOutcard) {
							removeOutCardFromTotalCard(cardDatas);
						}
						return cardDatas;
					}
				}
			}
		}
		
		for (int i = 0; i < 4; i++) {
			ArrayList<Integer> infoList = mCardGroups.get(i).getGroupInfo().get(4);
			if (infoList != null) {
				if (infoList.size() > 0) {
					for (int j = 0; j < 4; j++) {
						cardDatas.add(mCardGroups.get(i).getCardList().get(Integer.valueOf(infoList.get(0)) - j));
					}
					if (cardDatas.size() > 0) {
						if (isOutcard) {
							removeOutCardFromTotalCard(cardDatas);
						}
						return cardDatas;
					}
				}
			}
		}
		
		for (int i = 0; i < 4; i++) {
			ArrayList<Integer> infoList = mCardGroups.get(i).getGroupInfo().get(3);
			if (infoList != null) {
				if (infoList.size() > 0) {
					for (int j = 0; j < 3; j++) {
						cardDatas.add(mCardGroups.get(i).getCardList().get(Integer.valueOf(infoList.get(0)) - j));
					}
					if (cardDatas.size() > 0) {
						if (isOutcard) {
							removeOutCardFromTotalCard(cardDatas);
						}
						return cardDatas;
					}
				}
			}
		}
		
		
		cardDatas.add(mTotalCardList.get(mTotalCardList.size() - 1));
		if (isOutcard) {
			removeOutCardFromTotalCard(cardDatas);
		}
		return cardDatas;
	}
	
	private void addOutCardDatas(OutCardData outCard, ArrayList<CardData> cardDatas, boolean isFriendLarge) {
		int groupId = outCard.getFirstCard().getLevel() > 0 ? 4 : outCard.getFirstCard().getColor();
		cardDatas.addAll(mCardGroups.get(groupId).getOutCard(outCard.getCount(), outCard, isFriendLarge));
		
		if (cardDatas.size() <= 0 && !isFriendLarge) {
			cardDatas.addAll(mCardGroups.get(4).getOutCard(outCard.getCount(), outCard, isFriendLarge));
		}
		
		if (cardDatas.size() < outCard.getCount()) {
			addOutCardFromDifferentColor(groupId, outCard, cardDatas, isFriendLarge);
		}
		
		// 保证出牌张数符合要求
		if (cardDatas.size() < outCard.getCount()) {
			addOutCardFromTotalCardList(outCard.getCount(), cardDatas);
		}
	}
	
	private void addOutCardFromTotalCardList(int count, ArrayList<CardData> cardDatas) {
		for (int i = mTotalCardList.size() - 1; i >= 0; i--) {
			if (cardDatas.size() >= count) {
				break;
			}
			
			cardDatas.add(mTotalCardList.get(i));
		}
	}
	
	private void addOutCardFromDifferentColor(int groupId, OutCardData outCard, ArrayList<CardData> cardDatas, boolean isFriendLarge) {
		for (int i = 0; i < 5; i++) {
			if (groupId == i) {
				continue;
			}
			
			if (cardDatas.size() >= outCard.getCount()) {
				break;
			}
			cardDatas.addAll(mCardGroups.get(i).getOutCard(outCard.getCount() - cardDatas.size(), outCard, isFriendLarge));
		}
	}
	
	private void initCardList() {
		for (CardGroupData group : mCardGroups) {
			group.removeAll();
		}
		
		for (int i = 0; i < mTotalCardList.size(); i++) {
			CardData card = mTotalCardList.get(i);
			int groupId = card.getLevel() > 0 ? 4 : card.getColor();
			mCardGroups.get(groupId).getCardList().add(card);
		}
		
		for (CardGroupData group : mCardGroups) {
			group.initGroupInfo();
			if (isDebug) {
				// android.util.Log.e("zhangxx", group.toString());
			}
		}
	}
	
	public void removeOutCardFromTotalCard(ArrayList<CardData> cardDatas) {
		for (int i = 0; i < cardDatas.size(); i++) {
			// mTotalCardList.remove(cardDatas.get(i));
			for (CardData data : mTotalCardList) {
				if (data.getIndex() == cardDatas.get(i).getIndex()) {
					mTotalCardList.remove(data);
					break;
				}
			}
		}
		initCardList();
	}
	
	public String getPlayer() {
		return mPlayer;
	}
}