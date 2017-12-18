package nova.common.game.wsk.util;

public class CardConstant {

	// 测试开关
	public final static boolean DEBUG = false;
	/**
	 * 1 : 只有玩家1可以叫主
	 * 2 : 4个玩家都可以叫主，反主
	 */
	public static int Debug_Type = 1;
	
	public final static int CARD_COUNT = 55;
	public final static int CARD_MAX_COUNT = 136;
	public final static int CARD_SINGLE_COUNT = 13;
	public final static int CARD_KING_COUNT = 2;
	public final static int CARD_DEFAULT = 0;
	public final static int CARD_FANG_A = 1;
	public final static int CARD_MEI_A = CARD_FANG_A + CARD_SINGLE_COUNT;
	public final static int CARD_TAO_A = CARD_MEI_A + CARD_SINGLE_COUNT;
	public final static int CARD_XIN_A = CARD_TAO_A + CARD_SINGLE_COUNT;
	public final static int CARD_KING_BEGIN = CARD_XIN_A + CARD_SINGLE_COUNT;
	public final static int CARD_FANG_J = CARD_FANG_A + 10;
	public final static int CARD_MEI_J = CARD_MEI_A + 10;
	public final static int CARD_TAO_J = CARD_TAO_A + 10;
	public final static int CARD_XIN_J = CARD_XIN_A + 10;
	
	public final static int CARD_WIDTH = 97;
	public final static int CARD_HEIGHT = 127;
	
	public final static int[] CARD_ELEMENTS = {
		1, 5, 8, 9, 10, 11, 12, 13,
		1, 5, 8, 9, 10, 11, 12, 13,
		1, 5, 8, 9, 10, 11, 12, 13,
		1, 5, 8, 9, 10, 11, 12, 13,
		14, 18, 21, 22, 23, 24, 25, 26,
		14, 18, 21, 22, 23, 24, 25, 26,
		14, 18, 21, 22, 23, 24, 25, 26,
		14, 18, 21, 22, 23, 24, 25, 26,
		27, 31, 34, 35, 36, 37, 38, 39,
		27, 31, 34, 35, 36, 37, 38, 39,
		27, 31, 34, 35, 36, 37, 38, 39,
		27, 31, 34, 35, 36, 37, 38, 39,
		40, 44, 47, 48, 49, 50, 51, 52,
		40, 44, 47, 48, 49, 50, 51, 52,
		40, 44, 47, 48, 49, 50, 51, 52,
		40, 44, 47, 48, 49, 50, 51, 52,
		53, 53, 53, 53,
		54, 54, 54, 54
	};
	
	// CARD_ELEMENTS_DEBUG_1 : 只有玩家1可以叫主
	public final static int[][] CARD_ELEMENTS_DEBUG_1 = {
		{53, 53, 53, 53, 5,  5,  5,  5,  11, 11, 11, 11, 1,  8,  9,  10, 12, 13, 1,  8,  9,  10, 12, 13, 1,  8,  9,  10, 12, 13, 1,  8},
		{14, 18, 21, 22, 23, 24, 25, 26, 14, 18, 21, 22, 23, 24, 25, 26, 14, 18, 21, 22, 23, 24, 25, 26, 14, 18, 21, 22, 23, 24, 25, 26},
		{27, 31, 34, 35, 36, 37, 38, 39, 27, 31, 34, 35, 36, 37, 38, 39, 27, 31, 34, 35, 36, 37, 38, 39, 27, 31, 34, 35, 36, 37, 38, 39},
		{40, 44, 47, 48, 49, 50, 51, 52, 40, 44, 47, 48, 49, 50, 51, 52, 40, 44, 47, 48, 49, 50, 51, 52, 40, 44, 47, 48, 49, 50, 51, 52},
		{54, 54, 54, 54, 9, 10, 12, 13},
	};
	
	// CARD_ELEMENTS_DEBUG_2 : 4个玩家都可以叫主，反主
	public final static int[][] CARD_ELEMENTS_DEBUG_2 = {
		{54, 53, 11, 5,  5,  18, 24, 24, 31, 37, 37, 1,  8,  9,  10, 12, 13, 1,  8,  9,  10, 12, 13, 1,  8,  9,  10, 12, 13, 1,  8,  9},
		{54, 53, 24, 24, 18, 18,  5, 11, 44, 50, 14, 21, 22, 23, 25, 26, 14, 21, 22, 23, 25, 26, 14, 21, 22, 23, 25, 26, 14, 21, 22, 23},
		{54, 53, 37, 37, 31, 31, 31, 5,  11, 18, 27, 34, 35, 36, 38, 39, 27, 34, 35, 36, 38, 39, 27, 34, 35, 36, 38, 39, 27, 34, 35, 36},
		{54, 53, 50, 50, 50, 44, 44, 44, 11, 40, 47, 48, 49, 51, 52, 40, 47, 48, 49, 51, 52, 40, 47, 48, 49, 51, 52, 40, 47, 48, 49, 51},
		{10, 12, 13, 25, 26, 38, 39, 52},
	};
}
