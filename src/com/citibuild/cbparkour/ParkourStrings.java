package com.citibuild.cbparkour;

import org.bukkit.entity.Player;

public class ParkourStrings {
	
	Parkour pk;
	
	public String PREFIX;
	public String APREFIX;
	
	//Default colors
	public String defaultColor;
	public String defaultError;
	
	//Errors
	public String spawnNotSet;
	public String notUnlocked;
	public String disabled;
	public String invalidMap;
	public String invalidNumber;
	public String notInPK;
	public String noPermission;
	public String noPKPermission;
	public String notExist;
	public String startAtFirstCP; //StartAtFirst in config
	
	//In PK Messages
	public String welcome;
	public String leave;
	public String tpToLobby;
	public String timerStart;
	public String restartTimer;
	public String notInThisPK;
	public String firstFinish;
	public String globalRecord;
	public String beatTime;
	public String notBeatTime;
	public String finishCourse;
	public String newCP; //Checkpoint in config
	public String cpPrevReached; //CPReached in config
	public String cpMissing;
	public String newGlobalRecord;
	
	public ParkourStrings(Parkour plugin) {
		this.pk = plugin;
	}
	
	
	//Conversion Methods
	public String convertMapName(String message, int mapID) {
		message = message.replace("%MAPNAME", pk.getMapName(mapID));
		return message;
	}
	
	public String convertUsername(String message, Player player) {
		message = message.replace("%USERNAME", player.getDisplayName());
		return message;
	}
	
	public String convertTime(String message, Long time) {
		message = message.replace("%TIME", pk.convertTime(time));
		return message;
	}
	
	public String convertCheckpoint(String message, int checkpoint) {
		message = message.replace("%CHECKPOINT", String.valueOf(checkpoint));
		return message;
	}
	
	public String convertTotalCheckpoints(String message, int totalCP) {
		message = message.replace("%TOTALCHECKPOINTS", String.valueOf(totalCP));
		return message;
	}
	
	public String convertArgs(String message, String args) {
		message = message.replace("%ARGS", args);
		return message;
	}
	
	//Formatting
	public String addPrefix(String message, boolean admin) {
		if(admin) {
			message = pk.getAPrefix() + defaultColor + message;
		} else {
			message = pk.getPrefix() + defaultColor + message;
		}
		return message;
	}
	
	

}
