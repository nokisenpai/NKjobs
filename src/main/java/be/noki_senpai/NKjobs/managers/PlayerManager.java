package be.noki_senpai.NKjobs.managers;

import be.noki_senpai.NKjobs.NKjobs;
import be.noki_senpai.NKjobs.data.Job;
import be.noki_senpai.NKjobs.data.JobItem;
import be.noki_senpai.NKjobs.data.NKPlayer;
import be.noki_senpai.NKjobs.data.PlayerJob;
import be.noki_senpai.NKjobs.utils.Formatter;
import be.noki_senpai.NKjobs.utils.SQLConnect;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

public class PlayerManager
{
	// Players datas
	private Map<String, NKPlayer> players = null;
	private ConsoleCommandSender console = null;
	private JobManager jobManager = null;

	public PlayerManager(JobManager jobManager)
	{
		this.players = new TreeMap<String, NKPlayer>(String.CASE_INSENSITIVE_ORDER);
		this.console = Bukkit.getConsoleSender();
		this.jobManager = jobManager;

		new BukkitRunnable()
		{
			@Override public void run()
			{
				saveAll();
			}
		}.runTaskTimerAsynchronously(NKjobs.getPlugin(), 0, 120 * 20);

		new BukkitRunnable()
		{
			@Override public void run()
			{
				giveGain();
			}
		}.runTaskTimerAsynchronously(NKjobs.getPlugin(), 0, 3 * 20);
	}

	public void loadPlayer()
	{
		// Get all connected players
		Bukkit.getOnlinePlayers().forEach(player -> players.put(player.getName(), new NKPlayer(player.getUniqueId(), jobManager)));
	}

	public void unloadPlayer()
	{
		players.clear();
	}

	public void addPlayer(Player player)
	{
		players.put(player.getName(), new NKPlayer(player.getUniqueId(), jobManager));
	}

	public void delPlayer(String playerName)
	{
		players.remove(playerName);
	}

	// **************************************
	// **************************************
	// Jobs functions
	// **************************************
	// **************************************

	// ######################################
	// Save all players jobs
	// ######################################

	public void saveAll()
	{
		if(players.size() > 0)
		{
			Connection bdd = null;
			PreparedStatement ps = null;
			String req = "INSERT INTO " + DatabaseManager.table.PLAYER_JOBS + " ( player_id, job_id, lvl, xp, xp_goal, xp_total, old) VALUES ";
			boolean ok = false;
			for(Map.Entry<String, NKPlayer> player : players.entrySet())
			{
				for(Map.Entry<String, PlayerJob> jobs : player.getValue().getJobs().entrySet())
				{
					ok = true;
					req += "(" + player.getValue().getId() + " , " + jobs.getValue().id + " , " + jobs.getValue().lvl + " , " + jobs.getValue().xp
							+ " , " + jobs.getValue().xpGoal + " , " + jobs.getValue().xpTotal + " , false ),";
				}
			}
			if(!ok)
			{
				return;
			}
			req = req.substring(0, req.length() - 1);
			req += " ON DUPLICATE KEY UPDATE lvl = VALUES(lvl), xp = VALUES(xp), xp_goal = VALUES(xp_goal),"
					+ " xp_total = VALUES(xp_total)";
			try
			{
				bdd = DatabaseManager.getConnection();
				ps = bdd.prepareStatement(req);
				ps.executeUpdate();
				ps.close();
			}
			catch(SQLException e)
			{
				console.sendMessage(ChatColor.GOLD + req);
				e.printStackTrace();
			}
		}
	}

	// ######################################
	// Give stacked gains
	// ######################################

	public void giveGain()
	{
		if(players.size() > 0)
		{
			for(Map.Entry<String, NKPlayer> player : players.entrySet())
			{
				if(player.getValue().getTmpTime() != null)
				{
					Date now = new java.sql.Date(System.currentTimeMillis());
					if(player.getValue().getTmpTime().after(now))
					{
						Bukkit.getPlayer(player.getValue().getUuid()).spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder(
								ChatColor.YELLOW + "Cette action ne rapporte rien avant " + ChatColor.RED + ((player.getValue().getTmpTime().getTime()-now.getTime())/1000) + ChatColor.YELLOW + " secondes.").create());
						player.getValue().setTmpTime(null);
					}
				}
				else if(player.getValue().getTmpMoney() != 0)
				{
					NKjobs.getPlugin().getEconomy().depositPlayer(Bukkit.getOfflinePlayer(player.getValue().getUuid()), player.getValue().getTmpMoney());
					Bukkit.getPlayer(player.getValue().getUuid()).spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder(
							ChatColor.YELLOW + "+" + NKjobs.getPlugin().getEconomy().format(player.getValue().getTmpMoney()) + ChatColor.BLUE +  "   +" + Formatter.formatMoney(player.getValue().getTmpExp()) + " XP").create());
					player.getValue().resetTmp();
				}
				for(Map.Entry<String, PlayerJob> job : player.getValue().getJobs().entrySet())
				{
					if(job.getValue().updated)
					{
						job.getValue().setProgressBar();
						job.getValue().displayBar();
					}
					else
					{
						job.getValue().hideBar();
					}
				}
			}
		}
	}
	// ######################################
	//	getPlayer
	// ######################################

	public NKPlayer getPlayer(String playerName)
	{
		if(players.containsKey(playerName))
		{
			return players.get(playerName);
		}
		else
		{
			Connection bdd = null;
			ResultSet resultat = null;
			PreparedStatement ps = null;
			String req = null;

			try
			{
				bdd = DatabaseManager.getConnection();

				req = "SELECT id, name, uuid FROM " + DatabaseManager.common.PLAYERS + " WHERE name = ?";
				ps = bdd.prepareStatement(req);
				ps.setString(1, playerName);
				resultat = ps.executeQuery();

				if(resultat.next())
				{
					return new NKPlayer(UUID.fromString(resultat.getString("uuid")), jobManager);
				}
			}
			catch(SQLException e1)
			{
				Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKjobs.PNAME + " Error while getting a player.");
			}
		}
		return null;
	}

	// ######################################
	//	Join jobs
	// ######################################

	private void onlineJoinJob(NKPlayer player, String jobName, boolean crossServer)
	{
		if(player.getOldJobs().get(jobName) != null)
		{
			player.addJob(player.getOldJobs().get(jobName));
			player.removeOldJob(jobName);
		}
		else
		{
			player.addJob(jobName, jobManager.jobs.get(jobName).id, 1, 0.0, jobManager.jobs.get(jobName).equationLeveling(1), 0, Bukkit.getPlayer(player.getUuid()), jobManager.jobs.get(jobName).getChatColor(), jobManager.jobs.get(jobName).getColorBar());
		}

		if(crossServer && Bukkit.getPlayer(player.getUuid()) != null)
		{
			Bukkit.getPlayer(player.getUuid()).sendMessage(ChatColor.GREEN + "Vous êtes maintenant " + jobName);
		}
	}

	private void offlineJoinJob(NKPlayer player, String jobName)
	{
		Connection bdd = null;
		PreparedStatement ps = null;
		String req = null;
		try
		{
			bdd = DatabaseManager.getConnection();
			req = "INSERT INTO " + DatabaseManager.table.PLAYER_JOBS
					+ " ( player_id, job_id, lvl, xp, xp_goal, xp_total, old) VALUES (? , ? , 0 , 0 , 0 , 0 , false ) ON DUPLICATE KEY UPDATE "
					+ "old = false";
			ps = bdd.prepareStatement(req);
			ps.setInt(1, player.getId());
			ps.setInt(2, jobManager.jobs.get(jobName).id);
			ps.executeUpdate();
			ps.close();

		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	private void crossServerJoinJob(final String playerName, String jobName)
	{
		String server = getOtherServer(playerName);
		if(server != null)
		{
			if(players.size() != 0)
			{
				ByteArrayDataOutput out = ByteStreams.newDataOutput();
				out.writeUTF("Forward"); // So BungeeCord knows to forward it
				out.writeUTF(server.toUpperCase());
				out.writeUTF("NKjobs");

				ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
				DataOutputStream msgout = new DataOutputStream(msgbytes);
				try
				{
					msgout.writeUTF("join|" + playerName + "|" + jobName + "|null" + "|null"); // You can do
				}
				catch(IOException exception)
				{
					exception.printStackTrace();
				}

				out.writeShort(msgbytes.toByteArray().length);
				out.write(msgbytes.toByteArray());

				Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);

				player.sendPluginMessage(NKjobs.getPlugin(), "BungeeCord", out.toByteArray());
			}
			else
			{
				Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKjobs.PNAME + " " + playerName
						+ " est connecté(e) sur un autre serveur. Utilisez la console de ce serveur pour lui ajouter un métier.");
			}
		}
	}

	public void joinJob(NKPlayer player, String jobName, boolean crossServer)
	{
		if(!crossServer)
		{
			offlineJoinJob(player, jobName);
		}
		if(players.containsKey(player.getName()))
		{
			onlineJoinJob(player, jobName, crossServer);
		}
		else if(!crossServer)
		{
			crossServerJoinJob(player.getName(), jobName);
		}
	}

	// ######################################
	//	Leave jobs
	// ######################################

	private void onlineLeaveJob(NKPlayer player, String jobName, boolean crossServer)
	{
		player.addOldJob(player.getJobs().get(jobName));
		player.removeJob(jobName);

		if(crossServer && Bukkit.getPlayer(player.getUuid()) != null)
		{
			Bukkit.getPlayer(player.getUuid()).sendMessage(ChatColor.GREEN + "Vous n'êtes plus " + jobName);
		}
	}

	private void offlineLeaveJob(NKPlayer player, String jobName)
	{
		Connection bdd = null;
		PreparedStatement ps = null;
		String req = null;

		try
		{
			PlayerJob job = player.getJobs().get(jobName);

			bdd = DatabaseManager.getConnection();

			req = "UPDATE " + DatabaseManager.table.PLAYER_JOBS
					+ " SET lvl = ? , xp = ? , xp_goal = ? , xp_total = ? , old = true WHERE player_id = ? AND job_id = ?";
			ps = bdd.prepareStatement(req);
			ps.setInt(1, job.lvl);
			ps.setDouble(2, job.xp);
			ps.setDouble(3, job.xpTotal);
			ps.setDouble(4, job.xpGoal);
			ps.setInt(5, player.getId());
			ps.setInt(6, jobManager.jobs.get(jobName).id);

			ps.executeUpdate();
			ps.close();

		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	private void crossServerLeaveJob(final String playerName, String jobName)
	{
		String server = getOtherServer(playerName);
		if(server != null)
		{
			if(players.size() != 0)
			{
				ByteArrayDataOutput out = ByteStreams.newDataOutput();
				out.writeUTF("Forward"); // So BungeeCord knows to forward it
				out.writeUTF(server.toUpperCase());
				out.writeUTF("NKjobs");

				ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
				DataOutputStream msgout = new DataOutputStream(msgbytes);
				try
				{
					msgout.writeUTF("leave|" + playerName + "|" + jobName + "|null" + "|null"); // You can do
				}
				catch(IOException exception)
				{
					exception.printStackTrace();
				}

				out.writeShort(msgbytes.toByteArray().length);
				out.write(msgbytes.toByteArray());

				Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);

				player.sendPluginMessage(NKjobs.getPlugin(), "BungeeCord", out.toByteArray());
			}
			else
			{
				Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKjobs.PNAME + " " + playerName
						+ " est connecté(e) sur un autre serveur. Utilisez la console de ce serveur pour lui supprimer un métier.");
			}
		}
	}

	public void leaveJob(NKPlayer player, String jobName, boolean crossServer)
	{
		if(!crossServer)
		{
			offlineLeaveJob(player, jobName);
		}
		if(players.containsKey(player.getName()))
		{
			onlineLeaveJob(player, jobName, crossServer);
		}
		else if(!crossServer)
		{
			crossServerLeaveJob(player.getName(), jobName);
		}
	}

	// ######################################
	//	Leave all jobs
	// ######################################

	private void onlineLeaveAllJob(NKPlayer player, boolean crossServer)
	{
		for(Map.Entry<String, PlayerJob> jobs : player.getJobs().entrySet())
		{
			player.addOldJob(jobs.getValue());
		}
		player.removeAllJob();
		if(crossServer && Bukkit.getPlayer(player.getUuid()) != null)
		{
			Bukkit.getPlayer(player.getUuid()).sendMessage(ChatColor.GREEN + "Vous n'avez plus aucun métier.");
		}
	}

	private void offlineLeaveAllJob(NKPlayer player)
	{
		Connection bdd = null;
		PreparedStatement ps = null;
		String req = "INSERT INTO " + DatabaseManager.table.PLAYER_JOBS + " ( player_id, job_id, lvl, xp, xp_goal, xp_total, old) VALUES ";

		for(Map.Entry<String, PlayerJob> jobs : player.getJobs().entrySet())
		{
			req += "(" + player.getId() + " , " + jobs.getValue().id + " , " + jobs.getValue().lvl + " , " + jobs.getValue().xp + " , "
					+ jobs.getValue().xpGoal + " , " + jobs.getValue().xpTotal + " , true ),";
		}

		req = req.substring(0, req.length() - 1);
		req += " ON DUPLICATE KEY UPDATE lvl = VALUES(lvl), xp = VALUES(xp), xp_goal = VALUES(xp_goal)," + "xp_total = VALUES(xp_total), old = true";
		try
		{
			bdd = DatabaseManager.getConnection();
			ps = bdd.prepareStatement(req);
			ps.executeUpdate();
			ps.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	private void crossServerLeaveAllJob(final String playerName)
	{
		String server = getOtherServer(playerName);
		if(server != null)
		{
			if(players.size() != 0)
			{
				ByteArrayDataOutput out = ByteStreams.newDataOutput();
				out.writeUTF("Forward"); // So BungeeCord knows to forward it
				out.writeUTF(server.toUpperCase());
				out.writeUTF("NKjobs");

				ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
				DataOutputStream msgout = new DataOutputStream(msgbytes);
				try
				{
					msgout.writeUTF("leaveall|" + playerName + "|null|null" + "|null"); // You can do
				}
				catch(IOException exception)
				{
					exception.printStackTrace();
				}

				out.writeShort(msgbytes.toByteArray().length);
				out.write(msgbytes.toByteArray());

				Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);

				player.sendPluginMessage(NKjobs.getPlugin(), "BungeeCord", out.toByteArray());
			}
			else
			{
				Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKjobs.PNAME + " " + playerName
						+ " est connecté(e) sur un autre serveur. Utilisez la console de ce serveur pour lui supprimer tout ses métiers.");
			}
		}
	}

	public void leaveAllJob(NKPlayer player, boolean crossServer)
	{
		if(!crossServer)
		{
			offlineLeaveAllJob(player);
		}
		if(players.containsKey(player.getName()))
		{
			onlineLeaveAllJob(player, crossServer);
		}
		else if(!crossServer)
		{
			crossServerLeaveAllJob(player.getName());
		}
	}

	// ######################################
	//	Switch jobs
	// ######################################

	private void onlineSwitchJob(NKPlayer player, String jobName, boolean crossServer)
	{
		// Remove all jobs
		for(Map.Entry<String, PlayerJob> jobs : player.getJobs().entrySet())
		{
			player.addOldJob(jobs.getValue());
		}
		player.removeAllJob();

		// If player has old job then get it
		if(player.getOldJobs().get(jobName) != null)
		{
			player.addJob(player.getOldJobs().get(jobName));
			player.removeOldJob(jobName);
		}
		else
		{
			player.addJob(jobName, jobManager.jobs.get(jobName).id, 1, 0.0, jobManager.jobs.get(jobName).equationLeveling(1), 0, Bukkit.getPlayer(player.getUuid()), jobManager.jobs.get(jobName).getChatColor(), jobManager.jobs.get(jobName).getColorBar());
		}

		if(crossServer && Bukkit.getPlayer(player.getUuid()) != null)
		{
			Bukkit.getPlayer(player.getUuid()).sendMessage(ChatColor.GREEN + "Vous êtes maintenant " + jobManager.jobs.get(jobName).formattedName);
		}
	}

	private void offlineSwitchJob(NKPlayer player, String jobName)
	{

		Connection bdd = null;
		PreparedStatement ps = null;
		String req = null;

		try
		{
			bdd = DatabaseManager.getConnection();

			if(player.getJobs().size() != 0)
			{
				// Leave all job
				req = "INSERT INTO " + DatabaseManager.table.PLAYER_JOBS + " ( player_id, job_id, lvl, xp, xp_goal, xp_total, old) VALUES ";

				for(Map.Entry<String, PlayerJob> jobs : player.getJobs().entrySet())
				{
					req += "(" + player.getId() + " , " + jobs.getValue().id + " , " + jobs.getValue().lvl + " , " + jobs.getValue().xp + " , " + jobs.getValue().xpGoal + " , " + jobs.getValue().xpTotal + " , true ),";
				}

				req = req.substring(0, req.length() - 1);
				req += " ON DUPLICATE KEY UPDATE lvl = VALUES(lvl), xp = VALUES(xp), xp_goal = VALUES(xp_goal)," + "xp_total = VALUES(xp_total), old = true";
				ps = bdd.prepareStatement(req);
				ps.executeUpdate();
				ps.close();
			}

			// If player has old job then set 'old' to false
			req = "INSERT INTO " + DatabaseManager.table.PLAYER_JOBS
					+ " ( player_id, job_id, lvl, xp, xp_goal, xp_total, old) VALUES (? , ? , 0 , 0 , 0 , 0 , false ) ON DUPLICATE KEY UPDATE "
					+ "old = false";
			ps = bdd.prepareStatement(req);
			ps.setInt(1, player.getId());
			ps.setInt(2, jobManager.jobs.get(jobName).id);
			ps.executeUpdate();
			ps.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	private void crossServerSwitchJob(final String playerName, String jobName)
	{
		String server = getOtherServer(playerName);
		if(server != null)
		{
			if(players.size() != 0)
			{
				ByteArrayDataOutput out = ByteStreams.newDataOutput();
				out.writeUTF("Forward"); // So BungeeCord knows to forward it
				out.writeUTF(server.toUpperCase());
				out.writeUTF("NKjobs");

				ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
				DataOutputStream msgout = new DataOutputStream(msgbytes);
				try
				{
					msgout.writeUTF("switch|" + playerName + "|" + jobName + "|null" + "|null"); // You can do
				}
				catch(IOException exception)
				{
					exception.printStackTrace();
				}

				out.writeShort(msgbytes.toByteArray().length);
				out.write(msgbytes.toByteArray());

				Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);

				player.sendPluginMessage(NKjobs.getPlugin(), "BungeeCord", out.toByteArray());
			}
			else
			{
				Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKjobs.PNAME + " " + playerName
						+ " est connecté(e) sur un autre serveur. Utilisez la console de ce serveur pour lui supprimer un métier.");
			}
		}
	}

	public void switchJob(NKPlayer player, String jobName, boolean crossServer)
	{
		if(!crossServer)
		{
			offlineSwitchJob(player, jobName);
		}
		if(players.containsKey(player.getName()))
		{
			onlineSwitchJob(player, jobName, crossServer);
		}
		else if(!crossServer)
		{
			crossServerSwitchJob(player.getName(), jobName);
		}
	}

	// ######################################
	//	Exp jobs
	// ######################################

	private void onlineExpJob(NKPlayer player, String jobName, Double amount, String subCommand)
	{
		switch(subCommand)
		{
			case "add": jobManager.addXp(player, jobName, amount);
				break;
			case "remove": jobManager.removeXp(player, jobName, amount);
				break;
			case "set": jobManager.setXp(player, jobName, amount);
				break;
		}

		if(Bukkit.getPlayer(player.getUuid()) != null)
		{
			switch(subCommand)
			{
				case "add":
					Bukkit.getPlayer(player.getUuid()).sendMessage(ChatColor.GREEN + "Vous avez reçu " + amount + " xp pour votre métier " + jobName);
					break;
				case "remove":
					Bukkit.getPlayer(player.getUuid()).sendMessage(
							ChatColor.GREEN + "Vous avez perdu " + amount + " xp dans votre métier " + jobName);
					break;
				case "set":
					Bukkit.getPlayer(player.getUuid()).sendMessage(
							ChatColor.GREEN + "Votre xp a été défini à " + amount + " pour votre métier " + jobName);
					break;
			}
		}
	}

	private void offlineExpJob(NKPlayer player, String jobName, Double amount, String subCommand)
	{
		Connection bdd = null;
		PreparedStatement ps = null;
		String req = null;

		try
		{
			PlayerJob job = player.getJobs().get(jobName);

			bdd = DatabaseManager.getConnection();

			req = "UPDATE " + DatabaseManager.table.PLAYER_JOBS
					+ " SET lvl = ? , xp = ? , xp_goal = ? , xp_total = ? WHERE player_id = ? AND job_id = ?";
			ps = bdd.prepareStatement(req);
			ps.setInt(1, job.lvl);
			ps.setDouble(2, job.xp);
			ps.setDouble(3, job.xpTotal);
			ps.setDouble(4, job.xpGoal);
			ps.setInt(5, player.getId());
			ps.setInt(6, jobManager.jobs.get(jobName).id);

			ps.executeUpdate();
			ps.close();

		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	private void crossServerExpJob(final String playerName, String jobName, Double amount, String subCommand)
	{
		String server = getOtherServer(playerName);
		if(server != null)
		{
			if(players.size() != 0)
			{
				ByteArrayDataOutput out = ByteStreams.newDataOutput();
				out.writeUTF("Forward"); // So BungeeCord knows to forward it
				out.writeUTF(server.toUpperCase());
				out.writeUTF("NKjobs");

				ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
				DataOutputStream msgout = new DataOutputStream(msgbytes);
				try
				{
					msgout.writeUTF("exp|" + playerName + "|" + jobName + "|" + amount + "|" + subCommand); // You can do
				}
				catch(IOException exception)
				{
					exception.printStackTrace();
				}

				out.writeShort(msgbytes.toByteArray().length);
				out.write(msgbytes.toByteArray());

				Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);

				player.sendPluginMessage(NKjobs.getPlugin(), "BungeeCord", out.toByteArray());
			}
			else
			{
				Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKjobs.PNAME + " " + playerName
						+ " est connecté(e) sur un autre serveur. Utilisez la console de ce serveur pour lui supprimer un métier.");
			}
		}
	}

	public void expJob(NKPlayer player, String jobName, Double amount, String subCommand, boolean crossServer)
	{
		if(!crossServer)
		{
			offlineExpJob(player, jobName, amount, subCommand);
		}
		if(players.containsKey(player.getName()))
		{
			onlineExpJob(player, jobName, amount, subCommand);
		}
		else if(!crossServer)
		{
			crossServerExpJob(player.getName(), jobName, amount, subCommand);
		}
	}

	// ######################################
	//	Level jobs
	// ######################################

	private void onlineLevelJob(NKPlayer player, String jobName, int amount, String subCommand)
	{
		switch(subCommand)
		{
			case "add": jobManager.addLevel(player, jobName, amount);
				break;
			case "remove": jobManager.removeLevel(player, jobName, amount);
				break;
			case "set": jobManager.setLevel(player, jobName, amount);
				break;
		}

		if(Bukkit.getPlayer(player.getUuid()) != null)
		{
			switch(subCommand)
			{
				case "add":
					Bukkit.getPlayer(player.getUuid()).sendMessage(
							ChatColor.GREEN + "Vous avez reçu " + amount + " niveau(x) pour votre métier " + jobName);
					break;
				case "remove":
					Bukkit.getPlayer(player.getUuid()).sendMessage(
							ChatColor.GREEN + "Vous avez perdu " + amount + " niveau(x) dans votre métier " + jobName);
					break;
				case "set":
					Bukkit.getPlayer(player.getUuid()).sendMessage(
							ChatColor.GREEN + "Votre niveau a été défini à " + amount + " pour votre métier " + jobName);
					break;
			}
		}
	}

	private void offlineLevelJob(NKPlayer player, String jobName, int amount, String subCommand)
	{
		Connection bdd = null;
		PreparedStatement ps = null;
		String req = null;

		try
		{
			PlayerJob job = player.getJobs().get(jobName);

			bdd = DatabaseManager.getConnection();

			req = "UPDATE " + DatabaseManager.table.PLAYER_JOBS
					+ " SET lvl = ? , xp = ? , xp_goal = ? , xp_total = ? , old = true WHERE player_id = ? AND job_id = ?";
			ps = bdd.prepareStatement(req);
			ps.setInt(1, job.lvl);
			ps.setDouble(2, job.xp);
			ps.setDouble(3, job.xpTotal);
			ps.setDouble(4, job.xpGoal);
			ps.setInt(5, player.getId());
			ps.setInt(6, jobManager.jobs.get(jobName).id);

			ps.executeUpdate();
			ps.close();

		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	private void crossServerLevelJob(final String playerName, String jobName, int amount, String subCommand)
	{
		String server = getOtherServer(playerName);
		if(server != null)
		{
			if(players.size() != 0)
			{
				ByteArrayDataOutput out = ByteStreams.newDataOutput();
				out.writeUTF("Forward"); // So BungeeCord knows to forward it
				out.writeUTF(server.toUpperCase());
				out.writeUTF("NKjobs");

				ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
				DataOutputStream msgout = new DataOutputStream(msgbytes);
				try
				{
					msgout.writeUTF("exp|" + playerName + "|" + jobName + "|" + amount + "|" + subCommand); // You can do
				}
				catch(IOException exception)
				{
					exception.printStackTrace();
				}

				out.writeShort(msgbytes.toByteArray().length);
				out.write(msgbytes.toByteArray());

				Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);

				player.sendPluginMessage(NKjobs.getPlugin(), "BungeeCord", out.toByteArray());
			}
			else
			{
				Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKjobs.PNAME + " " + playerName
						+ " est connecté(e) sur un autre serveur. Utilisez la console de ce serveur pour lui supprimer un métier.");
			}
		}
	}

	public void levelJob(NKPlayer player, String jobName, int amount, String subCommand, boolean crossServer)
	{
		if(!crossServer)
		{
			offlineLevelJob(player, jobName, amount, subCommand);
		}
		if(players.containsKey(player.getName()))
		{
			onlineLevelJob(player, jobName, amount, subCommand);
		}
		else if(!crossServer)
		{
			crossServerLevelJob(player.getName(), jobName, amount, subCommand);
		}
	}






	// Check if a player is connected in other server
	public String getOtherServer(String playername)
	{
		Connection bdd = null;
		PreparedStatement ps = null;
		ResultSet resultat = null;
		String req = null;

		try
		{
			bdd = DatabaseManager.getConnection();

			req = "SELECT server FROM " + DatabaseManager.common.PLAYERS + " WHERE name = ?";
			ps = bdd.prepareStatement(req);
			ps.setString(1, playername);

			resultat = ps.executeQuery();

			if(resultat.next())
			{
				String server = resultat.getString("server");

				resultat.close();
				ps.close();

				return server;
			}
			resultat.close();
			ps.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}

		return null;
	}
}
