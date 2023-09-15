package ovh.lumen.NKjobs.commands.Jobs;

import ovh.lumen.NKjobs.data.NKWorker;
import ovh.lumen.NKjobs.data.PlayerJob;
import ovh.lumen.NKjobs.managers.PlayerManager;
import ovh.lumen.NKjobs.utils.Formatter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.function.Function;

public class Stats
{
	private QueueManager queueManager = null;
	private PlayerManager playerManager = null;

	public Stats(QueueManager queueManager, PlayerManager playerManager)
	{
		this.queueManager = queueManager;
		this.playerManager = playerManager;
	}

	public boolean stats(CommandSender sender, String[] args)
	{
		// Command called by a player
		if(sender instanceof Player)
		{
			String targetName = sender.getName();

			// Check if sender has permission
			if(!hasStatsPermissions(sender))
			{
				sender.sendMessage(ChatColor.RED + " Vous n'avez pas la permission !");
				return true;
			}

			// Check if a player is specified
			if(args.length >= 2)
			{
				// Check if sender has permission
				if(!hasStatsOtherPermissions(sender))
				{
					sender.sendMessage(ChatColor.RED + " Vous n'avez pas la permission !");
					return true;
				}

				targetName = args[1];
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

					// Check if player has a job and/or an old job
					if(player.getJobs().size() == 0 && player.getOldJobs().size() == 0)
					{
						sender.sendMessage(ChatColor.RED + " Ce joueur n'a aucun job.");
						return null;
					}

					// Display all player jobs
					String stats = ChatColor.GREEN + "---- Métier(s) de " + player.getName() + " ---------------------------------";
					if(player.getJobs().size() == 0)
					{
						stats += "\n" + ChatColor.WHITE + "Aucun métier" + ChatColor.GREEN;
					}
					else
					{
						for(Map.Entry<String, PlayerJob> jobs : player.getJobs().entrySet())
						{
							stats += "\n" + jobs.getValue().formattedName + " niveau " + jobs.getValue().lvl + " ( " + Formatter.format(jobs.getValue().xp) + " / " + Formatter.format(jobs.getValue().xpGoal) + " xp )";
						}
					}

					stats += "\n" + ChatColor.GREEN + "---- Ancien(s) métier(s) de " + player.getName() + " ---------------------";
					if(player.getOldJobs().size() == 0)
					{
						stats += "\n" + ChatColor.WHITE + "Aucun ancien métier" + ChatColor.GREEN;
					}
					else
					{
						for(Map.Entry<String, PlayerJob> jobs : player.getOldJobs().entrySet())
						{
							stats += "\n" + jobs.getValue().formattedName + " niveau " + jobs.getValue().lvl + " ( " + Formatter.format(jobs.getValue().xp) + " / " + Formatter.format(jobs.getValue().xpGoal) + " xp )";
						}
					}
					sender.sendMessage(stats);

					return null;
				}
			});
		}

		// Command called by Console
		if(sender instanceof ConsoleCommandSender)
		{
			String targetName = sender.getName();

			// Check if a player is specified
			if(args.length >= 2)
			{
				targetName = args[1];
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

					// Check if player has a job and/or an old job
					if(player.getJobs().size() == 0 && player.getOldJobs().size() == 0)
					{
						sender.sendMessage(ChatColor.RED + " Ce joueur n'a aucun job.");
						return null;
					}

					// Display all player jobs
					String stats = ChatColor.GREEN + "---- Métier(s) de " + player.getName() + " ---------------------------------";
					if(player.getJobs().size() == 0)
					{
						stats += "\n" + ChatColor.WHITE + "Aucun métier" + ChatColor.GREEN;
					}
					else
					{
						for(Map.Entry<String, PlayerJob> jobs : player.getJobs().entrySet())
						{
							stats += "\n" + jobs.getValue().formattedName + " niveau " + jobs.getValue().lvl + " ( " + Formatter.format(jobs.getValue().xp) + " / " + Formatter.format(jobs.getValue().xpGoal) + " xp )";
						}
					}

					stats = ChatColor.GREEN + "---- Ancien(s) métier(s) de " + player.getName() + " ------------------------";
					if(player.getJobs().size() == 0)
					{
						stats += "\n" + ChatColor.WHITE + "Aucun ancien métier" + ChatColor.GREEN;
					}
					else
					{
						for(Map.Entry<String, PlayerJob> jobs : player.getOldJobs().entrySet())
						{
							stats += "\n" + jobs.getValue().formattedName + " niveau " + jobs.getValue().lvl + " ( " + Formatter.format(jobs.getValue().xp) + " / " + Formatter.format(jobs.getValue().xpGoal) + " xp )";
						}
					}
					sender.sendMessage(stats);

					return null;
				}
			});
		}

		return true;
	}

	private boolean hasStatsPermissions(CommandSender sender)
	{
		return sender.hasPermission("*") || sender.hasPermission("nkjobs.*") || sender.hasPermission("nkjobs.stats")
				|| sender.hasPermission("nkjobs.admin");
	}

	private boolean hasStatsOtherPermissions(CommandSender sender)
	{
		return sender.hasPermission("*") || sender.hasPermission("nkjobs.*") || sender.hasPermission("nkjobs.stats.other")
				|| sender.hasPermission("nkjobs.admin");
	}
}
