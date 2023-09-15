package ovh.lumen.NKjobs.data;

public class JobItem
{
	
	private String name;
	private double exp;
	private double money;
	
	public JobItem(String name, double exp, double money)
	{
		this.name = name;
		this.exp = exp;
		this.money = money;
	}

	// ######################################
	// Getters & Setters
	// ######################################

	// Getter & Setter 'name'
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	// Getter & Setter 'exp'
	public double getExp()
	{
		return exp;
	}

	public void setExp(float exp)
	{
		this.exp = exp;
	}

	// Getter & Setter 'money'
	public double getMoney()
	{
		return money;
	}

	public void setName(double money)
	{
		this.money = money;
	}
}
