package be.noki_senpai.NKjobs.cmd.Jobs;

import be.noki_senpai.NKjobs.data.NKPlayer;
import be.noki_senpai.NKjobs.data.PlayerJob;
import be.noki_senpai.NKjobs.managers.ConfigManager;
import be.noki_senpai.NKjobs.managers.JobManager;
import be.noki_senpai.NKjobs.managers.PlayerManager;
import be.noki_senpai.NKjobs.managers.QueueManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.function.Function;

public class Switch
{
	private QueueManager queueManager = null;
	private PlayerManager playerManager = null;
	private JobManager jobManager = null;

	public Switch(QueueManager queueManager, PlayerManager playerManager, JobManager jobManager)
	{
		this.queueManager = queueManager;
		this.playerManager = playerManager;
		this.jobManager = jobManager;
	}

	public boolean switch_(CommandSender sender, String[] args)
	{
		// Command called by a player
		if(sender instanceof Player)
		{
			String targetName = sender.getName();

			// This command works only if maxJob value is set to 1.
			// Check if maxJob value is different to 1
			if(ConfigManager.MAXJOB != 1)
			{
				sender.sendMessage(ChatColor.RED + " Cette commande est désactivée.");
				Bukkit.getConsoleSender().sendMessage(
						ChatColor.RED + " La commande '/jobs switch' ne fonctionne que si le paramètre 'max-job' est défini à 1.");
				return true;
			}

			// Check if sender has permission
			if(!hasSwitchPermissions(sender))
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

			// Check if a player is specified
			if(args.length >= 3)
			{
				// Check if sender has permission
				if(!hasSwitchOtherPermissions(sender))
				{
					sender.sendMessage(ChatColor.RED + " Vous n'avez pas la permission !");
					return true;
				}
				targetName = args[2];
			}

			String finalTargetName = targetName;
			queueManager.addToQueue(new Function()
			{
				@Override public Object apply(Object o)
				{
					// Get the player
					NKPlayer player = playerManager.getPlayer(finalTargetName);

					// Check if player exist
					if(player == null)
					{
						sender.sendMessage(ChatColor.RED + " Ce joueur n'existe pas");
						return null;
					}

					// Check if player has this job
					if(player.hasJob(jobName))
					{
						sender.sendMessage(ChatColor.RED + " Ce joueur est déjà " + jobName);
						return null;
					}

					Player worker = Bukkit.getPlayer(finalTargetName);
					if(worker != null)
					{
						if(!worker.getName().equals(sender.getName()))
						{
							worker.sendMessage(ChatColor.GREEN + "Vous êtes maintenant " + jobManager.jobs.get(jobName).formattedName);
						}
						for(Map.Entry<String, PlayerJob> job : player.getJobs().entrySet())
						{
							job.getValue().hideBar();
						}
					}

					// Let the player switch job
					playerManager.switchJob(player, jobName, false);
					sender.sendMessage(ChatColor.GREEN + finalTargetName + " est maintenant " + jobManager.jobs.get(jobName).formattedName);

					return null;
				}
			});
		}

		// Command called by Console
		if(sender instanceof ConsoleCommandSender)
		{
			String targetName = sender.getName();

			// This command works only if maxJob value is set to 1.
			// Check if maxJob value is different to 1
			if(ConfigManager.MAXJOB != 1)
			{
				sender.sendMessage(ChatColor.RED + " Cette commande est désactivée.");
				Bukkit.getConsoleSender().sendMessage(
						ChatColor.RED + " La commande '/jobs switch' ne fonctionne que si le paramètre 'max-job' est défini à 1.");
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

			// Check if a player is specified
			if(args.length >= 3)
			{
				targetName = args[2];
			}
			else
			{
				sender.sendMessage(ChatColor.RED + " Vous devez spécifier un joueur.");
				return true;
			}

			String finalTargetName = targetName;
			queueManager.addToQueue(new Function()
			{
				@Override public Object apply(Object o)
				{
					// Get the player
					NKPlayer player = playerManager.getPlayer(finalTargetName);

					// Check if player exist
					if(player == null)
					{
						sender.sendMessage(ChatColor.RED + " Ce joueur n'existe pas");
						return null;
					}

					// Check if player has this job
					if(player.hasJob(jobName))
					{
						sender.sendMessage(ChatColor.RED + " Ce joueur est déjà " + jobName);
						return null;
					}

					// Let the player switch job
					playerManager.switchJob(player, jobName, false);
					sender.sendMessage(ChatColor.GREEN + finalTargetName + " est maintenant " + jobName);

					return null;
				}
			});
		}

		return true;
	}

	private boolean hasSwitchPermissions(CommandSender sender)
	{
		return sender.hasPermission("*") || sender.hasPermission("nkjobs.*") || sender.hasPermission("nkjobs.leaveall")
				|| sender.hasPermission("nkjobs.admin");
	}

	private boolean hasSwitchOtherPermissions(CommandSender sender)
	{
		return sender.hasPermission("*") || sender.hasPermission("nkjobs.*") || sender.hasPermission("nkjobs.leaveall.other")
				|| sender.hasPermission("nkjobs.admin");
	}
}
