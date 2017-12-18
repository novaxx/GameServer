package nova.common.game.wsk.data;

import nova.common.game.wsk.util.CardUtil;

public class GameInfo {
	// 第一局游戏
	private boolean firstGame = true;
	// 本局庄家
	private int bankerId = 0;
	// 本局主牌花色
	private int trumpColor = -1;
	// 本局主牌大小
	private int trumpFace = 5;
	// 获得的分数
	private int score = 0;
	// 进贡张数
	private int tributeCount = 0;
	
	public boolean isFirstGame() {
		return this.firstGame;
	}
	
	public void setFirstGame(boolean isFirst) {
		this.firstGame = isFirst;
	}
	
	public void setBankerId(int playerId) {
		this.bankerId = playerId;
	}
	
	public int getBankerId() {
		return this.bankerId;
	}
	
	public int getTrumpColor() {
		return this.trumpColor;
	}
	
	public void setTrumpColor(int color) {
		this.trumpColor = color;
	}
	
	public boolean isTrumped() {
		return this.trumpColor >= 0;
	}
	
	public void setTrumpFace(int face) {
		this.trumpFace = face;
	}
	
	public int getTrumpFace() {
		return this.trumpFace;
	}
	
	public void setScore(int score) {
		this.score = score;
	}
	
	public void addScore(int score) {
		this.score += score;
	}
	
	public int getScore() {
		return this.score;
	}
	
	public void setTributeCount(int count) {
		this.tributeCount = count;
	}
	
	public int getTributeCount() {
		return this.tributeCount;
	}
	
	public boolean isGameWin(int playerId) {
		if (isFriendBanker(playerId)) {
			return !isUpAchievedScore(this.score);
		} else {
			return isUpAchievedScore(this.score);
		}
	}
	
	public void handlerResult() {
		if (isUpAchievedScore(this.score)) {
			this.bankerId++;
			this.trumpFace = 5;
		} else {
			int currentTrumpFace = this.trumpFace;
			if (currentTrumpFace == 5) {
				this.trumpFace = 10;
			} else if (currentTrumpFace == 10) {
				this.trumpFace = 13;
			} else if (currentTrumpFace == 13) {
				this.trumpFace = 14;
			} else if (currentTrumpFace == 14) {
				this.trumpFace = 5;
			}
			
			this.bankerId += 2;
		}
		
		if (this.bankerId >= 4) {
			this.bankerId = this.bankerId - 4;
		}
		
		this.tributeCount = getTributeCount(this.score);
	}
	
	private int getTributeCount(int score) {
		int tributeCount = 0;
		if (isTrumped()) {
			tributeCount = Math.abs(score - 160) / 40;
		} else {
			tributeCount = Math.abs(score - 200) / 50;
		}
		
		/*
		 * 最多进4贡
		 * */
		if (tributeCount > 4) {
			tributeCount = 4;
		}
		
		return tributeCount;
	}
	
	private boolean isFriendBanker(int playerId) {
		return Math.abs(this.bankerId - playerId) % 2 == 0;
	}
	
	private boolean isUpAchievedScore(int score) {
		if (isTrumped()) {
			return score >= 160;
		} else {
			return score >= 200;
		}
	}
	
	public String toString() {
		return "庄家-" + bankerId
				+ " 主牌-" + trumpFace
				+ " 主花-" + CardUtil.getColorName(trumpColor)
				+ " 分数-" + score;
	}
}
