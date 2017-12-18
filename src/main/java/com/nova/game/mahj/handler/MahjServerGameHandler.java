package com.nova.game.mahj.handler;

import com.nova.game.model.ChannelManager;
import net.sf.json.JSONObject;
import nova.common.GameCommand;
import nova.common.game.mahjong.data.MahjResponeData;
import nova.common.game.mahjong.handler.MahjGameHandler;
import nova.common.game.mahjong.util.MahjGameCommand;

public class MahjServerGameHandler implements MahjGameHandler {

	@Override
	public void onGameInfoChange(int roomId, MahjResponeData data) {
		JSONObject dataJson = JSONObject.fromObject(data);
		ChannelManager.getInstance().sendMessage(GameCommand.MAHJ_TYPE_GAME, roomId, MahjGameCommand.RESPONE_GAME_INFO_UPDATE, dataJson.toString());
	}
}
