package ovh.lumen.NKjobs.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import ovh.lumen.NKjobs.data.NKData;
import ovh.lumen.NKjobs.enums.LogLevel;

public class NKLogger
{
	private static String pluginName = "";
	private static LogLevel logLevel = LogLevel.LOG;
	private static ConsoleCommandSender console = null;

	public static void init(ConsoleCommandSender console)
	{
		NKLogger.pluginName = "[" + NKData.PLUGIN_NAME + "]";
		NKLogger.console = console;
	}

	public static void setLogLevel(LogLevel logLevel)
	{
		NKLogger.logLevel = logLevel;
	}

	public static void debug(String message)
	{
		if(logLevel.value() <= LogLevel.DEBUG.value())
		{
			console.sendMessage(pluginName + ChatColor.BLUE + " [DEBUG] " + message);
		}
	}

	public static void log(String message)
	{
		if(logLevel.value() <= LogLevel.LOG.value())
		{
			console.sendMessage(pluginName + ChatColor.GRAY + " [LOG] " + message);
		}
	}

	public static void warn(String message)
	{
		if(logLevel.value() <= LogLevel.WARN.value())
		{
			console.sendMessage(pluginName + ChatColor.DARK_PURPLE + " [WARN] " + message);
		}
	}

	public static void error(String message)
	{
		if(logLevel.value() <= LogLevel.ERROR.value())
		{
			console.sendMessage(pluginName + ChatColor.RED + " [ERROR] " + message);
		}
	}

	public static void send(String message)
	{
		console.sendMessage(pluginName + " " + message);
	}

	public static void show(String message)
	{
		console.sendMessage(message);
	}
}
