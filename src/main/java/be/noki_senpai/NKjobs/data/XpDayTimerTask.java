package be.noki_senpai.NKjobs.data;

import be.noki_senpai.NKjobs.managers.PlayerManager;

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
			for(Map.Entry<String, NKPlayer> player : playerManager.getPlayers().entrySet())
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
