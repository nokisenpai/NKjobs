package be.noki_senpai.NKjobs.listeners;

import be.noki_senpai.NKjobs.managers.DataRegisterManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

public class PistonMoveListener implements Listener
{
	private DataRegisterManager dataRegisterManager = null;

	public PistonMoveListener(DataRegisterManager dataRegisterManager)
	{
		this.dataRegisterManager = dataRegisterManager;
	}

	// ######################################
	// Piston extend
	// ######################################

	@EventHandler(priority = EventPriority.LOW) public void onPistonExtend(BlockPistonExtendEvent event)
	{
		if(event.isCancelled())
		{
			return;
		}

		//Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Extension de piston vers le " + event.getDirection().name());
		//Bukkit.getConsoleSender().sendMessage("" + ChatColor.GREEN + event.getBlocks().size() + " bloc(s) poussé(s)");

		int i = 1;
		for(Block block : event.getBlocks())
		{
			//Bukkit.getConsoleSender().sendMessage("" + ChatColor.GREEN + i + ". " + block.getType().name());
			i++;
			if(!dataRegisterManager.checkBlockTimer(block.getLocation()))
			{
				dataRegisterManager.moveBlockTimer(block.getLocation(),event.getDirection().name());
			}
		}
	}

	// ######################################
	// Piston retract
	// ######################################

	@EventHandler(priority = EventPriority.LOW) public void onPistonExtend(BlockPistonRetractEvent event)
	{
		if(event.isCancelled())
		{
			return;
		}

		if(!event.isSticky())
		{
			return;
		}

		//Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Rétractation de piston vers le " + event.getDirection().name());
		//Bukkit.getConsoleSender().sendMessage("" + ChatColor.GREEN + event.getBlocks().size() + " bloc(s) tiré(s)");

		int i = 1;
		for(Block block : event.getBlocks())
		{
			//Bukkit.getConsoleSender().sendMessage("" + ChatColor.GREEN + i + ". " + block.getType().name());
			i++;
			if(!dataRegisterManager.checkBlockTimer(block.getLocation()))
			{
				dataRegisterManager.moveBlockTimer(block.getLocation(),event.getDirection().getOppositeFace().name());
			}
		}
	}
}
