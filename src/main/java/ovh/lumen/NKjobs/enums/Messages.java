package ovh.lumen.NKjobs.enums;

import org.bukkit.ChatColor;

public enum Messages
{
	PERMISSION_MISSING(ChatColor.RED + "Vous n'avez pas la permission."),
	ROOT_PLUGIN_INFO_MSG(ChatColor.GREEN + "%0% v%1%" + ChatColor.ITALIC + " by %2%"),
	ROOT_RELOAD_MSG(ChatColor.GREEN + "%0% a été rechargé.");

	private final String value;

	Messages(String value)
	{
		this.value = value;
	}

	public String toString()
	{
		return this.value;
	}
}
