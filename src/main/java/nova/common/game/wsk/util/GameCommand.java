package nova.common.game.wsk.util;

public class GameCommand {

	public static final int COM_GAME_MESSAGE = 102;
	public static final int COM_OTHER_MESSAGE = 1001;
	
	// 获取房间信息
	public static final int COM_ROOM_INFO = 1000;
	// 创建房间
	public static final int COM_ROOM_CREATE = 1001;
	// 加入房间
	public static final int COM_ROOM_JOIN = 1002;
	
	// 交换位置
	public static final int COM_PLAYER_REPLACE = 1102;
	
	// 初始化游戏
	public static final int COM_GAME_INIT = 1200;
	// 开始游戏
	public static final int COM_GAME_START = 1201;
	// 获取游戏信息
	public static final int COM_GET_GAME_INFO = 1202;
	// 获取游戏状态
	public static final int COM_GET_GAME_STATE = 1203;

	
	// 扣底牌
	public static final int COM_OUT_LAST_PORKER = 1301;
	// 出牌
	public static final int COM_OUT_PORKER = 1302;
}
