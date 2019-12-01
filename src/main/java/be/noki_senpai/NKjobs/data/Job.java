package be.noki_senpai.NKjobs.data;

import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Job
{
	public int id;
	public String name;
	public String formattedName = null;
	private int lvlMax;
	private String color;
	private double baseLeveling;
	private double ratioLeveling;
	private double ratioExp;
	private double ratioMoney;
	private Map<String, List<JobItem>> actions = new HashMap<String, List<JobItem>>();
	private Map<Integer, Reward> rewards = new HashMap<>();

	public Job(int id, String name, int lvlMax, String color, double baseLeveling, double ratioLeveling, double ratioExp, double ratioMoney)
	{
		this.id = id;
		this.name = name;
		if(color.equals("PINK"))
		{
			color = "LIGHT_PURPLE";
		}
		if(color.equals("PURPLE"))
		{
			color = "DARK_PURPLE";
		}
		this.formattedName = ChatColor.valueOf(color) + name.substring(0, 1).toUpperCase() + name.substring(1) + ChatColor.RESET;
		this.lvlMax = lvlMax;
		this.color = color;
		this.baseLeveling = baseLeveling;
		this.ratioLeveling = ratioLeveling;
		this.ratioExp = ratioExp;
		this.ratioMoney = ratioMoney;
	}

	public void addAction(String action, List<JobItem> data)
	{
		actions.put(action, data);
	}

	public void addReward(int level, Reward reward)
	{
		rewards.put(level, reward);
	}

	public double equationLeveling(int level)
	{
		return baseLeveling + baseLeveling * level * level * level / ratioLeveling;
	}

	public double equationExp(double base, int level)
	{
		return base + base * level / ratioExp;
	}

	public double equationMoney(double base, int level)
	{
		return base + base * level / ratioMoney;
	}

	// ######################################
	// Getters & Setters
	// ######################################

	// Getter & Setter 'lvlMax'
	public int getLvlMax()
	{
		return lvlMax;
	}

	public void setLvlMax(int lvlMax)
	{
		this.lvlMax = lvlMax;
	}

	// Getter & Setter 'color'
	public String getColor()
	{
		return color;
	}

	public void setColor(String color)
	{
		this.color = color;
	}

	// Getter & Setter 'baseLeveling'
	public double getBaseLeveling()
	{
		return baseLeveling;
	}

	public void setBaseLeveling(double baseLeveling)
	{
		this.baseLeveling = baseLeveling;
	}

	// Getter & Setter 'ratioLeveling'
	public double getRatioLeveling()
	{
		return ratioLeveling;
	}

	public void setRatioLeveling(double ratioLeveling)
	{
		this.ratioLeveling = ratioLeveling;
	}

	// Getter & Setter 'ratioExp'
	public double getRatioExp()
	{
		return ratioExp;
	}

	public void setRatioExp(double ratioExp)
	{
		this.ratioExp = ratioExp;
	}

	// Getter & Setter 'ratioMoney'
	public double getRatioMoney()
	{
		return ratioMoney;
	}

	public void setRatioMoney(double ratioMoney)
	{
		this.ratioMoney = ratioMoney;
	}

	// Getter & Setter 'actions'
	public Map<String, List<JobItem>> getActions()
	{
		return actions;
	}

	public void setActions(Map<String, List<JobItem>> actions)
	{
		this.actions = actions;
	}

	// Getter & Setter 'rewards'
	public Map<Integer, Reward> getRewards()
	{
		return rewards;
	}

	public void setRewards(Map<Integer, Reward> rewards)
	{
		this.rewards = rewards;
	}
}
