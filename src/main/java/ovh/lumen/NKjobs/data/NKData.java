package ovh.lumen.NKjobs.data;

import ovh.lumen.NKcore.api.data.DBAccess;
import ovh.lumen.NKcore.api.data.NKServer;
import ovh.lumen.NKjobs.enums.LogLevel;
import ovh.lumen.NKjobs.interfaces.NKplugin;

import java.util.HashMap;
import java.util.Map;

public class NKData
{
	public static DBAccess DBACCESS = new DBAccess();
	public static NKServer SERVER_INFO = null;
	public static String PREFIX = null;
	public static LogLevel LOGLEVEL = null;
	public static NKplugin PLUGIN = null;
	public static String PLUGIN_NAME = null;
	public static String PLUGIN_VERSION = null;
	public static String PLUGIN_AUTHOR = null;
	public static int MAX_JOB = -1;
	public static String FROM_SPAWNER = "NKjobs_from_spawner";
	public static String HIT_PIGMEN = "NKjobs_hit_pigmen";
	public static double RATIO_FROM_SPAWNER = 0.40;
	public static double MIN_EXPLORE_MONEY = 0.10;
	public static double MIN_EXPLORE_EXP = 0.10;
	public static int GLOBAL_BLOCKS_TIMER = 5;
	public static int MAX_REGISTERED_FURNACES = 16;
	public static int MAX_REGISTERED_BREWINGSTANDS = 16;
	public static Map<String, Job> JOBS = new HashMap<>();
}
