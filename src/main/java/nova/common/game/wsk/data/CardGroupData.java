package nova.common.game.wsk.data;


import java.util.ArrayList;
import java.util.HashMap;

import nova.common.game.wsk.util.CardUtil;

public class CardGroupData {
	private ArrayList<CardData> mCardList = new ArrayList<CardData>();
	private HashMap<Integer, ArrayList<Integer>> mCardInfo = new HashMap<Integer, ArrayList<Integer>>();
	
	public ArrayList<CardData> getCardList() {
		return mCardList;
	}
	
	public HashMap<Integer, ArrayList<Integer>> getGroupInfo() {
		return mCardInfo;
	}
	
	public boolean removeAll() {
		return mCardList.removeAll(mCardList);
	}
	
	public void initGroupInfo() {
		mCardInfo.clear();
		int count = 0;
		int index = -1;
		for (int i = 0; i < mCardList.size(); i++) {
			if (index == mCardList.get(i).getIndex()) {
				count++;
			} else {
				if (index != -1) {
					setCardInfo(count, i - 1);
				}
				
				index = mCardList.get(i).getIndex();
				count = 1;
			}
			
			if (i == mCardList.size() - 1) {
				setCardInfo(count, i);
			}
		}
	}
	
	private void setCardInfo(int count, int order) {
		/**
		String info = mCardInfo.get(count);
		if (info == null) {
			info = "";
		}
	    mCardInfo.put(count, info + order + ",");
	    **/
	    ArrayList<Integer> infoList = mCardInfo.get(count);
	    if (infoList == null) {
	    	infoList = new ArrayList<Integer>();
	    }
	    infoList.add(order);
	    mCardInfo.put(count, infoList);
	}
	
	public ArrayList<CardData> getBackTrumpCard(int count) {
		ArrayList<CardData> datas = new ArrayList<CardData>();
		
		for (int i = mCardList.size() - 1; i >= 0; i--) {
			if (datas.size() >= count) {
				break;
			}
			datas.add(mCardList.get(i));
		}
		
		return datas;
	}
	
	public ArrayList<CardData> getLastCard(int count) {
		ArrayList<CardData> datas = new ArrayList<CardData>();
		for (int i = 1; i <= 2; i++) {
			ArrayList<Integer> infos = mCardInfo.get(i);
			if (infos == null) {
				continue;
			}
			
			for (Integer index : infos) {
				if (datas.size() >= count) {
					break;
				}
				CardData data = mCardList.get(index);
				if (data.getFace() != 14) {
					datas.add(data);
				}
			}
		}
		return datas;
	}
	
	public ArrayList<CardData> getOutCard(int count, OutCardData outCard, boolean isFriendLarge) {
		ArrayList<CardData> cardList = new ArrayList<CardData>();
		if (isFriendLarge) {
			addScoreOutCard(count, cardList);
		} else {
			if (count >= outCard.getCount()) {
				addLargeOutCard(outCard, cardList);
			}
			addOutCard(count, cardList);
		}
		
		return cardList;
	}
	
	private boolean addLargeOutCard(OutCardData outCard, ArrayList<CardData> cardList) {
		if (mCardList.size() <= 0) {
			return false;
		}
		
		if (mCardList.get(0).getLevel() > 0 ||
				outCard.getLargeCard().getColor() == mCardList.get(0).getColor()) {
			for (int i = outCard.getCount(); i <= 4; i++) {
				ArrayList<Integer> infoList = mCardInfo.get(i);
				if (infoList == null) {
					continue;
				}
				
				for (int j = 0; j < infoList.size(); j++) {
					CardData card = mCardList.get(infoList.get(j));
					if (CardUtil.isLarge(outCard.getLargeCard(), card)) {
						for (int k = 0; k < outCard.getCount(); k++) {
							cardList.add(mCardList.get(infoList.get(j)- k));
						}
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	private void addScoreOutCard(int count, ArrayList<CardData> cardList) {
		addScore10OutCard(count, cardList);
		addScore5OutCard(count, cardList);
		addScore0OutCard(count, cardList);
	}
	
	private void addOutCard(int count, ArrayList<CardData> cardList) {
		addScore0OutCard(count, cardList);
		addScore5OutCard(count, cardList);
		addScore10OutCard(count, cardList);
	}
	
	private void addScore5OutCard(int count, ArrayList<CardData> cardList) {
		for (int i = mCardList.size() - 1; i >= 0;  i--) {
			CardData card = mCardList.get(i);
			if (cardList.size() >= count) {
				return;
			}
			
			if (card.getScore() == 5) {
				cardList.add(card);
			}
		}
	}
	
	private void addScore10OutCard(int count, ArrayList<CardData> cardList) {
		for (int i = mCardList.size() - 1; i >= 0;  i--) {
			CardData card = mCardList.get(i);
			if (cardList.size() >= count) {
				return;
			}
			
			if (card.getScore() == 10) {
				cardList.add(card);
			}
		}
	}
	
	private void addScore0OutCard(int count, ArrayList<CardData> cardList) {
		for (int i = mCardList.size() - 1; i >= 0;  i--) {
			CardData card = mCardList.get(i);
			if (cardList.size() >= count) {
				return;
			}
			
			if (card.getScore() != 5 && card.getScore() != 10) {
				cardList.add(card);
			}
		}
	}
	
	public String toString() {
		String info = "";
		if (mCardList.size() <= 0) {
			info = "no card";
		} else {
			info = "card color = " + mCardList.get(0).getColor() + " size = " + mCardList.size() + " :\n";
			for (CardData card : mCardList) {
				info = info + card.getIndex() + ",";
			}
			for (int i = 1; i <= 4; i++) {
				info = info + "\n" + i + ":";
				ArrayList<Integer> infoList = mCardInfo.get(i);
				if (infoList != null) {
					for (int j = 0; j < infoList.size(); j++) {
						info = info + mCardList.get(infoList.get(j)).getIndex() + ",";
					}
				} else {
					info = info + mCardInfo.get(i);
				}
			}
		}
		return info;
	}
}