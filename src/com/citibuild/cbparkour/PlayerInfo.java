package com.citibuild.cbparkour;

import org.bukkit.GameMode;

public class PlayerInfo {
	
	String username;
	Long time;
	int mapID;
	int checkpoint;
	GameMode prevGM;
	boolean setPrevGM;
	
	/*
	 * Get methods
	 */
	
	public String getUsername() {
		return username;
	}
	
	public Long getTime() {
		return time;
	}
	
	public int getMapID() {
		return mapID;
	}
	
	public int getCheckpoint() {
		return checkpoint;
	}
	
	public GameMode getPrevGM() {
		if(prevGM == null) {
			return GameMode.ADVENTURE;
		} else {
			return prevGM;
		}
	}
	
	public boolean isPrevGMSet() {
		return setPrevGM;
	}
	
	/*
	 * Set methods
	 */
	
	public void setUsername(String iUsername) {
		username = iUsername;
	}
	
	public void setTime(Long iTime) {
		time = iTime;
	}
	
	public void setMapID(int iMapID) {
		mapID = iMapID;
	}
	
	public void setCheckpoint(int iCheckpoint) {
		checkpoint = iCheckpoint;
	}
	
	public void setPrevGM(GameMode gm) {
		prevGM = gm;
	}
	
	public void setPrevGMSet(boolean value) {
		setPrevGM = value;
	}

}
