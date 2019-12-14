package be.noki_senpai.NKjobs.cmd.Jobs;

import be.noki_senpai.NKjobs.cmd.JobsCmd;
import be.noki_senpai.NKjobs.data.NKPlayer;
import be.noki_senpai.NKjobs.managers.JobManager;
import be.noki_senpai.NKjobs.managers.PlayerManager;
import be.noki_senpai.NKjobs.managers.QueueManager;
import be.noki_senpai.NKjobs.utils.CheckType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.function.Function;

public class Level
{
	private QueueManager queueManager = null;
	private PlayerManager playerManager = null;
	private JobManager jobManager = null;

	public Level(QueueManager queueManager, PlayerManager playerManager, JobManager jobManager)
	{
		this.queueManager = queueManager;
		this.playerManager = playerManager;
		this.jobManager = jobManager;
	}

	public boolean level(CommandSender sender, String[] args)
	{
		// Command called by a player
		if(sender instanceof Player)
		{
			String targetName = sender.getName();

			// Check if sender has permission
			if(!hasLevelPermissions(sender))
			{
				sender.sendMessage(ChatColor.RED + " Vous n'avez pas la permission !");
				return true;
			}

			// Check if sub command is specified
			if(args.length < 2)
			{
				sender.sendMessage(ChatColor.RED + JobsCmd.usageCmdLevel);
				return true;
			}

			String subCommand = args[1].toLowerCase();
			// Check if sub command is valid
			if( !(subCommand.equals("add") || subCommand.equals("set") || subCommand.equals("remove")) )
			{
				sender.sendMessage(ChatColor.RED + JobsCmd.usageCmdLevel);
				return true;
			}

			// Check if a value is specified
			if(args.length < 3)
			{
				sender.sendMessage(ChatColor.RED + JobsCmd.usageCmdLevel);
				return true;
			}

			// Check if value is a number
			if(!CheckType.isNumber(args[2]))
			{
				sender.sendMessage(ChatColor.RED + " La valeur doit être un nombre");
				return true;
			}
			int amount = Integer.parseInt(args[2]);

			// Check if a job is specified
			if(args.length < 4)
			{
				sender.sendMessage(ChatColor.RED + JobsCmd.usageCmdLevel);
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
				if(!hasLevelOtherPermissions(sender))
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
					NKPlayer player = playerManager.getPlayer(finalTargetName);

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
					playerManager.levelJob(player, jobName, amount, subCommand, false);
					sender.sendMessage(ChatColor.GREEN + "Le niveau de " + jobName + " de " + finalTargetName + " a été altérée.");

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
				sender.sendMessage(ChatColor.RED + JobsCmd.usageCmdLevel);
				return true;
			}

			String subCommand = args[1].toLowerCase();
			// Check if sub command is valid
			if( !(subCommand.equals("add") || subCommand.equals("set") || subCommand.equals("remove")) )
			{
				sender.sendMessage(ChatColor.RED + JobsCmd.usageCmdLevel);
				return true;
			}

			// Check if a value is specified
			if(args.length < 3)
			{
				sender.sendMessage(ChatColor.RED + JobsCmd.usageCmdLevel);
				return true;
			}

			// Check if value is a number
			if(!CheckType.isNumber(args[2]))
			{
				sender.sendMessage(ChatColor.RED + " La valeur doit être un nombre");
				return true;
			}
			int amount = Integer.parseInt(args[2]);

			// Check if a job is specified
			if(args.length < 4)
			{
				sender.sendMessage(ChatColor.RED + JobsCmd.usageCmdLevel);
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
					NKPlayer player = playerManager.getPlayer(finalTargetName);

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
					playerManager.levelJob(player, jobName, amount, subCommand, false);
					sender.sendMessage(ChatColor.GREEN + finalTargetName + " est maintenant " + jobName);

					return null;
				}
			});
		}

		return true;
	}

	private boolean hasLevelPermissions(CommandSender sender)
	{
		return sender.hasPermission("*") || sender.hasPermission("nkjobs.*") || sender.hasPermission("nkjobs.exp")
				|| sender.hasPermission("nkjobs.admin");
	}

	private boolean hasLevelOtherPermissions(CommandSender sender)
	{
		return sender.hasPermission("*") || sender.hasPermission("nkjobs.*") || sender.hasPermission("nkjobs.exp.other")
				|| sender.hasPermission("nkjobs.admin");
	}
}
