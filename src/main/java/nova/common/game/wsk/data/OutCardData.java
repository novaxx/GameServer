package nova.common.game.wsk.data;



public class OutCardData {
	private int mCount;
	private CardData mLargeCard;
	private CardData mFirstCard;
	
	public OutCardData(CardData card, int count) {
		mCount = count;
		mFirstCard = card;
		mLargeCard = card;
	}

	public void setLargeCard(CardData card) {
		mLargeCard = card;
	}

	public CardData getLargeCard() {
		return mLargeCard;
	}

	public void setCount(int count) {
		mCount = count;
	}

	public int getCount() {
		return mCount;
	}

	public void setFirstCard(CardData card) {
		mFirstCard = card;
	}

	public CardData getFirstCard() {
		return mFirstCard;
	}
}
