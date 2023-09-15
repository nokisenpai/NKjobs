package ovh.lumen.NKjobs.enums;

public enum LogLevel
{
	DEBUG(0),
	LOG(1),
	WARN(2),
	ERROR(3);

	private final int value;

	LogLevel(int value)
	{
		this.value = value;
	}

	public String toString()
	{
		return String.valueOf(this.value);
	}

	public int value()
	{
		return this.value;
	}
}
