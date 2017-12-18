package com.nova.game.mahj.handler;

import java.util.HashMap;

import com.nova.game.model.ChannelManager;
import com.nova.server.domain.GameRequest;
import com.nova.server.domain.GameResponse;
import com.nova.server.handler.GameInitHandler;
import net.sf.json.JSONObject;
import nova.common.GameCommand;
import nova.common.game.mahjong.MahjGameManager;
import nova.common.game.mahjong.handler.FileRecorderManager;
import nova.common.game.mahjong.handler.GameLogger;
import nova.common.game.mahjong.util.MahjGameCommand;
import nova.common.game.mahjong.util.TestMahjConstant;
import nova.common.room.RoomController;
import nova.common.room.data.PlayerInfo;

public class MahjGameInitHandler implements GameInitHandler {
	private static final String TAG = "MahjGameInitHandler";
	private GameLogger mLogger;
	private RoomController mahjRoomController = RoomController.getInstance(GameCommand.MAHJ_TYPE_GAME);

	public MahjGameInitHandler() {
		GameLogger.create(new ServerLog("SERVER_MAHJ"));
		mLogger = GameLogger.getInstance();
		mLogger.i(TAG, "init GameLogger...");
		FileRecorderManager.getInstance().setFilePath("/home/zhangxx/SCE/mahj/data/");
		FileRecorderManager.getInstance().startRecord();
	}
	
	public void execute(GameRequest request, GameResponse response) {
		String message = request.readString();
		processGameMessage(message, request);
	}

	private void processGameMessage(String message, GameRequest request) {
		JSONObject json = JSONObject.fromObject(message);
		int playerId = json.getInt("player");
		int command = json.getInt("com");
		mLogger.e(TAG, "playerId : " + playerId + ", command : " + command);
		
		if (!ChannelManager.getInstance().hasChannel(playerId, request.getChannel())) {
			ChannelManager.getInstance().addChannel(playerId, request.getChannel());
		}
		
		switch (command) {
		case MahjGameCommand.REQUEST_ROOM_CREATE:
			processRoomCreate(playerId);
			break;
		case MahjGameCommand.REQUEST_ROOM_JOIN:
			processRoomJoin(playerId, json);
			break;
		case MahjGameCommand.REQUEST_ROOM_LEAVE:
			processRoomLeave(playerId, json);
			break;
		case MahjGameCommand.REQUEST_GAME_START:
			processTestDebug(json);
			processGameStart(playerId);
			break;
		case MahjGameCommand.REQUEST_GAME_ROOM_START:
			processGameStartByRoom(json.getInt("room"));
			break;
		case MahjGameCommand.REQUEST_GAME_STOP:
			processGameStop(playerId, json.getInt("room"));
			break;
		case MahjGameCommand.REQUEST_GAME_RESUME:
			processGameResume(playerId, json.getInt("room"));
			break;
		case MahjGameCommand.REQUEST_OUT_DATA:
			processOutData(playerId, json);
			break;
		case MahjGameCommand.REQUEST_OPERATE_DATA:
			processMatchData(playerId, json);
			break;
		default:
			break;
		}
	}
	
	private void processRoomCreate(int playerId) {
		PlayerInfo player = new PlayerInfo(playerId, "", "", 0);
		
		int roomId = mahjRoomController.createRoom(player);
		mLogger.d(TAG, "processRoomCreate, roomId = " + roomId);
		if (roomId >= 0) {
			new MahjServerRoomHandler().onRoomInfoChange(roomId, mahjRoomController.getRoomManager(roomId).getRoomInfo().getPlayers());
		}
	}
	
	private void processRoomJoin(int playerId, JSONObject json) {
		int roomId = json.getInt("room");
		PlayerInfo player = new PlayerInfo(playerId, "", "", 0);
		
		int result = mahjRoomController.joinRoom(roomId, player);
		mLogger.d(TAG, "processRoomJoin, roomId = " + roomId + ", result = " + result);
		new MahjServerRoomHandler().onRoomJoinResult(playerId, result);
		if (result >= 0) {
			new MahjServerRoomHandler().onRoomInfoChange(roomId, mahjRoomController.getRoomManager(roomId).getRoomInfo().getPlayers());
		}
	}
	
	private void processRoomLeave(int playerId, JSONObject json) {
		int roomId = json.getInt("room");
		PlayerInfo player = new PlayerInfo(playerId, "", "", 0);
		int result = mahjRoomController.leaveRoom(roomId, player);
		mLogger.e(TAG, "processRoomLeave, roomId = " + roomId + ", result = " + result);
		if (result >= 0) {
			new MahjServerRoomHandler().onRoomInfoChange(roomId, mahjRoomController.getRoomManager(roomId).getRoomInfo().getPlayers());
		}
	}
	
	private void processTestDebug(JSONObject json) {
		if (json.has("debug")) {
			int debugType = json.getInt("debug");
			TestMahjConstant.setDebug(debugType);
		} else {
			TestMahjConstant.cancelDebug();
		}
	}
	
	private void processGameStart(int playerId) {
		PlayerInfo player = new PlayerInfo(playerId, "", "", 0);
		int roomId = mahjRoomController.searchSuitableRoom(player);
		processGameStartByRoom(roomId, false);
	}
	
	private void processGameStartByRoom(int roomId) {
		processGameStartByRoom(roomId, true);
	}
	
	private void processGameStartByRoom(int roomId, boolean isRoomGame) {
		mLogger.d(TAG, "processGameStartByRoom, roomId = " + roomId + ", isRoomGame = " + isRoomGame + ", running = " 
	+ mahjRoomController.getRoomManager(roomId).getRoomInfo().isRunning());
		if (mahjRoomController.getRoomManager(roomId).getRoomInfo().isRunning()) {
		} else {
			if (isRoomGame) {
				new MahjServerRoomHandler().onRoomGameStartResult(roomId);
			}
			mahjRoomController.getRoomManager(roomId).setRoomHandler(new MahjServerRoomHandler());
			((MahjGameManager)mahjRoomController.getRoomManager(roomId).getGameManager()).setHandler(new MahjServerGameHandler());
			mahjRoomController.getRoomManager(roomId).startGame();
		}
	}
	
	private void processGameResume(int playerId, int roomId) {
		mLogger.d(TAG, "processGameResume, roomId = " + roomId + ", playerId = " + playerId);
		mahjRoomController.getRoomManager(roomId).resumeGame();
	}
	
	private void processGameStop(int playerId, int roomId) {
		HashMap<Integer, PlayerInfo> players = mahjRoomController.getRoomManager(roomId).getRoomInfo().getPlayers();
		if (players == null || players.size() <= 0) {
			return;
		}
		
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i).getId() == playerId) {
				players.get(i).setType(1);
			}
		}
	}
	
	private void processOutData(int playerId, JSONObject json) {
		int roomId = json.getInt("room");
		int playerIndex = mahjRoomController.getRoomManager(roomId).getRoomInfo().getIndexForPlayerId(playerId);
		int data = json.getInt("data");
		if (playerIndex >= 0) {
			((MahjGameManager)mahjRoomController.getRoomManager(roomId).getGameManager()).activeOutData(playerIndex, data);
		} else {
			mLogger.e(TAG, "processOutData, player " + playerId + " not in room " + roomId + "! result = " + playerIndex);
		}
	}
	
	private void processMatchData(int playerId, JSONObject json) {
		int roomId = json.getInt("room");
		int playerIndex = mahjRoomController.getRoomManager(roomId).getRoomInfo().getIndexForPlayerId(playerId);
		int operateType = json.getInt("operate");
		if (playerIndex >= 0) {
			((MahjGameManager)mahjRoomController.getRoomManager(roomId).getGameManager()).activeOperateData(playerIndex, operateType);
		} else {
			mLogger.e(TAG, "processMatchData, player " + playerId + " not in room " + roomId + "! result = " + playerIndex);
		}
	}
}
