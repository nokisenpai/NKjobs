package ovh.lumen.NKjobs.data;

public class RegisteredBrewingStand
{
	private int id = -1;
	private double x = 0;
	private double y = 0;
	private double z = 0;
	private String playerName = "";
	private int playerId = -1;

	public RegisteredBrewingStand(int id, double x, double y, double z, String playerName, int playerId)
	{
		this.id = id;
		this.x = x;
		this.y = y;
		this.z = z;
		this.playerName = playerName;
		this.playerId = playerId;
	}

	public int getId()
	{
		return id;
	}

	public double getX()
	{
		return x;
	}

	public double getY()
	{
		return y;
	}

	public double getZ()
	{
		return z;
	}

	public String getPlayerName()
	{
		return playerName;
	}

	public int getPlayerId()
	{
		return playerId;
	}
}
