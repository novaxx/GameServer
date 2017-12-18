package com.nova.game.wsk.handler;


import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nova.game.model.ChannelManager;
import com.nova.server.domain.GameRequest;
import com.nova.server.domain.GameResponse;
import com.nova.server.handler.GameInitHandler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import nova.common.game.wsk.data.CardData;
import nova.common.game.wsk.data.PlayerInfo;
import nova.common.game.wsk.room.RoomController;
import nova.common.game.wsk.util.GameCommand;

public class WskInitHandler implements GameInitHandler {
	protected Logger logger = LoggerFactory.getLogger(getClass());

	public void execute(GameRequest request, GameResponse response) {
		String message = request.readString();
		this.logger.error(message);
		processGameMessage(message, request);
	}
	
	private void processGameMessage(String message, GameRequest request) {
		JSONObject json = JSONObject.fromObject(message);
		int command = -1;
		int playerId = 0;
		int roomId = 0;
		if (json.has("com")) {
			command = json.getInt("com");
		}
		this.logger.error("zhangxx, command : " + command);
		
		switch (command) {
		// 快速游戏
		case GameCommand.COM_GAME_INIT:
			playerId = Integer.valueOf(json.getString("player"));
			if (!ChannelManager.getInstance().hasChannel(playerId, request.getChannel())) {
				ChannelManager.getInstance().addChannel(playerId, request.getChannel());
			}
			
			PlayerInfo player = getPlayerInfo(playerId);
			
			final int room = RoomController.getInstance().joinRoom(player);
			this.logger.error("zhangxx, room : " + room + ", running = " + RoomController.getInstance().getRoomManager(room).getRoomInfo().isRunning());
			if (RoomController.getInstance().getRoomManager(room).getRoomInfo().isRunning()) {
				new WskGameHandler().onRoomInfoChange(room, RoomController.getInstance().getRoomManager(room).getRoomInfo().getPlayers());
			} else {
				RoomController.getInstance().getRoomManager(room).setHandler(new WskGameHandler());
				RoomController.getInstance().getRoomManager(room).startGame();
			}
			new WskGameHandler().onGameStarted(room);
			break;

		case GameCommand.COM_ROOM_CREATE:
			// 创建房间
			playerId = Integer.valueOf(json.getString("player"));
			if (!ChannelManager.getInstance().hasChannel(playerId, request.getChannel())) {
				ChannelManager.getInstance().addChannel(playerId, request.getChannel());
			}
			
			PlayerInfo player2 = getPlayerInfo(playerId);
			
			int room_1100 = RoomController.getInstance().createRoom(player2);
			sendMessage(GameCommand.COM_ROOM_CREATE, String.valueOf(room_1100), request.getChannel());
			if (room_1100 >= 0) {
				new WskGameHandler().onRoomInfoChange(room_1100, RoomController.getInstance().getRoomManager(room_1100).getRoomInfo().getPlayers());
			}
			break;
			
		case GameCommand.COM_ROOM_JOIN:
			// 加入游戏
			playerId = Integer.valueOf(json.getString("player"));
			if (!ChannelManager.getInstance().hasChannel(playerId, request.getChannel())) {
				ChannelManager.getInstance().addChannel(playerId, request.getChannel());
			}
			roomId = Integer.valueOf(json.getString("room"));
			PlayerInfo player3 = getPlayerInfo(playerId);
			
			int room_1200 = RoomController.getInstance().joinRoom(roomId, player3);
			sendMessage(GameCommand.COM_ROOM_JOIN, String.valueOf(room_1200), request.getChannel());
			if (room_1200 >= 0) {
				new WskGameHandler().onRoomInfoChange(room_1200, RoomController.getInstance().getRoomManager(room_1200).getRoomInfo().getPlayers());
			}
			break;
			
		case GameCommand.COM_GAME_START:
			// 开始游戏
			roomId = json.getInt("room");
			if (RoomController.getInstance().getRoomManager(roomId).getRoomInfo().isRunning()) {
			} else {
				RoomController.getInstance().getRoomManager(roomId).setHandler(new WskGameHandler());
				RoomController.getInstance().getRoomManager(roomId).startGame();
			}
			new WskGameHandler().onRoomInfoChange(roomId, RoomController.getInstance().getRoomManager(roomId).getRoomInfo().getPlayers());
			new WskGameHandler().onGameStarted(roomId);
			break;
		
		case GameCommand.COM_PLAYER_REPLACE:
		// 交换位置
			roomId = json.getInt("room");
			int sp = json.getInt("sp");
			int tp = json.getInt("tp");
			RoomController.getInstance().getRoomManager(roomId).getRoomInfo().replacePlayer(sp, tp);
			new WskGameHandler().onRoomInfoChange(roomId, RoomController.getInstance().getRoomManager(roomId).getRoomInfo().getPlayers());
			break;
		
		case GameCommand.COM_GET_GAME_STATE:
		// 获取游戏状态
			playerId = Integer.valueOf(json.getString("player"));
			break;
			
		case GameCommand.COM_OUT_PORKER:
			roomId = json.getInt("room");
			playerId = json.getInt("player");
			ArrayList<CardData> datas = null;
			if (json.has("card")) {
				JSONArray array = JSONArray.fromObject(json.get("card"));
				datas = (ArrayList<CardData>)JSONArray.toList(array, CardData.class);
			}
			this.logger.error("zhangxx, json : " + json.toString());
			if (datas == null) {
				RoomController.getInstance().getRoomManager(roomId).getGameManager().notOutCard(playerId);
			} else {
				RoomController.getInstance().getRoomManager(roomId).getGameManager().activeOutCard(playerId, datas);
			}
			break;
		case GameCommand.COM_OUT_LAST_PORKER:
			roomId = json.getInt("room");
			playerId = json.getInt("player");
			JSONArray array = JSONArray.fromObject(json.get("card"));
			ArrayList<CardData> lastDatas = (ArrayList<CardData>)JSONArray.toList(array, CardData.class);
			if (lastDatas != null) {
				RoomController.getInstance().getRoomManager(roomId).getGameManager().activeOutLastCard(playerId, lastDatas);
			}
			break;
		default:
			break;
		}
	}
	
	public static void sendMessage(int roomId, int command, String message) {
		HashMap<Integer, PlayerInfo> players = RoomController.getInstance().getRoomManager(roomId).getRoomInfo().getPlayers();
		int onlinePlayer = 0;
		for (int i = 0; i < 4; i++) {
			if (players.get(i) == null) {
				continue;
			}
			
			int playerId = players.get(i).getId();
			if (ChannelManager.getInstance().getChannel(playerId) != null) {
				sendMessage(command, message, ChannelManager.getInstance().getChannel(playerId));
				onlinePlayer++;
			}
		}
		
		if (onlinePlayer <= 0) {
			RoomController.getInstance().getRoomManager(roomId).stopGame();
			RoomController.getInstance().cleanRoom(roomId);
		}
	}
	
	private static void sendMessage(int command, String message, Channel ch) {
		ByteBuf messageData = Unpooled.buffer();
		messageData.writeInt(command);
		messageData.writeInt(message.getBytes().length);
		messageData.writeBytes(message.getBytes());
		ch.writeAndFlush(messageData);
	}
	
	private static final HashMap<Integer, PlayerInfo> PLAYERS = new HashMap<Integer, PlayerInfo>();
	static {
		PLAYERS.put(6960, new PlayerInfo(6960, "lh", "1", 1));
		PLAYERS.put(2949, new PlayerInfo(2949, "zhangxx", "7", 1));
		PLAYERS.put(6223, new PlayerInfo(6223, "gionee", "10", 0));
		PLAYERS.put(6934, new PlayerInfo(6934, "wei", "11", 0));
	}
	
	private PlayerInfo getPlayerInfo(int playerId) {
		if (PLAYERS.containsKey(playerId)) {
			return PLAYERS.get(playerId);
		}
		
		return new PlayerInfo(1234, "snail", "0", 1);
	}
}
