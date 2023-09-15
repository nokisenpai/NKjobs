package ovh.lumen.NKjobs.commands.Jobs;

import ovh.lumen.NKjobs.data.PlayerJob;
import ovh.lumen.NKjobs.managers.JobManager;
import ovh.lumen.NKjobs.utils.CheckType;
import ovh.lumen.NKjobs.utils.Formatter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.function.Function;

public class Top
{
	private QueueManager queueManager = null;
	private JobManager jobManager = null;

	public Top(QueueManager queueManager, JobManager jobManager)
	{
		this.queueManager = queueManager;
		this.jobManager = jobManager;
	}

	public boolean top(CommandSender sender, String[] args)
	{
		// Command called by a player
		if(sender instanceof Player)
		{
			String targetName = sender.getName();

			// Check if sender has permission
			if(!hasTopPermissions(sender))
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

			int page = 1;

			// If no more argument
			if(args.length >= 3)
			{
				if(CheckType.isNumber(args[2]))
				{
					page = Integer.parseInt(args[2]);
					if(page == 0)
					{
						page = 1;
					}
				}
				else
				{
					sender.sendMessage(ChatColor.RED + "La page doit être un nombre");
					return true;
				}
			}

			String jobName = args[1].toLowerCase();
			// Check if specified job exist
			if(!jobManager.jobs.containsKey(jobName))
			{
				sender.sendMessage(ChatColor.RED + " Le job '" + jobName + "' n'existe pas.");
				return true;
			}

			String finalJobName = jobName;
			int finalPage = page;
			queueManager.addToQueue(new Function()
			{
				@Override public Object apply(Object o)
				{
					// Get top of the job
					Map<String, PlayerJob> jobs = jobManager.getTop(finalJobName, finalPage);

					if(jobs.size() == 0)
					{
						sender.sendMessage(ChatColor.RED + "Il n'y a personne à cette page du classement");
					}

					String topList =
							ChatColor.GREEN + "---- Top " + finalJobName + ChatColor.GREEN + " ---- " + ChatColor.AQUA + ((finalPage - 1) * 10 + 1)
									+ ChatColor.GREEN + " à " + ChatColor.AQUA + (finalPage * 10) + ChatColor.GREEN
									+ " -----------------------------";

					int i = ((finalPage - 1) * 10 + 1);
					String itsMe = "";
					for(Map.Entry<String, PlayerJob> entry : jobs.entrySet())
					{
						itsMe = "";
						if(entry.getKey().equals(sender.getName()))
						{
							itsMe = ChatColor.GOLD + "" + ChatColor.BOLD + "> " + ChatColor.RESET;
						}
						if(i == 1)
						{
							topList += "\n" + ChatColor.GOLD + i + ". " + itsMe + ChatColor.GOLD + ChatColor.BOLD + entry.getKey() + "   niveau "
									+ entry.getValue().lvl + " ( " + Formatter.format(entry.getValue().xp) + " xp / " + Formatter.format(entry.getValue().xpGoal) + " xp )";
						}
						else
						{
							topList +=
									"\n" + ChatColor.GREEN + i + ". " + itsMe + ChatColor.GREEN + entry.getKey() + "   niveau " + entry.getValue().lvl
											+ " ( " + Formatter.format(entry.getValue().xp) + " xp / " + Formatter.format(entry.getValue().xpGoal) + " xp )";
						}
						i = i + 1;
					}
					sender.sendMessage(topList);

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

			int page = 1;

			// If no more argument
			if(args.length >= 3)
			{
				if(CheckType.isNumber(args[2]))
				{
					page = Integer.parseInt(args[2]);
					if(page == 0)
					{
						page = 1;
					}
				}
				else
				{
					sender.sendMessage(ChatColor.RED + "La page doit être un nombre");
					return true;
				}
			}

			String jobName = args[1].toLowerCase();
			// Check if specified job exist
			if(!jobManager.jobs.containsKey(jobName))
			{
				sender.sendMessage(ChatColor.RED + " Le job '" + jobName + "' n'existe pas.");
				return true;
			}

			String finalJobName = jobName;
			int finalPage = page;
			queueManager.addToQueue(new Function()
			{
				@Override public Object apply(Object o)
				{
					// Get top of the job
					Map<String, PlayerJob> jobs = jobManager.getTop(finalJobName, finalPage);

					if(jobs.size() == 0)
					{
						sender.sendMessage(ChatColor.RED + "Il n'y a personne à cette page du classement");
					}

					String topList =
							ChatColor.GREEN + "---- Top " + finalJobName + ChatColor.GREEN + " ---- " + ChatColor.AQUA + ((finalPage - 1) * 10 + 1)
									+ ChatColor.GREEN + " à " + ChatColor.AQUA + (finalPage * 10) + ChatColor.GREEN
									+ " -----------------------------";

					int i = ((finalPage - 1) * 10 + 1);
					String itsMe = "";
					for(Map.Entry<String, PlayerJob> entry : jobs.entrySet())
					{
						itsMe = "";
						if(entry.getKey().equals(sender.getName()))
						{
							itsMe = ChatColor.GOLD + "" + ChatColor.BOLD + "> " + ChatColor.RESET;
						}
						if(i == 1)
						{
							topList += "\n" + ChatColor.GOLD + i + ". " + itsMe + ChatColor.GOLD + ChatColor.BOLD + entry.getKey() + "   niveau "
									+ entry.getValue().lvl + " ( " + entry.getValue().xp + " xp / " + entry.getValue().xpGoal + " xp )";
						}
						else
						{
							topList +=
									"\n" + ChatColor.GREEN + i + ". " + itsMe + ChatColor.GREEN + entry.getKey() + "   niveau " + entry.getValue().lvl
											+ " ( " + entry.getValue().xp + " xp / " + entry.getValue().xpGoal + " xp )";
						}
						i = i + 1;
					}
					sender.sendMessage(topList);

					return null;
				}
			});
		}

		return true;
	}

	private boolean hasTopPermissions(CommandSender sender)
	{
		return sender.hasPermission("*") || sender.hasPermission("nkjobs.*") || sender.hasPermission("nkjobs.stats")
				|| sender.hasPermission("nkjobs.admin");
	}
}
