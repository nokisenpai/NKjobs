package ovh.lumen.NKjobs.managers;

import ovh.lumen.NKjobs.data.NKData;
import ovh.lumen.NKjobs.enums.InternalMessages;
import ovh.lumen.NKjobs.exceptions.SetupException;
import ovh.lumen.NKjobs.utils.NKLogger;
import ovh.lumen.NKjobs.utils.SQLConnect;

import java.sql.*;

public final class DatabaseManager
{
	private static Connection bdd = null;

	private DatabaseManager() {}

	public enum Tables
	{
		PLAYER_JOBS("player_jobs"),
		JOBS("jobs"),
		BLOCKS("blocks"),
		FURNACES("furnaces"),
		BREWINGSTANDS("brewingstands"),
		CHUNKS("chunks"),
		ITEMS("items");

		private final String name;

		Tables(String name)
		{
			this.name = name;
		}

		public String toString()
		{
			return NKData.PREFIX + name;
		}
	}

	public static void load() throws SetupException
	{
		SQLConnect.setInfo(NKData.DBACCESS);

		try
		{
			connect();
		}
		catch(SQLException e)
		{
			throw new SetupException(InternalMessages.DATABASE_CANT_CONNECT.toString());
		}

		try
		{
			if(!checkTables())
			{
				createTable();
			}
		}
		catch(SQLException e)
		{
			throw new SetupException(InternalMessages.DATABASE_CANT_CREATE_TABLES.toString());
		}
	}

	public void unload()
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

	private static void connect() throws SQLException
	{
		bdd = SQLConnect.getHikariDS().getConnection();
	}

	private static void createTable() throws SQLException
	{
		try(Statement s = bdd.createStatement())
		{
			String req = "CREATE TABLE IF NOT EXISTS `" + Tables.JOBS + "` ("
					+ " `id` int NOT NULL,"
					+ " `name` varchar(100) NOT NULL,"
					+ " `old` boolean NOT NULL,"
					+ " PRIMARY KEY (`id`),"
					+ " UNIQUE INDEX `jobs_id_name_UNIQUE` (`id`, `name`)) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;";
			s.execute(req);

			req = "CREATE TABLE IF NOT EXISTS `" + Tables.PLAYER_JOBS + "` ("
					+ " `player_uuid` varchar(40) NOT NULL,"
					+ " `job_id` int NOT NULL,"
					+ " `lvl` int NOT NULL,"
					+ " `xp` double NOT NULL,"
					+ " `xp_goal` double NOT NULL,"
					+ " `xp_total` double NOT NULL,"
					+ " `old` boolean NOT NULL,"
					+ " `time` date NOT NULL DEFAULT '2020-02-23',"
					+ " UNIQUE INDEX `player_uuid_job_id_UNIQUE` (`player_uuid`,`job_id`)) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;";
			s.execute(req);

			req = "CREATE TABLE IF NOT EXISTS `" + Tables.BLOCKS + "` ("
					+ " `id` int UNSIGNED NOT NULL AUTO_INCREMENT ,"
					+ " `server_id` int NOT NULL,"
					+ " `world_id` int NOT NULL,"
					+ " `x` double NOT NULL ,"
					+ " `y` double NOT NULL ,"
					+ " `z` double NOT NULL ,"
					+ " `time` datetime NOT NULL ,"
					+ " PRIMARY KEY (`id`),"
					+ " UNIQUE INDEX `blocks_server_world_x_y_z` (`server_id`, `world_id`, `x`, `y`, `z`)) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;";
			s.execute(req);

			req = "CREATE TABLE IF NOT EXISTS `" + Tables.FURNACES + "` ("
					+ " `id` int UNSIGNED NOT NULL AUTO_INCREMENT ,"
					+ " `player_uuid` varchar(40) NOT NULL,"
					+ " `server_id` int NOT NULL,"
					+ " `world_id` int NOT NULL,"
					+ " `x` double NOT NULL ,"
					+ " `y` double NOT NULL ,"
					+ " `z` double NOT NULL ,"
					+ " PRIMARY KEY (`id`),"
					+ " UNIQUE INDEX `furnaces_server_world_x_y_z` (`server_id`, `world_id`, `x`, `y`, `z`)) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;";
			s.execute(req);

			req = "CREATE TABLE IF NOT EXISTS `" + Tables.BREWINGSTANDS + "` ("
					+ " `id` int UNSIGNED NOT NULL AUTO_INCREMENT ,"
					+ " `player_uuid` varchar(40) NOT NULL,"
					+ " `server_id` int NOT NULL,"
					+ " `world_id` int NOT NULL,"
					+ " `x` double NOT NULL ,"
					+ " `y` double NOT NULL ,"
					+ " `z` double NOT NULL ,"
					+ " PRIMARY KEY (`id`),"
					+ " UNIQUE INDEX `brewingstands_server_world_x_y_z` (`server_id`, `world_id`, `x`, `y`, `z`)) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;";
			s.execute(req);

			req = "CREATE TABLE IF NOT EXISTS `" + Tables.CHUNKS + "` ("
					+ " `id` int UNSIGNED NOT NULL AUTO_INCREMENT ,"
					+ " `server_id` int NOT NULL,"
					+ " `world_id` int NOT NULL,"
					+ " `x` double NOT NULL ,"
					+ " `z` double NOT NULL ,"
					+ " `players` text NOT NULL,"
					+ " PRIMARY KEY (`id`),"
					+ " UNIQUE INDEX `chunks_server_world_x_z` (`server_id`, `world_id`, `x`, `z`)) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;";
			s.execute(req);

			req = "CREATE TABLE IF NOT EXISTS `" + Tables.ITEMS + "` ("
					+ " `id` int UNSIGNED NOT NULL AUTO_INCREMENT ,"
					+ " `player_uuid` varchar(40) NOT NULL,"
					+ " `item` text NOT NULL,"
					+ " PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;";
			s.execute(req);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}

		NKLogger.log(InternalMessages.DATABASE_CREATE_TABLES_SUCCESS.toString());
	}

	private static boolean checkTables()
	{
		String req = "SHOW TABLES FROM " + NKData.DBACCESS.getDbName() + " LIKE '" + NKData.PREFIX + "%'";

		try(PreparedStatement ps = bdd.prepareStatement(req); ResultSet result = ps.executeQuery())
		{
			int count = 0;

			while(result.next())
			{
				count++;
			}

			result.close();
			ps.close();

			if(count < Tables.values().length)
			{
				return false;
			}
		}
		catch(SQLException e)
		{
			NKLogger.error(InternalMessages.DATABASE_CANT_CHECK_TABLES.toString());

			return false;
		}

		return true;
	}

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

				connect();
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}

		return bdd;
	}
}
