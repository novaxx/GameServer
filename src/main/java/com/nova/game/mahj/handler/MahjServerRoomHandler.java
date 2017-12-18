package com.nova.game.mahj.handler;

import java.util.ArrayList;
import java.util.HashMap;

import com.nova.game.model.ChannelManager;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import nova.common.GameCommand;
import nova.common.game.mahjong.util.MahjGameCommand;
import nova.common.room.data.PlayerInfo;
import nova.common.room.handler.RoomHandler;

public class MahjServerRoomHandler implements RoomHandler {

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
		ChannelManager.getInstance().sendMessage(GameCommand.MAHJ_TYPE_GAME, roomId, MahjGameCommand.RESPONE_ROOM_INFO_UPDATE, json.toString());
	}

	public void onRoomJoinResult(int playerId, int result) {
		JSONObject json = new JSONObject();
		json.put("result", result >= 0 ? MahjGameCommand.RoomState.ROOM_STATE_JOIN : MahjGameCommand.RoomState.ROOM_STATE_FAIL);
		json.put("reson", getResultReson(result));
		ChannelManager.getInstance().sendMessageByPlayerId(GameCommand.MAHJ_TYPE_GAME, playerId, MahjGameCommand.RESPONE_ROOM_STATE_UPDATE, json.toString());
	}
	
	public void onRoomGameStartResult(int roomId) {
		JSONObject json = new JSONObject();
		json.put("result", MahjGameCommand.RoomState.ROOM_STATE_GAME_START);
		json.put("reson", "开始游戏");
		ChannelManager.getInstance().sendMessage(GameCommand.MAHJ_TYPE_GAME, roomId, MahjGameCommand.RESPONE_ROOM_STATE_UPDATE, json.toString());
	}
	
	private String getResultReson(int result) {
		String reson = "";
		if (result >= 0) {
			reson = "加入成功";
		} else if (result == -1) {
			reson = "没有该房间";
		} else if (result == -2) {
			reson = "房间已满";
		} else if (result == -3) {
			reson = "房间游戏已开始";
		}
		
		return reson;
	}
}
