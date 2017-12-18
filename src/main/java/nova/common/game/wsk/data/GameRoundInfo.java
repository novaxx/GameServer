package nova.common.game.wsk.data;

import java.util.ArrayList;

import nova.common.game.wsk.util.CardUtil;

public class GameRoundInfo {

	private int room;
	private int larger;
	private int current;
	private int securrent = -1;
	private int first;
	private int stage;
	private int secstage;

	private ArrayList<CardInfo> info;
	
	public void setRoomId(int room) {
		this.room = room;
	}
	
	public int getRoomId() {
		return this.room;
	}
	
	public void setLarger(int id) {
		larger = id;
	}
	
	public int getLarger() {
		return larger;
	}
	
	public void setCurrent(int id) {
		current = id;
		if (current >= 4) {
			current -= 4;
		}
	}
	
	public int getCurrent() {
		return this.current;
	}
	
	public void setSecurrent(int securrent) {
		this.securrent = securrent;
		if (this.securrent >= 4) {
			this.securrent -= 4;
		}
	}
	
	public int getSecurrent() {
		return this.securrent;
	}
	
	public void setFirst(int first) {
		this.first = first;
	}
	
	public int getFirst() {
		return this.first;
	}
	
	public void setStage(int stage) {
		this.stage = stage;
	}
	
	public int getStage() {
		return this.stage;
	}
	
	public void setSecStage(int stage) {
		this.secstage = stage;
	}
	
	public int getSecStage() {
		return this.secstage;
	}
	
	public void setInfo(ArrayList<CardInfo> info) {
		this.info = info;
	}
	
	public ArrayList<CardInfo> getInfo() {
		return this.info;
	}
	
	public String toString() {
		String msg = "First-" + first
				+ " Current-" + current
				+ " Securrent-" + securrent
				+ " Large-" + larger
				+ " Stage-" + stage
				+ " SecStage" + secstage;
		
		msg = msg + "; OUT:";
		
		for (int i = 0; i < info.size(); i++) {
			msg = msg + ", " + i + "=";
			for (Integer data : info.get(i).getData()) {
				msg = msg + CardUtil.getCardName(data) + " ";
			}
		}

		return msg;
	}
}
