package be.noki_senpai.NKjobs.managers;

import be.noki_senpai.NKjobs.NKjobs;
import be.noki_senpai.NKjobs.utils.SQLConnect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager
{
	private static Connection bdd = null;

	private ConsoleCommandSender console = null;
	private ConfigManager configManager = null;

	public DatabaseManager(ConfigManager configManager)
	{
		this.console = Bukkit.getConsoleSender();
		this.configManager = configManager;
	}

	public enum common
	{
		PLAYERS("NK_players"),
		SERVERS("NK_servers"),
		WORLDS("NK_worlds");

		private String name = "";

		common(String name)
		{
			this.name = name;
		}

		public String toString()
		{
			return name;
		}

		public static int size()
		{
			return common.values().length;
		}
	}

	public enum table
	{
		PLAYER_JOBS(ConfigManager.PREFIX + "player_jobs"),
		JOBS(ConfigManager.PREFIX + "jobs"),
		BLOCKS(ConfigManager.PREFIX + "blocks"),
		FURNACES(ConfigManager.PREFIX + "furnaces"),
		BREWINGSTANDS(ConfigManager.PREFIX + "brewingstands"),
		CHUNKS(ConfigManager.PREFIX + "chunks"),
		ITEMS(ConfigManager.PREFIX + "items")
		;

		private String name = "";

		table(String name)
		{
			this.name = name;
		}

		public String toString()
		{
			return name;
		}

		public static int size()
		{
			return table.values().length;
		}
	}

	public boolean loadDatabase()
	{

		// Setting database informations
		SQLConnect.setInfo(configManager.getDbHost(), configManager.getDbPort(), configManager.getDbName(), configManager.getDbUser(), configManager.getDbPassword());

		// Try to connect to database
		try
		{
			bdd = SQLConnect.getHikariDS().getConnection();
		}
		catch(SQLException e)
		{
			bdd = null;
			console.sendMessage(
					ChatColor.DARK_RED + NKjobs.PNAME + " Error while attempting database connexion. Verify your access informations in config.yml");
			e.printStackTrace();
			return false;
		}

		try
		{
			// Check if tables already exist on database
			if(!existTables())
			{
				// Create database structure if not exist
				createTable();
			}

		}
		catch(SQLException e)
		{
			console.sendMessage(ChatColor.DARK_RED + NKjobs.PNAME + " Error while creating database structure. (Error#A.2.002)");
			return false;
		}

		return true;
	}

	public void unloadDatabase()
	{
		if(bdd != null)
		{
			try
			{
				bdd.close();
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
		}
	}

	private boolean existTables() throws SQLException
	{
		// Select all tables beginning with the prefix
		String req = "SHOW TABLES FROM " + configManager.getDbName() + " LIKE '" + ConfigManager.PREFIX + "%'";
		ResultSet resultat = null;
		PreparedStatement ps = null;

		try
		{
			ps = bdd.prepareStatement(req);
			resultat = ps.executeQuery();
			int count = 0;
			while(resultat.next())
			{
				count++;
			}

			// if all tables are missing
			if(count == 0)
			{
				console.sendMessage(ChatColor.GREEN + NKjobs.PNAME + " Missing table(s). First start.");
				return false;
			}
			resultat.close();
			ps.close();

			// if 1 or more tables are missing
			if(count < table.size())
			{
				console.sendMessage(ChatColor.DARK_RED + NKjobs.PNAME
						+ " Missing table(s). Please don't alter tables name or structure in database. (Error#main.Storage.002)");
				return false;
			}
		}
		catch(SQLException e1)
		{
			console.sendMessage(ChatColor.DARK_RED + NKjobs.PNAME + " Error while testing existance of tables. (Error#main.Storage.003)");
		}

		return true;
	}

	private void createTable() throws SQLException
	{
		try
		{
			bdd = getConnection();

			String req = null;
			Statement s = null;

			console.sendMessage(ChatColor.GREEN + NKjobs.PNAME + " Creating Database structure ...");

			try
			{
				//Creating jobs table
				req = "CREATE TABLE IF NOT EXISTS `" + table.JOBS + "` (" + "  `id` INT NOT NULL," + " `name` VARCHAR(100) NOT NULL,"
						+ "  `old` BOOLEAN NOT NULL," + "  PRIMARY KEY (`id`)," + "  UNIQUE INDEX `jobs_id_name_UNIQUE` (`id`, `name`))"
						+ "ENGINE = InnoDB;";
				s = bdd.createStatement();
				s.execute(req);
				s.close();

				//Creating player_jobs table
				req = "CREATE TABLE IF NOT EXISTS `" + table.PLAYER_JOBS + "` ("
						+ " `player_id` INT NOT NULL,"
						+ " `job_id` INT NOT NULL,"
						+ " `lvl` double NOT NULL,"
						+ " `xp` double NOT NULL,"
						+ " `xp_goal` double NOT NULL,"
						+ " `xp_total` double NOT NULL,"
						+ " `old` BOOLEAN NOT NULL,"
						+ " `xp_day` double NOT NULL,"
						+ " `time` DATE NOT NULL DEFAULT '2020-02-23',"
						+ " UNIQUE INDEX `player_id_job_id_UNIQUE` (`player_id`,`job_id`)) ENGINE = InnoDB;";
				s = bdd.createStatement();
				s.execute(req);
				s.close();

				//Creating blocks table
				req = "CREATE TABLE IF NOT EXISTS `" + table.BLOCKS + "` ("
						+ " `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,"
						+ " `server_id` INT NOT NULL,"
						+ " `world_id` INT NOT NULL,"
						+ " `x` DOUBLE NOT NULL ,"
						+ " `y` DOUBLE NOT NULL ,"
						+ " `z` DOUBLE NOT NULL ,"
						+ " `time` DATETIME NOT NULL ,"
						+ " PRIMARY KEY (`id`),"
						+ " UNIQUE INDEX `blocks_server_world_x_y_z` (`server_id`, `world_id`, `x`, `y`, `z`)) ENGINE = InnoDB;";
				s = bdd.createStatement();
				s.execute(req);
				s.close();

				//Creating furnaces table
				req = "CREATE TABLE IF NOT EXISTS `" + table.FURNACES + "` ("
						+ " `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,"
						+ " `player_id` INT NOT NULL,"
						+ " `server_id` INT NOT NULL,"
						+ " `world_id` INT NOT NULL,"
						+ " `x` DOUBLE NOT NULL ,"
						+ " `y` DOUBLE NOT NULL ,"
						+ " `z` DOUBLE NOT NULL ,"
						+ " PRIMARY KEY (`id`),"
						+ " UNIQUE INDEX `furnaces_server_world_x_y_z` (`server_id`, `world_id`, `x`, `y`, `z`)) ENGINE = InnoDB;";
				s = bdd.createStatement();
				s.execute(req);
				s.close();

				//Creating brewingstands table
				req = "CREATE TABLE IF NOT EXISTS `" + table.BREWINGSTANDS + "` ("
						+ " `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,"
						+ " `player_id` INT NOT NULL,"
						+ " `server_id` INT NOT NULL,"
						+ " `world_id` INT NOT NULL,"
						+ " `x` DOUBLE NOT NULL ,"
						+ " `y` DOUBLE NOT NULL ,"
						+ " `z` DOUBLE NOT NULL ,"
						+ " PRIMARY KEY (`id`),"
						+ " UNIQUE INDEX `brewingstands_server_world_x_y_z` (`server_id`, `world_id`, `x`, `y`, `z`)) ENGINE = InnoDB;";
				s = bdd.createStatement();
				s.execute(req);
				s.close();

				//Creating chunks table
				req = "CREATE TABLE IF NOT EXISTS `" + table.CHUNKS + "` ("
						+ " `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,"
						+ " `server_id` INT NOT NULL,"
						+ " `world_id` INT NOT NULL,"
						+ " `x` DOUBLE NOT NULL ,"
						+ " `z` DOUBLE NOT NULL ,"
						+ " `players` TEXT NOT NULL,"
						+ " PRIMARY KEY (`id`),"
						+ " UNIQUE INDEX `chunks_server_world_x_z` (`server_id`, `world_id`, `x`, `z`)) ENGINE = InnoDB;";
				s = bdd.createStatement();
				s.execute(req);
				s.close();

				//Creating player_jobs table
				req = "CREATE TABLE IF NOT EXISTS `" + table.ITEMS + "` ("
						+ " `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,"
						+ " `player_id` INT NOT NULL,"
						+ " `item` TEXT NOT NULL,"
						+ " PRIMARY KEY (`id`)) ENGINE = InnoDB;";
				s = bdd.createStatement();
				s.execute(req);
				s.close();


				console.sendMessage(ChatColor.GREEN + NKjobs.PNAME + " Database structure created.");
			}
			catch(SQLException e)
			{
				console.sendMessage(ChatColor.DARK_RED + NKjobs.PNAME + " Error while creating database structure. (Error#main.Storage.000)");
				e.printStackTrace();
			}
			finally
			{
				if(s != null)
				{
					s.close();
				}
			}
		}
		catch(SQLException e)
		{
			console.sendMessage(ChatColor.DARK_RED + NKjobs.PNAME + " Error while creating database structure. (Error#main.Storage.001)");
		}
	}

	// Getter 'bdd'
	public static Connection getConnection()
	{
		try
		{
			if(!bdd.isValid(1))
			{
				if(!bdd.isClosed())
				{
					bdd.close();
				}
				bdd = SQLConnect.getHikariDS().getConnection();
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return bdd;
	}
}
