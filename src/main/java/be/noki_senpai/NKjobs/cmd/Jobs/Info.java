package be.noki_senpai.NKjobs.cmd.Jobs;

import be.noki_senpai.NKjobs.data.NKPlayer;
import be.noki_senpai.NKjobs.managers.ConfigManager;
import be.noki_senpai.NKjobs.managers.JobManager;
import be.noki_senpai.NKjobs.managers.PlayerManager;
import be.noki_senpai.NKjobs.managers.QueueManager;
import be.noki_senpai.NKjobs.utils.CheckType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.function.Function;

public class Info
{
	private QueueManager queueManager = null;
	private PlayerManager playerManager = null;
	private JobManager jobManager = null;

	public Info(QueueManager queueManager, PlayerManager playerManager, JobManager jobManager)
	{
		this.queueManager = queueManager;
		this.playerManager = playerManager;
		this.jobManager = jobManager;
	}

	public boolean info(CommandSender sender, String[] args)
	{
		// Command called by a player
		if(sender instanceof Player)
		{
			// Check if sender has permission
			if(!hasInfoPermissions(sender))
			{
				sender.sendMessage(ChatColor.RED + " Vous n'avez pas la permission !");
				return true;
			}

			// Check if a job is specified
			if(args.length < 2)
			{
				sender.sendMessage(ChatColor.RED + " Vous devez spécifier un métier");
				return true;
			}

			String jobName = args[1].toLowerCase();
			// Check if specified job exist
			if(!jobManager.jobs.containsKey(jobName))
			{
				sender.sendMessage(ChatColor.RED + " Le job '" + jobName + "' n'existe pas.");
				return true;
			}

			int page = 1;

			// Check if a page number is specified
			if(args.length >= 3)
			{
				if(!CheckType.isNumber(args[2]))
				{
					sender.sendMessage(ChatColor.RED + "'" + args[2] + "' n'est pas un nombre.");
					return true;
				}
				page = Integer.parseInt(args[2]);
				if(page == 0)
				{
					page = 1;
				}
			}
			int jobLevel = 0;
			if(playerManager.getPlayer(sender.getName()).getJobs().containsKey(jobName))
			{
				jobLevel = playerManager.getPlayer(sender.getName()).getJobs().get(jobName).lvl;
			}
			if(playerManager.getPlayer(sender.getName()).getOldJobs().containsKey(jobName))
			{
				jobLevel = playerManager.getPlayer(sender.getName()).getOldJobs().get(jobName).lvl;
			}

			sender.spigot().sendMessage(jobManager.getJobInfo(jobName, page, jobLevel));
		}

		// Command called by Console
		if(sender instanceof ConsoleCommandSender)
		{
			// Check if a job is specified
			if(args.length < 2)
			{
				sender.sendMessage(ChatColor.RED + " Vous devez spécifier un métier");
				return true;
			}

			String jobName = args[1].toLowerCase();
			// Check if specified job exist
			if(!jobManager.jobs.containsKey(jobName))
			{
				sender.sendMessage(ChatColor.RED + " Le job '" + jobName + "' n'existe pas.");
				return true;
			}
		}
		return true;
	}

	private boolean hasInfoPermissions(CommandSender sender)
	{
		return sender.hasPermission("*") || sender.hasPermission("nkjobs.*") || sender.hasPermission("nkjobs.join")
				|| sender.hasPermission("nkjobs.admin");
	}
}
