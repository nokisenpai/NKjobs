package ovh.lumen.NKjobs.completers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RootCompleter implements TabCompleter
{
	public RootCompleter()
	{

	}
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args)
	{
		if(args.length <= 1)
		{
			List<String> tmp = new ArrayList<>();
			tmp.add("reload");

			final List<String> completions = new ArrayList<>();
			org.bukkit.util.StringUtil.copyPartialMatches(args[0], tmp, completions);
			Collections.sort(completions);
			return completions;
		}

		return null;
	}
}