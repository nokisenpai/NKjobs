package be.noki_senpai.NKjobs.data;

import java.util.ArrayList;
import java.util.List;

public class ExploredChunk
{
	private double x = 0;
	private double z = 0;
	private List<String> playerNames = null;

	public ExploredChunk(double x, double z, List<String> playerNames)
	{
		this.x = x;
		this.z = z;
		this.playerNames = playerNames;
	}

	public double getX()
	{
		return x;
	}

	public double getZ()
	{
		return z;
	}

	public List<String> getPlayerNames()
	{
		return playerNames;
	}

	public String getPlayersToString()
	{
		String playerNamesToString = "";
		for (String name : playerNames)
		{
			playerNamesToString += name + ",";
		}
		playerNamesToString = playerNamesToString.substring(0, playerNamesToString.length() - 1);
		return playerNamesToString;
	}

	public void addPlayer(String playerName)
	{
		playerNames.add(playerName);
	}

	public boolean hasPlayer(String playerName)
	{
		return playerNames.contains(playerName);
	}

	public int getPlayersAmount()
	{
		return playerNames.size();
	}
}
