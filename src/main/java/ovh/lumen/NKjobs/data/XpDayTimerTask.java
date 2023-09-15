package ovh.lumen.NKjobs.data;

import ovh.lumen.NKjobs.managers.PlayerManager;

import java.util.Map;
import java.util.TimerTask;

public class XpDayTimerTask extends TimerTask
{
	PlayerManager playerManager = null;
	public XpDayTimerTask(PlayerManager playerManager)
	{
		this.playerManager = playerManager;
	}

	public void run()
	{
		if(playerManager.getPlayers().size() > 0)
		{
			for(Map.Entry<String, NKWorker> player : playerManager.getPlayers().entrySet())
			{
				for(Map.Entry<String, PlayerJob> job : player.getValue().getJobs().entrySet())
				{
					job.getValue().xpDay = 0;
				}
				for(Map.Entry<String, PlayerJob> job : player.getValue().getOldJobs().entrySet())
				{
					job.getValue().xpDay = 0;
				}
			}
		}
	}
}
