package be.noki_senpai.NKjobs.listeners;

import be.noki_senpai.NKjobs.data.Job;
import be.noki_senpai.NKjobs.managers.JobManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class JobCompleter implements TabCompleter
{

	JobManager jobManager = null;
	List<String> jobs = new ArrayList<String>();

	public JobCompleter(JobManager jobManager)
	{
		this.jobManager = jobManager;

		for(Map.Entry<String, Job> job : jobManager.jobs.entrySet())
		{
			jobs.add(job.getValue().name);
		}
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
	{
		if (sender instanceof Player)
		{
			if(args.length == 1)
			{
				List<String> sub = Arrays.asList("exp", "info", "join", "leave", "leaveall", "level", "stats", "switch", "top");
				final List<String> completions = new ArrayList<>();
				org.bukkit.util.StringUtil.copyPartialMatches(args[0], sub, completions);
				Collections.sort(completions);
				return completions;
			}

			if(args.length > 1)
			{
				switch(args[0])
				{
					case "exp":
					case "level":
						if(args.length == 2)
						{
							List<String> sub = Arrays.asList("add", "remove", "set");
							final List<String> completions = new ArrayList<>();
							org.bukkit.util.StringUtil.copyPartialMatches(args[1], sub, completions);
							Collections.sort(completions);
							return completions;
						}

						if(args.length == 3)
						{
							List<String> sub = Arrays.asList("1", "10", "100", "1000", "10000", "100000");
							final List<String> completions = new ArrayList<>();
							org.bukkit.util.StringUtil.copyPartialMatches(args[2], sub, completions);
							Collections.sort(completions);
							return completions;
						}

						if(args.length == 4)
						{
							final List<String> completions = new ArrayList<>();
							org.bukkit.util.StringUtil.copyPartialMatches(args[3], jobs, completions);
							Collections.sort(completions);
							return completions;
						}
						break;
					case "info":
					case "join":
					case "leave":
					case "switch":
					case "top":
						if(args.length == 2)
						{
							final List<String> completions = new ArrayList<>();
							org.bukkit.util.StringUtil.copyPartialMatches(args[1], jobs, completions);
							Collections.sort(completions);
							return completions;
						}
						break;
				}
			}
		}
		return null;
	}
}
