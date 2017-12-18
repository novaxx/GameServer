package nova.common.game.wsk.data;

public class PlayerInfo {

	private int id;
	private String name;
	private String head;
	private int sex;
	// 0: player 1: android
	private int type;
	
	public PlayerInfo() {
		
	}
	
	public PlayerInfo(int id, String name, String head, int sex) {
		this.id = id;
		this.name = name;
		this.head = head;
		this.sex = sex;
	}
    
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setHead(String head) {
		this.head = head;
	}
	
	public String getHead() {
		return this.head;
	}
	
    public void setSex(int sex) {
        this.sex = sex;
    }
    
    public int getSex () {
        return sex;
    }
    
    public void setType(int type) {
    	this.type = type;
    }
    
    public int getType() {
    	return this.type;
    }
    
    public boolean isNormalPlayer() {
    	return this.type == 0;
    }
}
