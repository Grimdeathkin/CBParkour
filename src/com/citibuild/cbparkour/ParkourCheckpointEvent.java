package com.citibuild.cbparkour;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ParkourCheckpointEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
	private final Player player;
	private final int MapNumber;
	private final int CheckpointNumber;
	private final long time;
	
	public ParkourCheckpointEvent(Player player, int MapNumber, int CheckpointNumber, long time) {
		this.player = player;
		this.MapNumber = MapNumber;
		this.CheckpointNumber = CheckpointNumber;
		this.time = time;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public int getMapNumber() {
		return MapNumber;
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