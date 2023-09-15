package ovh.lumen.NKjobs.commands;

import ovh.lumen.NKjobs.commands.Jobs.*;
import ovh.lumen.NKjobs.managers.JobManager;
import ovh.lumen.NKjobs.managers.PlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import ovh.lumen.NKjobs.Main;

public class JobsCmd implements CommandExecutor
{
	private QueueManager queueManager = null;
	private PlayerManager playerManager = null;
	private JobManager jobManager = null;

	public final static String usageCmdJoin = ChatColor.GREEN + "> /jobs join " + ChatColor.RED + "<job>";
	public final static String usageAdminCmdJoin = " " + ChatColor.BLUE + "[joueur]";

	public final static String usageCmdLeave = ChatColor.GREEN + "> /jobs leave " + ChatColor.RED + "<job>";
	public final static String usageAdminCmdLeave = " " + ChatColor.BLUE + "[joueur]";

	public final static String usageCmdLeaveall = ChatColor.GREEN + "> /jobs leaveall";
	public final static String usageAdminCmdLeaveall = " " + ChatColor.BLUE + "[joueur]";

	public final static String usageCmdSwitch = ChatColor.GREEN + "> /jobs switch " + ChatColor.RED + "<job>";
	public final static String usageAdminCmdSwitch = " " + ChatColor.BLUE + "[joueur]";

	public final static String usageCmdInfo = ChatColor.GREEN + "> /jobs info " + ChatColor.RED + "<job>";

	public final static String usageCmdStats = ChatColor.GREEN + "> /jobs stats";
	public final static String usageAdminCmdStats = " " + ChatColor.BLUE + "[joueur]";

	public final static String usageCmdTop = ChatColor.GREEN + "> /jobs top " + ChatColor.RED + "<job>";

	public final static String usageCmdLevel = ChatColor.GREEN + "> /jobs level " + ChatColor.RED + "<add|remove|set> <amount> <job>";
	public final static String usageAdminCmdLevel =  " " + ChatColor.BLUE + "[joueur]";

	public final static String usageCmdExp = ChatColor.GREEN + "> /jobs exp " + ChatColor.RED + "<add|remove|set> <amount> <job>";
	public final static String usageAdminCmdExp = " " + ChatColor.BLUE + "[joueur]";

	public JobsCmd(QueueManager queueManager, PlayerManager playerManager, JobManager jobManager)
	{
		this.queueManager = queueManager;
		this.playerManager = playerManager;
		this.jobManager = jobManager;
	}

	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args)
	{

		// if no argument
		if (args.length == 0)
		{
			sender.sendMessage(usageCmd(sender));
			return true;
		}

		args[0] = args[0].toLowerCase();
		switch (args[0])
		{
			case "join":
				return new Join(queueManager, playerManager, jobManager).join(sender, args);
			case "leave":
				return new Leave(queueManager, playerManager, jobManager).leave(sender, args);
			case "leaveall":
				return new Leaveall(queueManager, playerManager, jobManager).leaveall(sender, args);
			case "switch":
				return new Switch(queueManager, playerManager, jobManager).switch_(sender, args);
			case "info":
				return new Info(queueManager, playerManager, jobManager).info(sender, args);
			case "stats":
				return new Stats(queueManager, playerManager).stats(sender, args);
			case "top":
				return new Top(queueManager, jobManager).top(sender, args);
			case "level":
				return new Level(queueManager, playerManager, jobManager).level(sender, args);
			case "exp":
				return new Exp(queueManager, playerManager, jobManager).exp(sender, args);
			default:
				sender.sendMessage(usageCmd(sender));
				return true;
		}
	}

	private String usageCmd(CommandSender sender)
	{
		String usageCmd = "\n" + ChatColor.GREEN + "Liste des commandes pour " + ChatColor.RED + Main.PNAME + ChatColor.GREEN + "\n----------------------------------------------------";

		if(sender.hasPermission("*") || sender.hasPermission("nkjobs.*") || sender.hasPermission("nkjobs.admin"))
		{
			usageCmd += "\n" + usageCmdJoin + usageAdminCmdJoin;
			usageCmd += "\n" + usageCmdLeave + usageAdminCmdLeave;
			usageCmd += "\n" + usageCmdLeaveall + usageAdminCmdLeaveall;
			usageCmd += "\n" + usageCmdSwitch + usageAdminCmdSwitch;
			usageCmd += "\n" + usageCmdInfo;
			usageCmd += "\n" + usageCmdStats + usageAdminCmdStats;
			usageCmd += "\n" + usageCmdTop;
			usageCmd += "\n" + usageCmdLevel + usageAdminCmdLevel;
			usageCmd += "\n" + usageCmdExp + usageAdminCmdExp;
		}
		else
		{
			if(sender.hasPermission("nkjobs.join"))
			{
				usageCmd += "\n" + usageCmdJoin;
				if(sender.hasPermission("nkjobs.join.other"))
				{
					usageCmd += usageAdminCmdJoin;
				}
			}

			if(sender.hasPermission("nkjobs.leave"))
			{
				usageCmd += "\n" + usageCmdLeave;
				if(sender.hasPermission("nkjobs.leave.other"))
				{
					usageCmd += usageAdminCmdLeave;
				}
			}

			if(sender.hasPermission("nkjobs.leaveall"))
			{
				usageCmd += "\n" + usageCmdLeaveall;
				if(sender.hasPermission("nkjobs.leaveall.other"))
				{
					usageCmd += usageAdminCmdLeaveall;
				}
			}

			if(sender.hasPermission("nkjobs.switch"))
			{
				usageCmd += "\n" + usageCmdSwitch;
				if(sender.hasPermission("nkjobs.switch.other"))
				{
					usageCmd += usageAdminCmdSwitch;
				}
			}

			if(sender.hasPermission("nkjobs.info"))
			{
				usageCmd += "\n" + usageCmdInfo;
			}

			if(sender.hasPermission("nkjobs.stats"))
			{
				usageCmd += "\n" + usageCmdStats;
				if(sender.hasPermission("nkjobs.stats.other"))
				{
					usageCmd += usageAdminCmdStats;
				}
			}

			if(sender.hasPermission("nkjobs.top"))
			{
				usageCmd += "\n" + usageCmdTop;
			}

			if(sender.hasPermission("nkjobs.level"))
			{
				usageCmd += "\n" + usageCmdLevel;
				if(sender.hasPermission("nkjobs.level.other"))
				{
					usageCmd += usageAdminCmdLevel;
				}
			}

			if(sender.hasPermission("nkjobs.exp"))
			{
				usageCmd += "\n" + usageCmdExp;
				if(sender.hasPermission("nkjobs.exp.other"))
				{
					usageCmd += usageAdminCmdExp;
				}
			}
		}

		usageCmd += "\n----------------------------------------------------";

		return usageCmd;
	}
}
