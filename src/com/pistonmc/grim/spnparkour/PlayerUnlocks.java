package com.pistonmc.grim.spnparkour;

import java.util.ArrayList;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlayerUnlocks {
	
	private Player player;
	private OfflinePlayer oPlayer;
	private String username;
	private ArrayList<String> unlocks = new ArrayList<String>();
	
	
	public PlayerUnlocks(Player p) {
		setPlayer(p);
		setUsername(p.getName().toLowerCase());
	}
	
	public PlayerUnlocks(OfflinePlayer p) {
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
	
	public OfflinePlayer getOPlayer() {
		return oPlayer;
	}
	
	public ArrayList<String> getUnlocks() {
		if(unlocks != null) {
			return unlocks;
		} else {
			unlocks = new ArrayList<String>();
			unlocks.add("0");
			return unlocks;
		}
	}
	
	
	
	/*
	 * Set Functions
	 */

	public void setUsername(String uName) {
		username = uName;
		
	}

	public void setPlayer(Player p) {
		this.player = p;
	}
	
	public void setPlayer(OfflinePlayer p) {
		this.oPlayer = p;
	}

	public void setUnlocks(ArrayList<String> unlcks) {
		if(unlcks != null) {
			this.unlocks = unlcks;
			return;
		} else {
			unlcks = new ArrayList<String>();
			unlcks.add("0");
			this.unlocks = unlcks;
			return;
		}
	}

}
