package be.noki_senpai.NKjobs.data;

import be.noki_senpai.NKjobs.NKjobs;
import be.noki_senpai.NKjobs.managers.*;
import be.noki_senpai.NKjobs.utils.SQLConnect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NKPlayer
{
	private int id;
	private UUID uuid;
	private String name;
	private Map<String, PlayerJob> jobs = new HashMap<>();
	private Map<String, PlayerJob> oldJobs = new HashMap<>();
	private int nbFurnace = 0;
	private int nbBrewingStand = 0;
	private double tmpMoney = 0;
	private double tmpExp = 0;

	public NKPlayer(UUID uuid, JobManager jobManager) //throws SQLException
	{
		this.uuid = uuid;
		this.name = Bukkit.getOfflinePlayer(uuid).getName();

		Connection bdd = null;
		ResultSet resultat = null;
		PreparedStatement ps = null;
		String req = null;

		try
		{
			bdd = SQLConnect.getHikariDS().getConnection();

			req = "SELECT id, name FROM " + DatabaseManager.table.PLAYERS + " WHERE uuid = ?";
			ps = bdd.prepareStatement(req);
			ps.setString(1, uuid.toString());
			resultat = ps.executeQuery();

			if(resultat.next())
			{
				id = resultat.getInt("id");
				String tmpName = resultat.getString("name");

				if(NKjobs.managePlayerDb)
				{
					// If names are differents, update in database
					if(!tmpName.equals(name))
					{
						ps.close();
						resultat.close();

						req = "UPDATE " + DatabaseManager.table.PLAYERS + " SET name = ? WHERE id = ?";
						ps = bdd.prepareStatement(req);
						ps.setString(1, name);
						ps.setInt(2, getId());

						ps.executeUpdate();
					}
				}
			}
			else
			{
				ps.close();
				resultat.close();

				req = "INSERT INTO " + DatabaseManager.table.PLAYERS + " ( uuid, name ) VALUES ( ? , ? )  ON DUPLICATE KEY UPDATE id = LAST_INSERT_ID(id)";
				ps = bdd.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, uuid.toString());
				ps.setString(2, name);
				ps.executeUpdate();
				resultat = ps.getGeneratedKeys();

				resultat.next();
				id = resultat.getInt(1);
			}

			ps.close();
			resultat.close();

			req = "SELECT name, job_id, lvl, xp, xp_goal, xp_total, p.old FROM " + DatabaseManager.table.PLAYER_JOBS + " p " + "LEFT JOIN "
					+ DatabaseManager.table.JOBS + " j " + "ON p.job_id = j.id WHERE player_id = " + id;
			ps = bdd.prepareStatement(req);
			resultat = ps.executeQuery();

			while(resultat.next())
			{
				if(resultat.getBoolean("old"))
				{
					addOldJob(resultat.getString("name"), resultat.getInt("job_id"), resultat.getInt("lvl"), resultat.getDouble("xp"), resultat.getDouble("xp_goal"), resultat.getDouble("xp_total"), Bukkit.getPlayer(uuid), jobManager.jobs.get(resultat.getString("name")).getColor());
				}
				else
				{
					addJob(resultat.getString("name"), resultat.getInt("job_id"), resultat.getInt("lvl"), resultat.getDouble("xp"), resultat.getDouble("xp_goal"), resultat.getDouble("xp_total"), Bukkit.getPlayer(uuid), jobManager.jobs.get(resultat.getString("name")).getColor());
				}

			}

			ps.close();
			resultat.close();

			req = "SELECT COUNT(id) AS count FROM " + DatabaseManager.table.FURNACES + " WHERE player_id = ? AND server_id = ?";
			ps = bdd.prepareStatement(req);
			ps.setInt(1, id);
			ps.setInt(2, DataRegisterManager.SERVERID);
			resultat = ps.executeQuery();
			if(resultat.next())
			{
				nbFurnace = resultat.getInt("count");
			}

			ps.close();
			resultat.close();

			req = "SELECT COUNT(id) AS count FROM " + DatabaseManager.table.BREWINGSTANDS + " WHERE player_id = ? AND server_id = ?";
			ps = bdd.prepareStatement(req);
			ps.setInt(1, id);
			ps.setInt(2, DataRegisterManager.SERVERID);
			resultat = ps.executeQuery();
			if(resultat.next())
			{
				nbBrewingStand = resultat.getInt("count");
			}

			ps.close();
			resultat.close();

		}
		catch(SQLException e1)
		{
			Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKjobs.PNAME + " Error while setting a player. (Error#3.004)");
			e1.printStackTrace();
		}
	}

	// ######################################
	// Add & Remove jobs
	// ######################################

	public void addJob(String jobName, int jobId, int lvl, double xp, double xpGoal, double xpTotal, Player player, String color)
	{
		jobs.putIfAbsent(jobName, new PlayerJob(jobId, jobName, lvl, xp, xpGoal, xpTotal, player, color));
	}

	public void addJob(PlayerJob job)
	{
		jobs.putIfAbsent(job.name, job);
	}

	public void removeJob(String jobName)
	{
		jobs.remove(jobName);
	}

	public void removeAllJob()
	{
		jobs.clear();
	}

	// ######################################
	// Add & Remove oldJobs
	// ######################################

	public void addOldJob(String jobName, int jobId, int lvl, double xp, double xpGoal, double xpTotal, Player player, String color)
	{
		oldJobs.putIfAbsent(jobName, new PlayerJob(jobId, jobName, lvl, xp, xpGoal, xpTotal, player, color));
	}

	public void addOldJob(PlayerJob job)
	{
		oldJobs.putIfAbsent(job.name.toLowerCase(), job);
	}

	public void removeOldJob(String jobName)
	{
		oldJobs.remove(jobName);
	}

	// ######################################
	// Getters & Setters
	// ######################################

	// Getter & Setter 'id'
	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	// Getter & Setter 'playerUUID'
	public UUID getUuid()
	{
		return uuid;
	}

	public void setUuid(UUID uuid)
	{
		this.uuid = uuid;
	}

	// Getter & Setter 'playerName'
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	// Getter 'jobs'
	public Map<String, PlayerJob> getJobs()
	{
		return jobs;
	}

	// Getter 'oldJobs'
	public Map<String, PlayerJob> getOldJobs()
	{
		return oldJobs;
	}

	public int getNbBrewingStand()
	{
		return nbBrewingStand;
	}

	public int getNbFurnace()
	{
		return nbFurnace;
	}

	public void addBrewingStand()
	{
		this.nbBrewingStand++;
	}

	public void addFurnace()
	{
		this.nbFurnace++;
	}

	public void removeBrewingStand()
	{
		this.nbBrewingStand--;
	}

	public void removeFurnace()
	{
		this.nbFurnace--;
	}

	// ######################################
	// Save function
	// ######################################

	public void save(QueueManager queueManager)
	{
		Connection bdd = null;
		PreparedStatement ps = null;
		String req = null;

		try
		{
			bdd = DatabaseManager.getConnection();
			for(Map.Entry<String, PlayerJob> entry : jobs.entrySet())
			{
				req = "UPDATE " + DatabaseManager.table.PLAYER_JOBS + " SET lvl = ? , xp = ? , xp_goal = ? , xp_total = ? WHERE job_id = ?";
				ps = bdd.prepareStatement(req);
				ps.setDouble(1, entry.getValue().lvl);
				ps.setDouble(2, entry.getValue().xp);
				ps.setDouble(3, entry.getValue().xpGoal);
				ps.setDouble(4, entry.getValue().xpTotal);
				ps.setInt(5, entry.getValue().id);

				ps.executeUpdate();
				ps.close();
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	// ######################################
	//	hasJob
	// ######################################
	public boolean hasJob(String jobName)
	{
		if(getJobs().containsKey(jobName))
		{
			return true;
		}
		return false;
	}

	// ######################################
	//	canJoinJob
	// ######################################
	public boolean canJoinJob()
	{
		if(getJobs().size() < ConfigManager.MAXJOB)
		{
			return true;
		}
		return false;
	}

	// ######################################
	//	getTmpMoney
	// ######################################

	public double getTmpMoney()
	{
		return tmpMoney;
	}

	public void addTmpMoney(double amount)
	{
		tmpMoney += amount;
	}

	// ######################################
	//	getTmpExp
	// ######################################

	public double getTmpExp()
	{
		return tmpExp;
	}

	public void addTmpExp(double amount)
	{
		tmpExp += amount;
	}

	// ######################################
	//	reset tmpMoney & tmpExp
	// ######################################

	public void resetTmp()
	{
		tmpMoney = 0;
		tmpExp = 0;
	}
}
