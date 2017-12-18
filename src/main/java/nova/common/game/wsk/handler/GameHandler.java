package nova.common.game.wsk.handler;

import java.util.ArrayList;
import java.util.HashMap;

import nova.common.game.wsk.data.CardData;
import nova.common.game.wsk.data.GameInfo;
import nova.common.game.wsk.data.GameRoundInfo;
import nova.common.game.wsk.data.PlayerData;
import nova.common.game.wsk.data.PlayerInfo;

public interface GameHandler {
	public void onRoomInfoChange(int roomId, HashMap<Integer, PlayerInfo> players);

	public void onGameInfoChange(int roomId, 
			// 游戏信息
			GameInfo gameInfo, 
			// 游戏圈信息
			GameRoundInfo roundInfo, 
			// 玩家牌信息
			HashMap<Integer, PlayerData> playerDatas, 
			// 出牌信息
			HashMap<Integer, ArrayList<CardData>> outcardDatas, 
			// 底牌
			ArrayList<CardData> lastDatas, 
			// 扣底牌
			ArrayList<CardData> outLastDatas);
}
