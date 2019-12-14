package be.noki_senpai.NKjobs.managers;

import be.noki_senpai.NKjobs.NKjobs;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager
{
	private ConsoleCommandSender console = null;
	private FileConfiguration config = null;

	private String dbHost = null;
	private int dbPort = 3306;
	private String dbName = null;
	private String dbUser = null;
	private String dbPassword = null;

	public static String PREFIX = null;
	public static String SERVERNAME = null;
	public static int MAXJOB = -1;
	public static String FROMSPAWNER = "NKjobs_from_spawner";
	public static String HITPIGMEN = "NKjobs_hit_pigmen";
	public static double RATIOFROMSPAWNER = 0.40;
	public static double MINEXPLOREMONEY = 0.10;
	public static double MINEXPLOREEXP = 0.10;
	public static int GLOBALBLOCKSTIMER = 5;
	public static int MAXREGISTEREDFURNACES = 16;
	public static int MAXREGISTEREDBREWINGSTANDS = 16;

	// Constructor
	public ConfigManager(FileConfiguration config)
	{
		this.console = Bukkit.getConsoleSender();
		this.config = config;
	}

	public boolean loadConfig()
	{
		// Check if "use-mysql" is to true. Plugin only use MySQL database.
		if(!config.getBoolean("use-mysql"))
		{
			console.sendMessage(ChatColor.DARK_RED + NKjobs.PNAME
					+ " Disabled because this plugin only use MySQL database. Please set to true the 'use-mysql' field in config.yml");
			return false;
		}

		// Get database access informations
		dbHost = config.getString("host");
		dbPort = config.getInt("port");
		dbName = config.getString("dbName");
		dbUser = config.getString("user");
		dbPassword = config.getString("password");

		// Get prefix used for table name on database
		PREFIX = config.getString("table-prefix", "NKhome_");

		// Get server name gave to bungeecord config
		SERVERNAME = config.getString("server-name", "world");

		// Get max job amount per player
		MAXJOB = config.getInt("max-job", 1);

		// Get ratio gains from spawner
		RATIOFROMSPAWNER = config.getDouble("ratio-from-spawner", 0.40);

		// Get minimum money earned from exploring
		MINEXPLOREMONEY = config.getDouble("min-explore-money", 0.10);

		// Get minimum exp earned from exploring
		MINEXPLOREEXP = config.getDouble("min-explore-exp", 0.10);

		// Get global blocks timer
		GLOBALBLOCKSTIMER = config.getInt("global-blocks-timer", 5);

		// Get global blocks timer
		MAXREGISTEREDFURNACES = config.getInt("max-registered-furnaces", 16);

		// Get global blocks timer
		MAXREGISTEREDBREWINGSTANDS = config.getInt("max-registered-brewingstands", 16);

		return true;
	}

	// ######################################
	// Getters (only)
	// ######################################

	public String getDbHost()
	{
		return dbHost;
	}

	public int getDbPort()
	{
		return dbPort;
	}

	public String getDbName()
	{
		return dbName;
	}

	public String getDbUser()
	{
		return dbUser;
	}

	public String getDbPassword()
	{
		return dbPassword;
	}


}
