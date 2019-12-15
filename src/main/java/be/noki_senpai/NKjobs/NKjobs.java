package be.noki_senpai.NKjobs;

import java.io.File;

import be.noki_senpai.NKjobs.listeners.*;
import be.noki_senpai.NKjobs.managers.Manager;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import be.noki_senpai.NKjobs.cmd.JobsCmd;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class NKjobs extends JavaPlugin implements PluginMessageListener
{
	public static String PNAME = "[NKjobs]";
	private Manager manager = null;
	private ConsoleCommandSender console = null;
	private static NKjobs plugin = null;
	private static Economy economy = null;

	// Fired when plugin is first enabled
	@Override public void onEnable()
	{
		plugin = this;
		System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "WARN");
		this.saveDefaultConfig();

		console = Bukkit.getConsoleSender();
		manager = new Manager(this);

		if(!checkNKmanager())
		{
			console.sendMessage(ChatColor.DARK_RED + PNAME + " NKmanager in not enabled !");
			disablePlugin();
			return;
		}

		// Link with Vault
		if(!setupEconomy())
		{
			console.sendMessage(ChatColor.DARK_RED + PNAME + " No Vault dependency found !");
			disablePlugin();
			return;
		}

		if(!new File(getDataFolder() + "/jobConfig.yaml").isFile())
		{
			this.saveResource("jobConfig.yaml", false);
		}

		if(!new File(getDataFolder() + "/restrictedBlocks.yaml").isFile())
		{
			this.saveResource("restrictedBlocks.yaml", false);
		}

		if(!new File(getDataFolder() + "/fr_items.yaml").isFile())
		{
			this.saveResource("fr_items.yaml", false);
		}

		// Load configuration
		if(!manager.getConfigManager().loadConfig())
		{
			disablePlugin();
			return;
		}

		// Load database connection (with check)
		if(!manager.getDatabaseManager().loadDatabase())
		{
			disablePlugin();
			return;
		}

		// Load job config (with check)
		if(!manager.getJobManager().loadJob())
		{
			disablePlugin();
			return;
		}

		// Load blocks, furnaces and brewingStands data (with check)
		if(!manager.getDataRegisterManager().loadData())
		{
			disablePlugin();
			return;
		}

		// Load translations
		if(!manager.getTranslationManager().loadTranslation())
		{
			disablePlugin();
			return;
		}

		// Load data for online players
		manager.getPlayerManager().loadPlayer();

		// On command
		getCommand("jobs").setExecutor(new JobsCmd(manager.getQueueManager(), manager.getPlayerManager(), manager.getJobManager()));

		// Event
		getServer().getPluginManager().registerEvents(new PlayerConnectionListener(manager.getPlayerManager()), this);
		getServer().getPluginManager().registerEvents(new JobsListener(manager.getPlayerManager(), manager.getJobManager(), manager.getDataRegisterManager()), this);
		getServer().getPluginManager().registerEvents(new PistonMoveListener(manager.getDataRegisterManager()), this);
		getServer().getPluginManager().registerEvents(new ProtectedItemListener(manager.getPlayerManager()), this);
		getCommand("jobs").setTabCompleter(new JobCompleter(manager.getJobManager()));

		// Data exchange between servers
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);

		console.sendMessage(ChatColor.WHITE + "     .--. ");
		console.sendMessage(ChatColor.WHITE + "     |    '.   " + ChatColor.GREEN + PNAME + " by NoKi_senpai - successfully enabled !");
		console.sendMessage(ChatColor.WHITE + "'-..____.-'");
	}

	// Fired when plugin is disabled
	@Override public void onDisable()
	{
		manager.getDatabaseManager().unloadDatabase();
		manager.getJobManager().unloadJob();
		manager.getPlayerManager().unloadPlayer();
		manager.getDataRegisterManager().purgeOnlineBlocksTimer();
		manager.getDataRegisterManager().saveAll();
		console.sendMessage(ChatColor.GREEN + PNAME + " has been disable.");
	}

	// ######################################
	// Getters & Setters
	// ######################################

	// Getter 'instance'
	public static NKjobs getPlugin()
	{
		return plugin;
	}

	// Getter 'bdd'
	public Economy getEconomy()
	{
		return economy;
	}

	// ######################################
	// Disable this plugin
	// ######################################

	public void disablePlugin()
	{
		getServer().getPluginManager().disablePlugin(this);
	}

	// ######################################
	// Data exchange between servers
	// ######################################

	@Override public void onPluginMessageReceived(String channel, Player player, byte[] message)
	{

		if(!channel.equals("BungeeCord"))
		{
			return;
		}
		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		String subchannel = in.readUTF();

		if(subchannel.equals("NKjobs"))
		{
			String tmp = in.readUTF();
			tmp = tmp.substring(2, tmp.length());
			String[] args = tmp.split("\\|");

			if(args.length >= 5)
			{

				switch(args[0])
				{
					case "join":
						manager.getPlayerManager().joinJob(manager.getPlayerManager().getPlayer(args[1]), args[2], true);
						break;
					case "leave":
						manager.getPlayerManager().leaveJob(manager.getPlayerManager().getPlayer(args[1]), args[2], true);
						break;
					case "leaveall":
						manager.getPlayerManager().leaveAllJob(manager.getPlayerManager().getPlayer(args[1]), true);
						break;
					case "switch":
						manager.getPlayerManager().switchJob(manager.getPlayerManager().getPlayer(args[1]), args[2], true);
						break;
					case "exp":
						manager.getPlayerManager().expJob(manager.getPlayerManager().getPlayer(args[1]), args[2], Double.valueOf(args[3]), args[4], true);
						break;
					case "level":
						manager.getPlayerManager().levelJob(manager.getPlayerManager().getPlayer(args[1]), args[2], Integer.parseInt(args[3]), args[4], true);
						break;
					default:
				}

			}
		}
	}

	private boolean setupEconomy()
	{
		if(getServer().getPluginManager().getPlugin("Vault") == null)
		{
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if(rsp == null)
		{
			return false;
		}
		economy = rsp.getProvider();
		return economy != null;
	}

	private static void alterPlayerMoney(OfflinePlayer player, String worldName, double value)
	{
		if(value > 0)
		{
			economy.depositPlayer(player, worldName, value);
			return;
		}
		if(value < 0)
		{
			value *= -1;
			economy.withdrawPlayer(player, worldName, value);
		}
	}

	// ######################################
	// Check if NKmanager is enabled
	// ######################################

	public boolean checkNKmanager()
	{
		return getServer().getPluginManager().getPlugin("NKmanager").isEnabled();
	}
}
