package ovh.lumen.NKjobs.utils;

import org.bukkit.entity.Player;

public class Permission
{
	//Thanks PlotSquared **
	public static int hasPermissionRange(Player player, String stub, int range)
	{
		if(player.hasPermission("NKjobs.admin"))
		{
			return Integer.MAX_VALUE;
		}

		String[] nodes = stub.split("\\.");
		StringBuilder n = new StringBuilder();

		for(int i = 0; i < (nodes.length - 1); i++)
		{
			n.append(nodes[i]).append(".");

			if(!stub.equals(n + "*"))
			{
				if(player.hasPermission(n + "*"))
				{
					return Integer.MAX_VALUE;
				}
			}
		}

		if(player.hasPermission(stub + ".*"))
		{
			return Integer.MAX_VALUE;
		}

		for(int i = range; i > 0; i--)
		{
			if(player.hasPermission(stub + "." + i))
			{
				return i;
			}
		}

		return 0;
	}
}
