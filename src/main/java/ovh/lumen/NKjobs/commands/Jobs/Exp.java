package ovh.lumen.NKjobs.commands.Jobs;

import ovh.lumen.NKjobs.commands.JobsCmd;
import ovh.lumen.NKjobs.data.NKWorker;
import ovh.lumen.NKjobs.managers.JobManager;
import ovh.lumen.NKjobs.managers.PlayerManager;
import ovh.lumen.NKjobs.utils.CheckType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.function.Function;

public class Exp
{
	private QueueManager queueManager = null;
	private PlayerManager playerManager = null;
	private JobManager jobManager = null;

	public Exp(QueueManager queueManager, PlayerManager playerManager, JobManager jobManager)
	{
		this.queueManager = queueManager;
		this.playerManager = playerManager;
		this.jobManager = jobManager;
	}

	public boolean exp(CommandSender sender, String[] args)
	{
		// Command called by a player
		if(sender instanceof Player)
		{
			String targetName = sender.getName();

			// Check if sender has permission
			if(!hasExpPermissions(sender))
			{
				sender.sendMessage(ChatColor.RED + " Vous n'avez pas la permission !");
				return true;
			}

			// Check if sub command is specified
			if(args.length < 2)
			{
				sender.sendMessage(ChatColor.RED + JobsCmd.usageCmdExp);
				return true;
			}

			String subCommand = args[1].toLowerCase();
			// Check if sub command is valid
			if( !(subCommand.equals("add") || subCommand.equals("set") || subCommand.equals("remove")) )
			{
				sender.sendMessage(ChatColor.RED + JobsCmd.usageCmdExp);
				return true;
			}

			// Check if a value is specified
			if(args.length < 3)
			{
				sender.sendMessage(ChatColor.RED + JobsCmd.usageCmdExp);
				return true;
			}

			// Check if value is a number
			if(!CheckType.isNumber(args[2]))
			{
				sender.sendMessage(ChatColor.RED + " La valeur doit être un nombre");
				return true;
			}
			Double amount = Double.valueOf(args[2]);

			// Check if a job is specified
			if(args.length < 4)
			{
				sender.sendMessage(ChatColor.RED + JobsCmd.usageCmdExp);
				return true;
			}

			String jobName = args[3].toLowerCase();
			// Check if specified job exist
			if(!jobManager.jobs.containsKey(jobName))
			{
				sender.sendMessage(ChatColor.RED + " Le job '" + jobName + "' n'existe pas.");
				return true;
			}



			// Check if a player is specified
			if(args.length >= 5)
			{
				// Check if sender has permission
				if(!hasExpOtherPermissions(sender))
				{
					sender.sendMessage(ChatColor.RED + " Vous n'avez pas la permission !");
					return true;
				}

				targetName = args[4];
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

					// Alter job xp
					playerManager.expJob(player, jobName, amount, subCommand, false);
					sender.sendMessage(ChatColor.GREEN + "L'xp de " + jobName + " de " + finalTargetName + " a été altérée.");

					return null;
				}
			});
		}

		// Command called by Console
		if(sender instanceof ConsoleCommandSender)
		{
			String targetName = sender.getName();

			// Check if sub command is specified
			if(args.length < 2)
			{
				sender.sendMessage(ChatColor.RED + JobsCmd.usageCmdExp);
				return true;
			}

			String subCommand = args[1].toLowerCase();
			// Check if sub command is valid
			if( !(subCommand.equals("add") || subCommand.equals("set") || subCommand.equals("remove")) )
			{
				sender.sendMessage(ChatColor.RED + JobsCmd.usageCmdExp);
				return true;
			}

			// Check if a value is specified
			if(args.length < 3)
			{
				sender.sendMessage(ChatColor.RED + JobsCmd.usageCmdExp);
				return true;
			}

			// Check if value is a number
			if(!CheckType.isNumber(args[2]))
			{
				sender.sendMessage(ChatColor.RED + " La valeur doit être un nombre");
				return true;
			}
			Double amount = Double.valueOf(args[2]);

			// Check if a job is specified
			if(args.length < 4)
			{
				sender.sendMessage(ChatColor.RED + JobsCmd.usageCmdExp);
				return true;
			}

			String jobName = args[3].toLowerCase();
			// Check if specified job exist
			if(!jobManager.jobs.containsKey(jobName))
			{
				sender.sendMessage(ChatColor.RED + " Le job '" + jobName + "' n'existe pas.");
				return true;
			}

			// Check if a player is specified
			if(args.length >= 5)
			{
				targetName = args[4];
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

					// Alter job xp
					playerManager.expJob(player, jobName, amount, subCommand, false);
					sender.sendMessage(ChatColor.GREEN + finalTargetName + " est maintenant " + jobName);

					return null;
				}
			});
		}

		return true;
	}

	private boolean hasExpPermissions(CommandSender sender)
	{
		return sender.hasPermission("*") || sender.hasPermission("nkjobs.*") || sender.hasPermission("nkjobs.exp")
				|| sender.hasPermission("nkjobs.admin");
	}

	private boolean hasExpOtherPermissions(CommandSender sender)
	{
		return sender.hasPermission("*") || sender.hasPermission("nkjobs.*") || sender.hasPermission("nkjobs.exp.other")
				|| sender.hasPermission("nkjobs.admin");
	}
}
