package nova.common.game.wsk.data;

import nova.common.game.wsk.util.CardConstant;
import nova.common.game.wsk.util.CardUtil;


public class CardData {

	// card index :
	// 0:默认
	// 1-13:方 14-26:梅 27-39:桃 40-52:心
	// 53,54:king
	private int index;
	// card color:
	// 0:方 1:梅 2:桃 3:心 4:king 
	private int color;
	// card face
	// 2,3,4,5,6,7,8,9,10,11(J),12(Q),13(K),14(A),15(小王),16(大王)
	private int face;
	// card level:
	// 0:普通色 1:主花色 2:普通主牌 3:主花色主牌 4:普通J 5:主花色J 6:小王 7:大王
	private int level;
	// card score:
	// 5:5 10,k:10
	private int score;
	
	public CardData() {
		
	}
	
	public CardData(int index, int trumpFace, int trumpColor) {
		this.index = index;
		initCardData(trumpFace, trumpColor);
	}
	
	public int getIndex() {
		return this.index;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	public int getColor() {
		return this.color;
	}
	
	public void setColor(int color) {
		this.color = color;
	}
	
	public int getFace() {
		return this.face;
	}
	
	public void setFace(int face) {
		this.face = face;
	}
	
	public int getLevel() {
		return this.level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
	
	public int getScore() {
		return this.score;
	}
	
	public void setScore(int score) {
		this.score = score;
	}
	
	public void updateLevel(int trumpFace, int trumpColor) {
		this.level = getCardLevel(this.index, trumpFace, trumpColor);
	}
	
	private void initCardData(int trumpFace, int trumpColor) {
		this.color = CardUtil.getCardColor(this.index);
		this.face = CardUtil.getCardFace(this.index);
		this.score = getCardScore(this.face);
		this.level = getCardLevel(this.index, trumpFace, trumpColor);
	}
	
	private int getCardScore(int face) {
		if (face == 5) {
			return 5;
		} else if (face == 10 || face == 13) {
			return 10;
		} else {
			return 0;
		}
	}
	
	private int getCardLevel(int index, int trumpFace, int trumpColor) {
		if (index == CardConstant.CARD_KING_BEGIN + 1) {
			return 7;
		} else if (index == CardConstant.CARD_KING_BEGIN) {
			return 6;
		} else if (CardUtil.isJCard(index)) {
			if (CardUtil.getCardColor(index) == trumpColor) {
				return 5;
			} else {
				return 4;
			}
		} else if (CardUtil.getCardFace(index) == trumpFace) {
			if (CardUtil.getCardColor(index) == trumpColor) {
				return 3;
			} else {
				return 2;
			}
		} else if (CardUtil.getCardColor(index) == trumpColor) {
			return 1;
		} else {
			return 0;
		}
	}
}
