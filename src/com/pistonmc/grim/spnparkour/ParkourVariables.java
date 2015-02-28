package com.pistonmc.grim.spnparkour;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

public class ParkourVariables {

	Parkour pk;

	// Used for parkour creation
	private ArrayList<Location> newMapCheckpoints = new ArrayList<>();
	private boolean newMap = false;
	public String newMapPlayerEditor = "";
	private int CheckpointNumber = 0;
	private int NewMapNumber = 0;
	private String newMapName = null;
	public int newMapPrevious = 0;
	private int newMapNext = 0;

	// Options
	private boolean removePotionsEffectsOnParkour = false;
	private boolean BroadcastMessage = false;
	private String BroadcastMsg = "&7&oPLAYER &aset a new record of &7&oTIME &aon &7&oMAPNAME";
	String PrefixString = "PK";
	private boolean CheckpointEffect = true;
	private boolean InvincibleWhileParkour = true;
	private boolean FullHunger = false;
	boolean LastCheckpointTeleport = false;
	boolean rewardEnable = false;
	private boolean rewardIfBetterScore = true;
	public ArrayList<String> allowedCommands = new ArrayList<String>();

	// Used for player parkour management
	private Location lobby = null;
	public ArrayList<Integer> maps = new ArrayList<>();
	public HashMap<Integer, Boolean> toggleParkour = new HashMap<>(); // Parkour active or not
	private HashMap<Location, String> cLoc = new HashMap<>(); // HashMap infos> Location : mapNumber_Checkpoint
	public HashMap<String, String> ParkourContainer = new HashMap<>(); // HashMap infos> playerName : mapNumber_parkour,StartTime_Chekcpoint
	private HashMap<String, Long> Records = new HashMap<>(); // MapID:Player,Time
	public HashMap<String, Long> rewardPlayersCooldown = new HashMap<>(); // HashMap infos> playerName:LastRewardTime

	public ArrayList<PlayerInfo> playerInfo = new ArrayList<PlayerInfo>();
	public HashMap<String, PlayerInfo> loadedUsers = new HashMap<>(); //HashMap infos> username, playerInfo
	public HashMap<String, PlayerUnlocks> loadedPUnlocks = new HashMap<>(); //HasMap infos> username, playerUnlocks

	// Used for saving/loading scores and Player status
	public String configPath = "plugins" + File.separator + "CBParkour" + File.separator;
	public String scoresPath = configPath + "PlayersScores.scores";
	public File scores = new File(scoresPath);

	//PlayerInfo Files
	public String playerInfoPath = configPath + "PlayerInfo.yml";
	public File playerInfoFile = new File(playerInfoPath);
	public FileConfiguration playerInfoConfig = null;

	//Strings.yml Files
	public String stringsPath = configPath + "Strings.yml";
	public File stringsFile = new File(stringsPath);
	public FileConfiguration stringsConfig = null;


	public ParkourVariables(Parkour plugin) {
		this.pk = plugin;
	}


	public Location getLobby() {
		return lobby;
	}


	public void setLobby(Location lobby) {
		this.lobby = lobby;
	}


	public HashMap<String, Long> getRecords() {
		return Records;
	}


	public void setRecords(HashMap<String, Long> records) {
		Records = records;
	}


	public boolean isNewMap() {
		return newMap;
	}


	public void setNewMap(boolean newMap) {
		this.newMap = newMap;
	}


	public int getCheckpointNumber() {
		return CheckpointNumber;
	}


	public void setCheckpointNumber(int checkpointNumber) {
		CheckpointNumber = checkpointNumber;
	}


	public String getNewMapName() {
		return newMapName;
	}


	public void setNewMapName(String newMapName) {
		this.newMapName = newMapName;
	}


	public int getNewMapNext() {
		return newMapNext;
	}


	public void setNewMapNext(int newMapNext) {
		this.newMapNext = newMapNext;
	}


	public ArrayList<Location> getNewMapCheckpoints() {
		return newMapCheckpoints;
	}


	public void setNewMapCheckpoints(ArrayList<Location> newMapCheckpoints) {
		this.newMapCheckpoints = newMapCheckpoints;
	}


	public int getNewMapNumber() {
		return NewMapNumber;
	}


	public void setNewMapNumber(int newMapNumber) {
		NewMapNumber = newMapNumber;
	}


	public HashMap<Location, String> getcLoc() {
		return cLoc;
	}


	public void setcLoc(HashMap<Location, String> cLoc) {
		this.cLoc = cLoc;
	}


	public boolean isFullHunger() {
		return FullHunger;
	}


	public void setFullHunger(boolean fullHunger) {
		FullHunger = fullHunger;
	}


	public boolean isCheckpointEffect() {
		return CheckpointEffect;
	}


	public void setCheckpointEffect(boolean checkpointEffect) {
		CheckpointEffect = checkpointEffect;
	}


	public boolean isRemovePotionsEffectsOnParkour() {
		return removePotionsEffectsOnParkour;
	}


	public void setRemovePotionsEffectsOnParkour(
			boolean removePotionsEffectsOnParkour) {
		this.removePotionsEffectsOnParkour = removePotionsEffectsOnParkour;
	}


	public boolean isBroadcastMessage() {
		return BroadcastMessage;
	}


	public void setBroadcastMessage(boolean broadcastMessage) {
		BroadcastMessage = broadcastMessage;
	}


	public String getBroadcastMsg() {
		return BroadcastMsg;
	}


	public void setBroadcastMsg(String broadcastMsg) {
		BroadcastMsg = broadcastMsg;
	}


	public boolean isRewardIfBetterScore() {
		return rewardIfBetterScore;
	}


	public void setRewardIfBetterScore(boolean rewardIfBetterScore) {
		this.rewardIfBetterScore = rewardIfBetterScore;
	}


	public boolean isInvincibleWhileParkour() {
		return InvincibleWhileParkour;
	}


	public void setInvincibleWhileParkour(boolean invincibleWhileParkour) {
		InvincibleWhileParkour = invincibleWhileParkour;
	}

}
