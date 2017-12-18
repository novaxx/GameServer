package com.nova.game.mahj.handler;


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
import nova.common.GameCommand;
import nova.common.game.mahjong.handler.GameLogger;
import nova.common.game.mahjong.util.MahjGameCommand;
import nova.common.room.RoomController;
import nova.common.room.data.PlayerInfo;

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

public class MahjMessageInitHandler implements GameInitHandler {
	private GameLogger mLogger;
	private static final String TAG = "MahjMessageInitHandler";

	public MahjMessageInitHandler() {
		GameLogger.create(new ServerLog("SERVER_MAHJ"));
		mLogger = GameLogger.getInstance();
		mLogger.i(TAG, "init GameLogger...");
	}
	
	public void execute(GameRequest request, GameResponse response) {
		// type: 0-文本 1-语音 2-文本&语音
		int type = request.readInt();
		int room = request.readInt();
		int playerId = request.readInt();
		mLogger.e(TAG, "exeute, type = " + type + ", room = " + room + ", playerId = " + playerId);
		if (type == MahjGameCommand.MessageType.TYPE_MESSAGE) {
			processorReceiveText(room, playerId, request.readByte());
		} else if (type == MahjGameCommand.MessageType.TYPE_VOICE) {
			processorReceiveVoice(room, playerId, request.readByte());
		} else if (type == MahjGameCommand.MessageType.TYPE_MESSAGE_VOICE) {
			processorReceiveTextAndVoice(room, playerId, request.readByte());
		}
	}
	
	private void processorReceiveText(int room, int playerId, byte[] msg) {
		processorSendMessage(MahjGameCommand.MessageType.TYPE_MESSAGE, room, playerId, msg);
	}
	
	private void processorReceiveVoice(int room, int playerId, byte[] msg) {
		try {
			String filePath = "/home/zhangxx/SCE/mahj/sound/" + room + "_" + System.currentTimeMillis() + ".amr";
			FileOutputStream fos = new FileOutputStream(filePath);
			fos.write(msg);
			fos.close();
			processorSendMessage(MahjGameCommand.MessageType.TYPE_VOICE, room, playerId, msg);
		} catch (Exception e) {
			mLogger.e(TAG, e.toString());
		}
	}
	
	private void processorReceiveTextAndVoice(int room, int playerId, byte[] msg) {
		processorSendMessage(MahjGameCommand.MessageType.TYPE_MESSAGE_VOICE, room, playerId, msg);
	}
	
	// type: 0-文本 1-语音 2-文本&语音
	private void processorSendMessage(int type, int room, int playerId, byte[] msg) {
		HashMap<Integer, PlayerInfo> players = RoomController.getInstance(GameCommand.MAHJ_TYPE_GAME).getRoomManager(room).getRoomInfo().getPlayers();
		for (int i = 0; i < 4; i++) {
			if (players.get(i) == null) {
				continue;
			}
			
			int id = players.get(i).getId();
			if (playerId == id) {
				continue;
			}
			
			if (ChannelManager.getInstance().getChannel(id) != null) {
				ByteBuf bytebuf = Unpooled.buffer();
				bytebuf.writeInt(GameCommand.MAHJ_TYPE_MESSAGE);
				bytebuf.writeInt(getCommandIdByType(type));
				bytebuf.writeInt(playerId);
				bytebuf.writeInt(msg.length);
				bytebuf.writeBytes(msg);
				ChannelManager.getInstance().getChannel(id).writeAndFlush(bytebuf);
			}
		}
	}
	
	private int getCommandIdByType(int type) {
		if (type == MahjGameCommand.MessageType.TYPE_MESSAGE) {
			return MahjGameCommand.RESPONE_SEND_MESSAGE;
		} else if (type == 1) {
			return MahjGameCommand.RESPONE_SEND_VOICE;
		} else if (type == 2) {
			return MahjGameCommand.RESPONE_SEND_MESSAGE_VOICE;
		}
		
		return 9999;
	}
}
