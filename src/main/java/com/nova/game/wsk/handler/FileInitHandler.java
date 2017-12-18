package com.nova.game.wsk.handler;


import java.io.FileOutputStream;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nova.game.model.ChannelManager;
import com.nova.server.domain.GameRequest;
import com.nova.server.domain.GameResponse;
import com.nova.server.handler.GameInitHandler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import nova.common.game.wsk.data.PlayerInfo;
import nova.common.game.wsk.room.RoomController;

class ByteHelp
{
	public static byte[] IntToBytes(int a)
	{
		byte[] bytes = new byte[4];
		for (int i = 0; i < bytes.length; i++)
		{
			bytes[i] = (byte) ((a >> (i * 8)) & 0xff);
		}
		return bytes;
	}

	public static int BytesToInt(byte[] bbb)
	{
		int a = 0;
		for (int i = 0; i < bbb.length; i++)
		{
			int temp = bbb[i];
			a += (temp & 0xff) << (i * 8);
		}
		return a;
	}
}

public class FileInitHandler implements GameInitHandler {
	protected Logger logger = LoggerFactory.getLogger(getClass());

	public void execute(GameRequest request, GameResponse response) {
		// type: 0-文本 1-语音 2-文本&语音
		int type = request.readInt();
		int room = request.readInt();
		int playerId = request.readInt();
		logger.error("zhangxx, type = " + type + ", room = " + room + ", playerId = " + playerId);
		if (type == 0) {
			processorReceiveText(room, playerId, request.readByte());
		} else if (type == 1) {
			processorReceiveVoice(room, playerId, request.readByte());
		} else if (type == 2) {
			processorReceiveTextAndVoice(room, playerId, request.readByte());
		}
	}
	
	private void processorReceiveText(int room, int playerId, byte[] msg) {
		processorSendMessage(0, room, playerId, msg);
	}
	
	private void processorReceiveVoice(int room, int playerId, byte[] msg) {
		try {
			String filePath = "/home/zhangxx/SCE/wsk/sound/" + room + "_" + System.currentTimeMillis() + ".amr";
			FileOutputStream fos = new FileOutputStream(filePath);
			fos.write(msg);
			fos.close();
			processorSendMessage(1, room, playerId, msg);
		} catch (Exception e) {
			this.logger.error(e.toString());
		}
	}
	
	private void processorReceiveTextAndVoice(int room, int playerId, byte[] msg) {
		processorSendMessage(2, room, playerId, msg);
	}
	
	// type: 0-文本 1-语音 2-文本&语音
	private void processorSendMessage(int type, int room, int id, byte[] msg) {
		HashMap<Integer, PlayerInfo> players = RoomController.getInstance().getRoomManager(room).getRoomInfo().getPlayers();
		for (int i = 0; i < 4; i++) {
			if (players.get(i) == null) {
				continue;
			}
			
			int playerId = players.get(i).getId();
			if (playerId == id) {
				continue;
			}
			if (ChannelManager.getInstance().getChannel(playerId) != null) {
				ByteBuf bytebuf = Unpooled.buffer();
				bytebuf.writeInt(getCommandIdByType(type));
				bytebuf.writeInt(id);
				bytebuf.writeInt(msg.length);
				bytebuf.writeBytes(msg);
				ChannelManager.getInstance().getChannel(playerId).writeAndFlush(bytebuf);
			}
		}
	}
	
	private int getCommandIdByType(int type) {
		if (type == 0) {
			return 9000;
		} else if (type == 1) {
			return 9001;
		} else if (type == 2) {
			return 9002;
		}
		
		return 9999;
	}
}
