package be.noki_senpai.NKjobs.managers;

import be.noki_senpai.NKjobs.NKjobs;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

public class Manager
{
	private ConsoleCommandSender console = null;
	private JobManager jobManager = null;
	private ConfigManager configManager = null;
	private DatabaseManager databaseManager = null;
	private PlayerManager playerManager = null;
	private QueueManager queueManager = null;
	private DataRegisterManager dataRegisterManager = null;
	private TranslationManager translationManager = null;

	public Manager(NKjobs instance)
	{
		console = Bukkit.getConsoleSender();
		configManager = new ConfigManager(instance.getConfig());
		databaseManager = new DatabaseManager(configManager);
		jobManager = new JobManager();
		playerManager = new PlayerManager(jobManager);
		queueManager = new QueueManager();
		dataRegisterManager = new DataRegisterManager(playerManager, queueManager);
		translationManager = new TranslationManager();
	}

	// ######################################
	// Getters & Setters
	// ######################################

	// Console
	public ConsoleCommandSender getConsole()
	{
		return console;
	}

	// PluginManager
	public ConfigManager getConfigManager()
	{
		return configManager;
	}

	// DatabaseManager
	public DatabaseManager getDatabaseManager()
	{
		return databaseManager;
	}

	// PlayerManager
	public PlayerManager getPlayerManager()
	{
		return playerManager;
	}

	// JobManager
	public JobManager getJobManager()
	{
		return jobManager;
	}

	// QueueManager
	public QueueManager getQueueManager()
	{
		return queueManager;
	}

	// DataRegisterManager
	public DataRegisterManager getDataRegisterManager()
	{
		return dataRegisterManager;
	}

	// TranslationManager
	public TranslationManager getTranslationManager()
	{
		return translationManager;
	}
}
