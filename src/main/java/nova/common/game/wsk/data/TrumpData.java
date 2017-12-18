package nova.common.game.wsk.data;



import java.util.ArrayList;

import nova.common.game.wsk.util.CardConstant;
import nova.common.game.wsk.util.CardUtil;


public class TrumpData {

	private int mTrumpColor = -1;
	private int mTrumpFace;
	private int mKingCount;
	private int mJCount;
	private int mTrumpFaceCount;
	private int mCount;
	
	public TrumpData(int face) {
		mTrumpFace = face;
	}
	
	public void updateData(ArrayList<CardData> datas) {
		mCount = datas.size();
		for (CardData data : datas) {
			if (data.getIndex() >= CardConstant.CARD_KING_BEGIN) {
				mKingCount++;
			} else if (CardUtil.isJCard(data.getIndex())) {
				mJCount++;
			} else {
				mTrumpColor = data.getColor();
				mTrumpFace = data.getFace();
				mTrumpFaceCount++;
			}
		}
	}
	
	public int getCount() {
		return mCount;
	}
	
	public int getKingCount() {
		return mKingCount;
	}
	
	public int getTrumpColor() {
		return mTrumpColor;
	}
	
	public void setTrumpFace(int face) {
		mTrumpFace = face;
	}
	
	public int getTrumpFace() {
		return mTrumpFace;
	}
}
