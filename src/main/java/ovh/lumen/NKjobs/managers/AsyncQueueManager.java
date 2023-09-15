package ovh.lumen.NKjobs.managers;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import ovh.lumen.NKjobs.data.NKData;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

public final class AsyncQueueManager
{
	private AsyncQueueManager() {}

	private static final Queue<Function> queue = new ConcurrentLinkedQueue<>();
	private static BukkitRunnable async = initAsync();

	private static BukkitRunnable initAsync()
	{
		BukkitRunnable async = new BukkitRunnable()
		{
			@Override
			public void run()
			{

			}
		};
		async.runTask((Plugin) NKData.PLUGIN);
		return async;
	}

	public static void addToQueue(Function async)
	{
		queue.add(async);
		apply();
	}

	private static void apply()
	{
		if(!Bukkit.getScheduler().isCurrentlyRunning(async.getTaskId()))
		{
			async = new BukkitRunnable()
			{
				@Override
				public void run()
				{
					while(!queue.isEmpty())
					{
						Function executer = queue.poll();
						executer.apply(null);
					}
				}
			};
			async.runTaskAsynchronously((Plugin) NKData.PLUGIN);
		}
	}
}
