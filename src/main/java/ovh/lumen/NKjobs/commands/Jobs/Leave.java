package ovh.lumen.NKjobs.commands.Jobs;

import ovh.lumen.NKjobs.data.NKWorker;
import ovh.lumen.NKjobs.managers.JobManager;
import ovh.lumen.NKjobs.managers.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.function.Function;

public class Leave
{
	private QueueManager queueManager = null;
	private PlayerManager playerManager = null;
	private JobManager jobManager = null;

	public Leave(QueueManager queueManager, PlayerManager playerManager, JobManager jobManager)
	{
		this.queueManager = queueManager;
		this.playerManager = playerManager;
		this.jobManager = jobManager;
	}

	public boolean leave(CommandSender sender, String[] args)
	{
		// Command called by a player
		if(sender instanceof Player)
		{
			String targetName = sender.getName();

			// Check if sender has permission
			if(!hasLeavePermissions(sender))
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
				if(!hasLeaveOtherPermissions(sender))
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
					NKWorker player = playerManager.getPlayer(finalTargetName);

					// Check if player exist
					if(player == null)
					{
						sender.sendMessage(ChatColor.RED + " Ce joueur n'existe pas");
						return null;
					}

					// Check if player has this job
					if(!player.hasJob(jobName))
					{
						sender.sendMessage(ChatColor.RED + " Ce joueur n'est pas " + jobName);
						return null;
					}

					// Let the player leave the job
					playerManager.leaveJob(player, jobName, false);
					sender.sendMessage(ChatColor.GREEN + finalTargetName + " n'est plus " + jobManager.jobs.get(jobName).formattedName);

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
					NKWorker player = playerManager.getPlayer(finalTargetName);

					// Check if player exist
					if(player == null)
					{
						sender.sendMessage(ChatColor.RED + " Ce joueur n'existe pas");
						return null;
					}

					// Check if player has this job
					if(!player.hasJob(jobName))
					{
						sender.sendMessage(ChatColor.RED + " Ce joueur n'est pas " + jobName);
						return null;
					}

					Player worker = Bukkit.getPlayer(finalTargetName);
					if(worker != null)
					{
						worker.sendMessage(ChatColor.GREEN + "Vous n'êtes plus " + jobManager.jobs.get(jobName).formattedName);
						player.getJobs().get(jobName).hideBar();
					}

					// Let the player leave the job
					playerManager.leaveJob(player, jobName, false);
					sender.sendMessage(ChatColor.GREEN + finalTargetName + " n'est plus " + jobManager.jobs.get(jobName).formattedName);

					return null;
				}
			});
		}

		return true;
	}

	private boolean hasLeavePermissions(CommandSender sender)
	{
		return sender.hasPermission("*") || sender.hasPermission("nkjobs.*") || sender.hasPermission("nkjobs.leave")
				|| sender.hasPermission("nkjobs.admin");
	}

	private boolean hasLeaveOtherPermissions(CommandSender sender)
	{
		return sender.hasPermission("*") || sender.hasPermission("nkjobs.*") || sender.hasPermission("nkjobs.leave.other")
				|| sender.hasPermission("nkjobs.admin");
	}
}
