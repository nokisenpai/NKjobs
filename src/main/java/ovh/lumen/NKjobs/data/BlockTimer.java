package ovh.lumen.NKjobs.data;

import java.sql.Timestamp;

public class BlockTimer
{
	private double x = 0;
	private double y = 0;
	private double z = 0;
	private Timestamp time = null;

	public BlockTimer(double x, double y, double z, Timestamp time)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.time = time;
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

	public Timestamp getTime()
	{
		return time;
	}
}
