package com.citibuild.cbparkour;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;

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
		public HashMap<String, String> ParkourContainer = new HashMap<>(); // HashMap infos> playerName :
																			// mapNumber_parkourStartTime_Chekcpoint
		HashMap<String, Long> Records = new HashMap<>(); // Map:Player, Time
		HashMap<String, Long> rewardPlayersCooldown = new HashMap<>(); // HashMap infos> playerName :
																					// LastRewardTime

		// Used for saving/loading scores
		String path = "plugins" + File.separator + "CBParkour" + File.separator + "PlayersScores.scores";
		File scores = new File(path);

		// Chat colours
		public ChatColor BLACK = ChatColor.BLACK;				//\u00A70
		public ChatColor D_BLUE = ChatColor.DARK_BLUE;			//\u00A71
		public ChatColor D_GREEN = ChatColor.DARK_GREEN;		//\u00A72
		public ChatColor D_AQUA = ChatColor.DARK_AQUA;			//\u00A73
		public ChatColor D_RED = ChatColor.DARK_RED;			//\u00A74
		public ChatColor D_PURPLE = ChatColor. DARK_PURPLE;	//\u00A75
		public ChatColor GOLD = ChatColor.GOLD;				//\u00A76
		public ChatColor D_GRAY = ChatColor.DARK_GRAY;			//\u00A77
		public ChatColor GRAY = ChatColor.GRAY;				//\u00A78
		public ChatColor BLUE = ChatColor.BLUE;				//\u00A79
		public ChatColor GREEN = ChatColor.GREEN;				//\u00A7a
		public ChatColor AQUA = ChatColor.AQUA;				//\u00A7b
		public ChatColor RED = ChatColor.RED;					//\u00A7c
		public ChatColor LIGHT_PURPLE = ChatColor.LIGHT_PURPLE;//\u00A7d
		public ChatColor YELLOW = ChatColor.YELLOW;			//\u00A7e
		public ChatColor WHITE = ChatColor.WHITE;				//\u00A7f	
		// Chat effects
		public ChatColor BOLD = ChatColor.BOLD;				//\u00A7l
		public ChatColor STRIKE = ChatColor.STRIKETHROUGH;		//\u00A7m
		public ChatColor ULINE = ChatColor.UNDERLINE;			//\u00A7n
		public ChatColor ITALIC = ChatColor.ITALIC;			//\u00A7o
		public ChatColor RESET = ChatColor.RESET;				//\u00A7r
		// Prefixes, user and admin
		public String PREFIX;
		public String APREFIX;
		
		//GameMode Variable
		public GameMode prePKGM = GameMode.SURVIVAL;
	
	public ParkourVars(Parkour plugin) {
		this.pk = plugin;
		PREFIX = (GRAY+ "[" + D_AQUA + PrefixString + GRAY + "] ");
		APREFIX = (GRAY+ "[" + RED + PrefixString + GRAY + "] ");
	}

}
