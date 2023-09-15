package ovh.lumen.NKjobs.listeners;

import ovh.lumen.NKjobs.managers.PlayerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import ovh.lumen.NKjobs.Main;
import org.bukkit.scheduler.BukkitRunnable;;

public class PlayerListener implements Listener
{
	private PlayerManager playerManager = null;

	public PlayerListener(PlayerManager playerManager)
	{
		this.playerManager = playerManager;
	}

	@EventHandler
	public void PlayerJoinEvent(PlayerJoinEvent event) 
	{
		new BukkitRunnable()
		{
			@Override public void run()
			{
				playerManager.addPlayer(event.getPlayer());
			}
		}.runTaskLaterAsynchronously(Main.getPlugin(), 3*20);
	}

	@EventHandler
	public void onPlayerQuitEvent(PlayerQuitEvent event) 
	{
		String playerName = event.getPlayer().getName();
		new BukkitRunnable()
		{
			@Override public void run()
			{
				playerManager.getPlayer(playerName).save();
				playerManager.getPlayer(playerName).saveRewardedItem();
				playerManager.delPlayer(event.getPlayer().getName());
			}
		}.runTaskAsynchronously(Main.getPlugin());
	}
}
