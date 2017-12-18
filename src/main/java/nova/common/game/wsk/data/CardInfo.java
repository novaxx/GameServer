package nova.common.game.wsk.data;


import java.util.ArrayList;

public class CardInfo {
	private int id;
	private ArrayList<Integer> data;
	
	public CardInfo() {
		this.data = new ArrayList<Integer>();
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setData(ArrayList<CardData> datas) {
		for (CardData data : datas) {
			this.data.add(data.getIndex());
		}
	}
	
	public ArrayList<Integer> getData() {
		return this.data;
	}
}
