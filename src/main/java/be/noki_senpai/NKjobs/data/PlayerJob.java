package be.noki_senpai.NKjobs.data;

import be.noki_senpai.NKjobs.utils.Formatter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class PlayerJob
{
	public int id = -1;
	public String name = null;
	public String formattedName = null;
	public int lvl = 0;
	public double xp = 0;
	public double xpGoal = 0;
	public double xpTotal = 0;
	public BossBar bossBar = null;
	public boolean updated = false;

	public PlayerJob(int jobId, String name, int lvl, double xp, double xpGoal, double xpTotal, Player player, String color)
	{
		this.id = jobId;
		this.name = name;
		this.formattedName = ChatColor.valueOf(color) + name.substring(0, 1).toUpperCase() + name.substring(1) + ChatColor.RESET;
		this.lvl = lvl;
		this.xp = xp;
		this.xpGoal = xpGoal;
		this.xpTotal = xpTotal;
		if(player != null && color != null)
		{
			BossBar bossBar = Bukkit.getServer().createBossBar(
					formattedName + " niv. " + lvl + " : " + Formatter.format(xp) + " / " + Formatter.format(xpGoal) + "xp", BarColor.valueOf(color), BarStyle.SEGMENTED_20);
			bossBar.setVisible(false);
			bossBar.addPlayer(player);
			bossBar.setProgress(xp / xpGoal);

			this.bossBar = bossBar;
		}
	}

	public void addExp(double xpGain)
	{
		xp += xpGain;
		xpTotal += xpGain;
		updated = true;
	}

	public void setProgressBar()
	{
		this.updated = false;
		this.bossBar.setTitle(formattedName + " niv. " + lvl + " : " + Formatter.format(xp) + " / " + Formatter.format(xpGoal) + "xp");
		this.bossBar.setProgress(xp / xpGoal);
	}

	public void displayBar()
	{
		this.bossBar.setVisible(true);
	}

	public void hideBar()
	{
		this.bossBar.setVisible(false);
	}
}
