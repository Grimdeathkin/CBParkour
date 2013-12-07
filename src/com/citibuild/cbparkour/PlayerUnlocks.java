package com.citibuild.cbparkour;

import java.util.ArrayList;

import org.bukkit.entity.Player;

public class PlayerUnlocks {
	
	private Player player;
	private String username;
	private ArrayList<String> unlocks = new ArrayList<String>();
	
	
	public PlayerUnlocks(Player p) {
		setPlayer(p);
		setUsername(p.getName().toLowerCase());
	}
	
	
	
	
	/*
	 * Get Functions
	 */
	
	public String getUsername() {
		return username;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public ArrayList<String> getUnlocks() {
		return unlocks;
	}
	
	
	
	/*
	 * Set Functions
	 */

	public void setUsername(String uName) {
		username = uName;
		
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public void setUnlocks(ArrayList<String> unlocks) {
		this.unlocks = unlocks;
	}

}
