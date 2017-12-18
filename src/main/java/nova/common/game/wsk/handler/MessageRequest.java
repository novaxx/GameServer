package nova.common.game.wsk.handler;

public class MessageRequest {

	private int mRoomId;
	private String mBody;
	private String mTime;
	
	public MessageRequest(int roomId, String time, String body) {
		mRoomId = roomId;
		mBody = body;
		mTime = time;
	}
	
	public int getRoom() {
		return mRoomId;
	}
	
	public String getBody() {
		return mBody;
	}
	
	public String getTime() {
		return mTime;
	}
}
