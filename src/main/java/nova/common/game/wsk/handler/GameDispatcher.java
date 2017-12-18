package nova.common.game.wsk.handler;


import java.util.ArrayList;

import nova.common.game.wsk.data.CardData;

public interface GameDispatcher {
	public void notOutCard(int playerId);
	public int activeOutCard(int playerId, ArrayList<CardData> datas);
	public int activeOutLastCard(int playerId, ArrayList<CardData> datas);
}
