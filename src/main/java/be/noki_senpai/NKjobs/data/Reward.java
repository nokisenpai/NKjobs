package be.noki_senpai.NKjobs.data;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Reward
{
	private List<ItemStack> items = null;
	private List<String> commands = null;
	private List<String> permissions = null;

	public Reward(List<ItemStack> items, List<String> commands, List<String> permissions)
	{
		this.items = items;
		this.commands = commands;
		this.permissions = permissions;
	}

	public List<ItemStack> getItems()
	{
		return items;
	}

	public List<String> getCommands()
	{
		return commands;
	}

	public List<String> getPermission()
	{
		return permissions;
	}

}
