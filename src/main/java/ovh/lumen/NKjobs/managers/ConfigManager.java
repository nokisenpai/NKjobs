package ovh.lumen.NKjobs.managers;

import org.bukkit.configuration.file.FileConfiguration;
import ovh.lumen.NKcore.api.data.DBAccess;
import ovh.lumen.NKjobs.data.NKData;
import ovh.lumen.NKjobs.enums.InternalMessages;
import ovh.lumen.NKjobs.enums.LogLevel;
import ovh.lumen.NKjobs.utils.NKLogger;

public final class ConfigManager
{
	private ConfigManager() {}

	private static FileConfiguration config = null;

	public static void init(FileConfiguration config)
	{
		ConfigManager.config = config;
	}

	public static void load()
	{
		boolean useNKcoreAccess = config.getBoolean("use-nkcore-access", true);

		if(useNKcoreAccess)
		{
			NKData.DBACCESS = NKcoreAPIManager.nKcoreAPI.getDBAccess();
		}
		else
		{
			DBAccess dbAccess = new DBAccess();
			dbAccess.setHost(config.getString("host"));
			dbAccess.setPort(config.getInt("port"));
			dbAccess.setDbName(config.getString("dbName"));
			dbAccess.setUser(config.getString("user"));
			dbAccess.setPassword(config.getString("password"));

			NKData.DBACCESS = dbAccess;
		}

		NKData.PREFIX = config.getString("table-prefix", NKData.PLUGIN_NAME + "_");

		NKData.SERVER_INFO = NKcoreAPIManager.nKcoreAPI.getNKServer();

		NKData.MAX_JOB = config.getInt("max-job", 1);
		NKData.RATIO_FROM_SPAWNER = config.getDouble("ratio-from-spawner", 0.40);
		NKData.MIN_EXPLORE_MONEY = config.getDouble("min-explore-money", 0.10);
		NKData.MIN_EXPLORE_EXP = config.getDouble("min-explore-exp", 0.10);
		NKData.GLOBAL_BLOCKS_TIMER = config.getInt("global-blocks-timer", 5);
		NKData.MAX_REGISTERED_FURNACES = config.getInt("max-registered-furnaces", 16);
		NKData.MAX_REGISTERED_BREWINGSTANDS = config.getInt("max-registered-brewingstands", 16);

		try
		{
			NKData.LOGLEVEL = LogLevel.valueOf(config.getString("log-level", "LOG").toUpperCase());
		}
		catch(IllegalArgumentException e)
		{
			NKLogger.error(InternalMessages.CONFIG_KEY_LOGLEVEL_BAD_VALUE.toString());
			NKData.LOGLEVEL = LogLevel.LOG;
		}

		NKLogger.setLogLevel(NKData.LOGLEVEL);
	}
}
