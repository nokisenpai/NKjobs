package ovh.lumen.NKjobs.registers;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import ovh.lumen.NKjobs.listeners.JobsListener;
import ovh.lumen.NKjobs.listeners.PistonMoveListener;
import ovh.lumen.NKjobs.listeners.PlayerListener;
import ovh.lumen.NKjobs.listeners.ProtectedItemListener;

import java.util.ArrayList;
import java.util.List;

public class ListenerRegister
{
	private static final List<Listener> listeners = setListeners();

	public static void registerAllListeners(JavaPlugin plugin)
	{
		listeners.forEach(listener -> {
			plugin.getServer().getPluginManager().registerEvents(listener, plugin);
		});
	}

	public static void unregisterAllListeners(JavaPlugin plugin)
	{
		HandlerList.unregisterAll(plugin);
	}

	private static List<Listener> setListeners()
	{
		List<Listener> listeners = new ArrayList<>();
		listeners.add(new PlayerListener());
		listeners.add(new JobsListener());
		listeners.add(new PistonMoveListener());
		listeners.add(new ProtectedItemListener());

		return listeners;
	}
}
