package be.noki_senpai.NKjobs.cmd.Jobs;

import java.util.function.Function;

import be.noki_senpai.NKjobs.managers.ConfigManager;
import be.noki_senpai.NKjobs.managers.JobManager;
import be.noki_senpai.NKjobs.managers.PlayerManager;
import be.noki_senpai.NKjobs.managers.QueueManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import be.noki_senpai.NKjobs.data.NKPlayer;

public class Join
{
	private QueueManager queueManager = null;
	private PlayerManager playerManager = null;
	private JobManager jobManager = null;

	public Join(QueueManager queueManager, PlayerManager playerManager, JobManager jobManager)
	{
		this.queueManager = queueManager;
		this.playerManager = playerManager;
		this.jobManager = jobManager;
	}

	public boolean join(CommandSender sender, String[] args)
	{
		// /jobs join <job> [joueur]
		// args[0] = join
		// args[1] = #job
		// args[2] = #player

		// Command called by a player
		if(sender instanceof Player)
		{
			String targetName = sender.getName();

			// Check if sender has permission
			if(!hasJoinPermissions(sender))
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
				if(!hasJoinOtherPermissions(sender))
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

					// Check if player has already this job
					if(player.hasJob(jobName))
					{
						sender.sendMessage(ChatColor.RED + " Ce joueur est déjà " + jobName);
						return null;
					}

					// Check if player can join a new job
					if(!player.canJoinJob())
					{
						sender.sendMessage(ChatColor.RED + " Ce joueur ne peut pas avoir plus de " + ConfigManager.MAXJOB + " métier(s)");
						return null;
					}

					// Let the player join the job
					playerManager.joinJob(player, jobName, false);
					sender.sendMessage(ChatColor.GREEN + finalTargetName + " est maintenant " + jobName);

					return null;
				}
			});
		}

		// Command called by Console
		if(sender instanceof ConsoleCommandSender)
		{
			String targetName = sender.getName();

			// Check if a job is specified
			if(args.length < 2)
			{
				sender.sendMessage(ChatColor.RED + " Vous devez spécifier un métier");
				return true;
			}

			// Check if specified job exist
			if(!jobManager.jobs.containsKey(args[1]))
			{
				sender.sendMessage(ChatColor.RED + " Le job '" + args[1] + "' n'existe pas.");
				return true;
			}

			String jobName = args[1].toLowerCase();
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

					// Check if player has already this job
					if(player.hasJob(jobName))
					{
						sender.sendMessage(ChatColor.RED + " Ce joueur est déjà " + jobName);
						return null;
					}

					// Check if player can join a new job
					if(player.canJoinJob())
					{
						sender.sendMessage(ChatColor.RED + " Ce joueur ne peut pas avoir plus de " + ConfigManager.MAXJOB + " métier(s)");
						return null;
					}

					// Let the player join the job
					playerManager.joinJob(player, jobName, false);
					sender.sendMessage(ChatColor.GREEN + finalTargetName + " est maintenant " + jobName);

					return null;
				}
			});
		}

		return true;
	}

	private boolean hasJoinPermissions(CommandSender sender)
	{
		return sender.hasPermission("*") || sender.hasPermission("nkjobs.*") || sender.hasPermission("nkjobs.join")
				|| sender.hasPermission("nkjobs.admin");
	}

	private boolean hasJoinOtherPermissions(CommandSender sender)
	{
		return sender.hasPermission("*") || sender.hasPermission("nkjobs.*") || sender.hasPermission("nkjobs.join.other")
				|| sender.hasPermission("nkjobs.admin");
	}
}
