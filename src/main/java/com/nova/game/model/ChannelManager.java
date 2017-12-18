package com.nova.game.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import nova.common.GameCommand;
import nova.common.room.RoomController;
import nova.common.room.data.PlayerInfo;

public class ChannelManager {
	private static final Object mLock = new Object();
	private static ChannelManager mInstance;
	private HashMap<Integer, Channel> mChannels = new HashMap<Integer, Channel>();
	
	
	private ChannelManager() {
		
	}
	
	public static ChannelManager getInstance() {
		synchronized (mLock) {
			if (mInstance == null) {
				mInstance = new ChannelManager();
			}
			
			return mInstance;
		}
	}
	
	public void addChannel(int playerId, Channel ch) {
		if (mChannels.get(playerId) == null) {
			mChannels.put(playerId, ch);
			return;
		}
		
		if (!mChannels.get(playerId).equals(ch)) {
			mChannels.remove(playerId);
			mChannels.put(playerId, ch);
			return;
		}
	}
	
	public void removeChannel(Channel ch) {
		Set set = mChannels.entrySet();
		Iterator it=set.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			if (((Channel)entry.getValue()).equals(ch)) {
				int playerId = (Integer)(entry.getKey());
				mChannels.remove(playerId);
				RoomController controller = RoomController.getInstance(GameCommand.MAHJ_TYPE_GAME);
				int roomId = controller.updateRoomManagerForPlayerOffline(playerId);
				if (!controller.getRoomManager(roomId).getRoomInfo().hasNormalPlayer()) {
					handleResultForNoPlayer(roomId);
				}
				return;
			}
		}
	}
	
	public Channel getChannel(int playerId) {
		return mChannels.get(playerId);
	}
	
	public boolean hasChannel(int playerId, Channel ch) {
		if (mChannels.get(playerId) == null) {
			return false;
		}
		
		return mChannels.get(playerId).equals(ch);
	}
	
	public void sendMessage(int gameType, int roomId, int command, String message) {
		if (!RoomController.getInstance(GameCommand.MAHJ_TYPE_GAME).getRoomManager(roomId).getRoomInfo().hasNormalPlayer()) {
			handleResultForNoPlayer(roomId);
			return;
		}
		
		HashMap<Integer, PlayerInfo> players = RoomController.getInstance(GameCommand.MAHJ_TYPE_GAME).getRoomManager(roomId).getRoomInfo().getPlayers();
		for (int i = 0; i < 4; i++) {
			if (players.get(i) == null) {
				continue;
			}
			
			int playerId = players.get(i).getId();
			if (ChannelManager.getInstance().getChannel(playerId) != null && players.get(i).isNormalPlayer()) {
				sendMessage(gameType, command, message, ChannelManager.getInstance().getChannel(playerId));
			}
		}
	}
	
	public void sendMessageByPlayerId(int gameType, int playerId, int command, String message) {
		if (ChannelManager.getInstance().getChannel(playerId) != null) {
			sendMessage(gameType, command, message, ChannelManager.getInstance().getChannel(playerId));
		}
	}
	
	private void sendMessage(int gameType, int command, String message, Channel ch) {
		ByteBuf messageData = Unpooled.buffer();
		messageData.writeInt(gameType);
		messageData.writeInt(command);
		messageData.writeInt(message.getBytes().length);
		messageData.writeBytes(message.getBytes());
		ch.writeAndFlush(messageData);
	}
	
	private void handleResultForNoPlayer(int roomId) {
		// 结束游戏
		RoomController.getInstance(GameCommand.MAHJ_TYPE_GAME).getRoomManager(roomId).stopGame();
		// 清空房间
		RoomController.getInstance(GameCommand.MAHJ_TYPE_GAME).cleanRoom(roomId);
	}
}
