package com.citibuild.cbparkour;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

public class ParkourVars {

	Parkour pk;

	// Used for parkour creation
	ArrayList<Location> newMapCheckpoints = new ArrayList<>();
	boolean newMap = false;
	public String newMapPlayerEditor = "";
	int CheckpointNumber = 0;
	int NewMapNumber = 0;
	String newMapName = null;
	int newMapPrevious = 0;
	int newMapNext = 0;

	// Options
	boolean removePotionsEffectsOnParkour = false;
	boolean BroadcastMessage = false;
	String BroadcastMsg = "&7&oPLAYER &aset a new record of &7&oTIME &aon &7&oMAPNAME";
	String PrefixString = "PK";
	boolean CheckpointEffect = true;
	boolean InvincibleWhileParkour = true;
	boolean FullHunger = false;
	boolean LastCheckpointTeleport = false;
	boolean rewardEnable = false;
	boolean rewardIfBetterScore = true;

	// Used for player parkour management
	Location lobby = null;
	public ArrayList<Integer> maps = new ArrayList<>();
	public HashMap<Integer, Boolean> toggleParkour = new HashMap<>(); // Parkour active or not
	HashMap<Location, String> cLoc = new HashMap<>(); // HashMap infos> Location : mapNumber_Checkpoint
	public HashMap<String, String> ParkourContainer = new HashMap<>(); // HashMap infos> playerName : mapNumber_parkour,StartTime_Chekcpoint
	HashMap<String, Long> Records = new HashMap<>(); // MapID:Player,Time
	HashMap<String, Long> rewardPlayersCooldown = new HashMap<>(); // HashMap infos> playerName:LastRewardTime

	public ArrayList<PlayerInfo> playerInfo = new ArrayList<PlayerInfo>();
	public HashMap<String, PlayerInfo> loadedUsers = new HashMap<>(); //HashMap infos> username, playerInfo

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


	public ParkourVars(Parkour plugin) {
		this.pk = plugin;
	}

}
