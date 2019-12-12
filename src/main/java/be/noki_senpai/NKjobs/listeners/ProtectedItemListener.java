package be.noki_senpai.NKjobs.listeners;

import be.noki_senpai.NKjobs.NKjobs;
import be.noki_senpai.NKjobs.data.RewardedItem;
import be.noki_senpai.NKjobs.managers.DataRegisterManager;
import be.noki_senpai.NKjobs.managers.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

public class ProtectedItemListener implements Listener
{
	private PlayerManager playerManager = null;

	public ProtectedItemListener(PlayerManager playerManager)
	{
		this.playerManager = playerManager;
	}

	// ######################################
	// on Drop item
	// ######################################

	@EventHandler(priority = EventPriority.LOW)
	public void onDropItem(PlayerDropItemEvent event)
	{
		if(event.isCancelled())
		{
			return;
		}

		if(event.getItemDrop().getItemStack().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(NKjobs.getPlugin(), "NKprotect"), PersistentDataType.INTEGER))
		{
			event.setCancelled(true);
		}
	}

	// ######################################
	// on Death
	// ######################################

	@EventHandler(priority = EventPriority.LOW)
	public void onDeath(PlayerDeathEvent event)
	{
		List<ItemStack> l = event.getDrops();
		Iterator<ItemStack> i = l.iterator();

		while(i.hasNext())
		{
			ItemStack item = i.next();
			if(item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(NKjobs.getPlugin(), "NKprotect"), PersistentDataType.INTEGER))
			{
				i.remove();
				playerManager.getPlayer(event.getEntity().getName()).addRewardedItem(new RewardedItem(-1, item));
			}
		}
	}

	// ######################################
	// on Revive
	// ######################################

	@EventHandler(priority = EventPriority.LOW)
	public void onDeath(PlayerRespawnEvent event)
	{
		playerManager.getPlayer(event.getPlayer().getName()).checkLostRewardedItems(event.getPlayer().getInventory());
	}
}
