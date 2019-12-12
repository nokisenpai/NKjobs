package be.noki_senpai.NKjobs.data;

import be.noki_senpai.NKjobs.NKjobs;
import be.noki_senpai.NKjobs.managers.*;
import be.noki_senpai.NKjobs.utils.SQLConnect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.type.Cocoa;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;

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
	private Timestamp tmpTime = null;
	private List<RewardedItem> rewardedItems = new ArrayList<RewardedItem>();

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
			bdd = DatabaseManager.getConnection();

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
					addOldJob(resultat.getString("name"), resultat.getInt("job_id"), resultat.getInt("lvl"), resultat.getDouble("xp"), resultat.getDouble("xp_goal"), resultat.getDouble("xp_total"), Bukkit.getPlayer(uuid), jobManager.jobs.get(resultat.getString("name")).getChatColor(), jobManager.jobs.get(resultat.getString("name")).getColorBar());
				}
				else
				{
					addJob(resultat.getString("name"), resultat.getInt("job_id"), resultat.getInt("lvl"), resultat.getDouble("xp"), resultat.getDouble("xp_goal"), resultat.getDouble("xp_total"), Bukkit.getPlayer(uuid), jobManager.jobs.get(resultat.getString("name")).getChatColor(), jobManager.jobs.get(resultat.getString("name")).getColorBar());
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

			// ######################################
			// Load items data
			// ######################################

			req = "SELECT id, item FROM " + DatabaseManager.table.ITEMS + " WHERE player_id = ?";
			ps = bdd.prepareStatement(req);
			ps.setInt(1, getId());
			resultat = ps.executeQuery();

			while(resultat.next())
			{
				YamlConfiguration restoreConfig = new YamlConfiguration();
				restoreConfig.loadFromString(resultat.getString("item"));
				rewardedItems.add(new RewardedItem(resultat.getInt("id"), restoreConfig.getItemStack("item")));
			}

			ps.close();
			resultat.close();

		}
		catch(SQLException | InvalidConfigurationException e1)
		{
			Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKjobs.PNAME + " Error while setting a player. (Error#3.004)");
			e1.printStackTrace();
		}

		checkLostRewardedItems(Bukkit.getPlayer(uuid).getInventory());
	}

	// ######################################
	// Add & Remove jobs
	// ######################################

	public void addJob(String jobName, int jobId, int lvl, double xp, double xpGoal, double xpTotal, Player player, String chatColor, String colorBar)
	{
		jobs.putIfAbsent(jobName, new PlayerJob(jobId, jobName, lvl, xp, xpGoal, xpTotal, player, chatColor, colorBar));
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

	public void addOldJob(String jobName, int jobId, int lvl, double xp, double xpGoal, double xpTotal, Player player, String chatColor, String colorBar)
	{
		oldJobs.putIfAbsent(jobName, new PlayerJob(jobId, jobName, lvl, xp, xpGoal, xpTotal, player, chatColor,colorBar ));
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

	public void save()
	{
		Connection bdd = null;
		PreparedStatement ps = null;
		String req = null;

		try
		{
			bdd = DatabaseManager.getConnection();
			for(Map.Entry<String, PlayerJob> entry : jobs.entrySet())
			{
				req = "UPDATE " + DatabaseManager.table.PLAYER_JOBS + " SET lvl = ? , xp = ? , xp_goal = ? , xp_total = ? , old = false WHERE player_id = ? AND job_id = ?";
				ps = bdd.prepareStatement(req);
				ps.setDouble(1, entry.getValue().lvl);
				ps.setDouble(2, entry.getValue().xp);
				ps.setDouble(3, entry.getValue().xpGoal);
				ps.setDouble(4, entry.getValue().xpTotal);
				ps.setInt(5, this.getId());
				ps.setInt(6, entry.getValue().id);

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
	//	getTmpTime
	// ######################################

	public Timestamp getTmpTime()
	{
		return tmpTime;
	}

	public void setTmpTime(Timestamp time)
	{
		tmpTime = time;
	}

	// ######################################
	//	reset tmpMoney & tmpExp
	// ######################################

	public void resetTmp()
	{
		tmpMoney = 0;
		tmpExp = 0;
	}

	// ######################################
	//	check for lost rewarded items
	// ######################################

	public void checkLostRewardedItems(Inventory inventory)
	{
		List<Integer> ids = new ArrayList<Integer>();

		Iterator<RewardedItem> i = rewardedItems.iterator();

		while(i.hasNext())
		{
			RewardedItem item = i.next();
			if(inventory.firstEmpty() == -1)
			{
				break;
			}
			inventory.addItem(item.getItem());
			if(item.getId() != -1)
			{
				ids.add(item.getId());
			}
			i.remove();
		}

		if(!ids.isEmpty())
		{
			Connection bdd = null;
			PreparedStatement ps = null;
			String req = null;

			try
			{
				bdd = DatabaseManager.getConnection();
				req = "DELETE FROM " + DatabaseManager.table.ITEMS + " WHERE id IN (";
				for (int deleteId : ids)
				{
					req += deleteId + ",";
				}
				req = req.substring(0, req.length() - 1);
				req += ")";
				ps = bdd.prepareStatement(req);
				ps.execute();

				ps.close();
			}
			catch(SQLException e)
			{
				Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKjobs.PNAME + " Error while deleting rewarded item.");
				e.printStackTrace();
			}
		}
	}

	public void saveRewardedItem()
	{
		if(rewardedItems.isEmpty())
		{
			return;
		}

		Connection bdd = null;
		PreparedStatement ps = null;
		String req = null;
		boolean ok = false;

		try
		{
			bdd = DatabaseManager.getConnection();
			req = "INSERT INTO " + DatabaseManager.table.ITEMS + " ( player_id , item ) VALUES ";
			for(RewardedItem rewardedItem : rewardedItems)
			{
				if(rewardedItem.getId() == -1)
				{
					YamlConfiguration itemConfig = new YamlConfiguration();
					itemConfig.set("item", rewardedItem.getItem());

					ok = true;
					req += "(" + getId() + ",'" + itemConfig.saveToString() + "'),";
				}
			}

			if(!ok)
			{
				return;
			}

			req = req.substring(0, req.length() - 1);
			ps = bdd.prepareStatement(req);
			ps.execute();

			ps.close();
		}
		catch(SQLException e)
		{
			Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKjobs.PNAME + " Error while adding rewarded item.");
			e.printStackTrace();
		}
	}

	public void addRewardedItem(RewardedItem rewardedItem)
	{
		this.rewardedItems.add(rewardedItem);
	}
}
