package nova.common.game.wsk.util;

import nova.common.game.wsk.data.CardData;


public class CardUtil {

	public static String getColorName(int color) {
		String[] name = {"方", "梅", "桃", "心"};
		if (color >= 0 && color < 4) {
			return name[color];
		} else {
			return "无";
		}
	}
	
	public static String getCardName(int index) {
		String[] colorName = {"方", "梅", "桃", "心"};
		String[] faceName = {"-", "-", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A", "小王", "大王"};
		int face = getCardFace(index);
		if (face > 14) {
			return faceName[face];
		} else {
			int color = getCardColor(index);
			return colorName[color] + faceName[face];
		}
	}
	
	public static boolean isJCard(int index) {
		boolean isJ = (index == CardConstant.CARD_FANG_J 
				|| index == CardConstant.CARD_TAO_J
				|| index == CardConstant.CARD_MEI_J
				|| index == CardConstant.CARD_XIN_J);
		
		return isJ;
	}
	
	public static boolean isACard(int index) {
		boolean isA = (index == CardConstant.CARD_FANG_A 
				|| index == CardConstant.CARD_TAO_A
				|| index == CardConstant.CARD_MEI_A
				|| index == CardConstant.CARD_XIN_A);
		
		return isA;
	}
	
	public static boolean isKingCard(int index) {
		return index >= CardConstant.CARD_KING_BEGIN;
	}
	
	public static boolean is10Card(int index) {
		boolean is10 = (index == CardConstant.CARD_FANG_J - 1
				|| index == CardConstant.CARD_TAO_J - 1
				|| index == CardConstant.CARD_MEI_J - 1
				|| index == CardConstant.CARD_XIN_J - 1);
		
		return is10;
	}
	
	public static boolean isQCard(int index) {
		boolean isQ = (index == CardConstant.CARD_FANG_J + 1
				|| index == CardConstant.CARD_TAO_J + 1
				|| index == CardConstant.CARD_MEI_J + 1
				|| index == CardConstant.CARD_XIN_J + 1);
		
		return isQ;
	}
	
	public static boolean isTrumpCard(int index, int trumpFace) {
		boolean isTrump = (index == CardConstant.CARD_FANG_A + trumpFace - 1
				|| index == CardConstant.CARD_MEI_A + trumpFace - 1
				|| index == CardConstant.CARD_TAO_A + trumpFace - 1
				|| index == CardConstant.CARD_XIN_A + trumpFace - 1);
		
		return isTrump;
	}
	
	public static int getCardColor(int index) {
		return (index - 1) / CardConstant.CARD_SINGLE_COUNT;
	}
	
	public static int getCardFace(int index) {
		if (index == CardConstant.CARD_KING_BEGIN + 1) {
			return 16;
		}
		
        if (index == CardConstant.CARD_KING_BEGIN) {
			return 15;
		}
		
		int temp = (index - 1) % CardConstant.CARD_SINGLE_COUNT;
		int face = (temp == 0 ? CardConstant.CARD_SINGLE_COUNT + 1 : temp + 1);
		return face;
	}
	
	public static boolean isLarge(CardData data1, CardData data2) {
		
		if (data2.getLevel() != data1.getLevel()) {
			return data2.getLevel() > data1.getLevel();
		} else if (data2.getColor() ==  data1.getColor()) {
			return data2.getFace() > data1.getFace();
		}
		
		return false;
	}
}
