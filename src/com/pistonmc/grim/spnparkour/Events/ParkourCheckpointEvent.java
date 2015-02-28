package com.pistonmc.grim.spnparkour.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.pistonmc.grim.spnparkour.Parkour;

public class ParkourCheckpointEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private final Player player;
	private final int mapID;
	private final int CheckpointNumber;
	private final long time;
	
	public ParkourCheckpointEvent(Parkour plugin, Player player, int mapID, int CheckpointNumber, long time) {
		this.player = player;
		this.mapID = mapID;
		this.CheckpointNumber = CheckpointNumber;
		this.time = time;
		
		plugin.pkVars.loadedUsers.get(player.getName()).setMapID(mapID);
		plugin.pkVars.loadedUsers.get(player.getName()).setTime(time);
		plugin.pkVars.loadedUsers.get(player.getName()).setCheckpoint(CheckpointNumber + 1);
		plugin.pkFuncs.savePlayerInfo(player);
		
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public int getMapNumber() {
		return mapID;
	}
	
	public int getCheckpointNumber() {
		return CheckpointNumber;
	}
	
	public long getTime() {
		return time;
	}
	
    @Override
	public HandlerList getHandlers() {
	    return handlers;
	}
	 
	public static HandlerList getHandlerList() {
	    return handlers;
	}
}