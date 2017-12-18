package com.nova.game.wsk.handler;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import nova.common.game.wsk.data.CardData;
import nova.common.game.wsk.data.CardInfo;
import nova.common.game.wsk.data.GameInfo;
import nova.common.game.wsk.data.GameRoundInfo;
import nova.common.game.wsk.data.PlayerData;
import nova.common.game.wsk.data.PlayerInfo;
import nova.common.game.wsk.handler.GameHandler;
import nova.common.game.wsk.util.GameCommand;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.LoggerFactory;

public class WskGameHandler implements GameHandler {

	public void onGameStarted(int roomId) {
		WskInitHandler.sendMessage(roomId, GameCommand.COM_GAME_START, String.valueOf(roomId));
	}

	@Override
	public void onRoomInfoChange(int roomId, HashMap<Integer, PlayerInfo> players) {
		ArrayList<PlayerInfo> infos = new ArrayList<PlayerInfo>();
		for (int i = 0; i < 4; i++) {
			infos.add(players.get(i));
		}
		JSONArray playerInfos = JSONArray.fromObject(infos);
		JSONObject json = new JSONObject();
		json.put("room", roomId);
		json.put("players", playerInfos);
		LoggerFactory.getLogger("WskGameHandler").error(json.toString());
		WskInitHandler.sendMessage(roomId, GameCommand.COM_ROOM_INFO, json.toString());
	}

	@Override
	public void onGameInfoChange(int roomId, GameInfo gameInfo, GameRoundInfo roundInfo,
			HashMap<Integer, PlayerData> playerDatas, HashMap<Integer, ArrayList<CardData>> outcardDatas,
			ArrayList<CardData> lastDatas, ArrayList<CardData> outLastDatas) {
		JSONObject json = new JSONObject();
		JSONObject gameJson = JSONObject.fromObject(gameInfo);
		JSONObject roundJson = JSONObject.fromObject(roundInfo);
		JSONArray lastDataJson = JSONArray.fromObject(lastDatas);
		JSONArray outLastDataJson = JSONArray.fromObject(outLastDatas);
		ArrayList<CardInfo> infos = new ArrayList<CardInfo>();
		for (int i = 0; i < playerDatas.size(); i++) {
			CardInfo info = new CardInfo();
			info.setId(i);
			info.setData(playerDatas.get(i).getCardList());
			infos.add(info);
		}
		JSONArray playerDatasJson = JSONArray.fromObject(infos);
		json.put("game", gameJson);
		json.put("round", roundJson);
		json.put("last", lastDataJson);
		json.put("outlast", outLastDataJson);
		json.put("datas", playerDatasJson);
		WskInitHandler.sendMessage(roomId, GameCommand.COM_GET_GAME_INFO, json.toString());
	}
}
