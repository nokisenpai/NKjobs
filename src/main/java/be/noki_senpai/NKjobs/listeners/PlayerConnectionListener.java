package be.noki_senpai.NKjobs.listeners;

import be.noki_senpai.NKjobs.managers.PlayerManager;
import be.noki_senpai.NKjobs.managers.QueueManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import be.noki_senpai.NKjobs.NKjobs;
import be.noki_senpai.NKjobs.data.NKPlayer;
import org.bukkit.scheduler.BukkitRunnable;;import java.util.function.Function;

public class PlayerConnectionListener implements Listener
{
	private PlayerManager playerManager = null;

	public PlayerConnectionListener(PlayerManager playerManager)
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
		}.runTaskLaterAsynchronously(NKjobs.getPlugin(), 20);
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
		}.runTaskAsynchronously(NKjobs.getPlugin());
	}
}
