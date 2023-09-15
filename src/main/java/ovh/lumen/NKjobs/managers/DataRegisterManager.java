package ovh.lumen.NKjobs.managers;

import ovh.lumen.NKjobs.Main;
import ovh.lumen.NKjobs.data.*;
import org.bukkit.*;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.function.Function;

public class DataRegisterManager
{
	private Map<String, Map<String, BlockTimer>> blocks = new HashMap<>();
	private Map<String, Map<String, RegisteredFurnace>> furnaces = new HashMap<>();
	private Map<String, Map<String, RegisteredBrewingStand>> brewingStands = new HashMap<>();
	private Map<String, Map<String, ExploredChunk>> chunks = new HashMap<>();

	private Map<String, Map<String, ExploredChunk>> newChunks = new HashMap<>();

	public static int SERVERID = -1;
	private Map<String, Integer> worlds = new HashMap<>();

	PlayerManager playerManager = null;
	private QueueManager queueManager = null;
	private ConsoleCommandSender console = null;

	public DataRegisterManager(PlayerManager playerManager, QueueManager queueManager)
	{
		this.playerManager = playerManager;
		this.queueManager = queueManager;
		console = Bukkit.getConsoleSender();

		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				purgeAll();
				saveAll();
			}
		}.runTaskTimerAsynchronously(Main.getPlugin(), 0, 300 * 20);
	}

	// Load data from database
	public boolean loadData()
	{
		if(!makeServerId())
		{
			return false;
		}

		if(!makeWorldsId())
		{
			return false;
		}

		purgeOfflineBlocksTimer(true);
		if(!loadBlocksTimer())
		{
			return false;
		}

		if(!loadFurnaces())
		{
			return false;
		}

		if(!loadBrewingStands())
		{
			return false;
		}

		return true;
	}
	// **************************************
	// **************************************
	// Purge all old data
	// **************************************
	// **************************************

	// ######################################
	// Purge all old data
	// ######################################

	public void purgeAll()
	{
		purgeOnlineBlocksTimer();
		purgeOfflineBlocksTimer(false);
	}

	// ######################################
	// Purge old blocks timer data on database
	// ######################################

	public void purgeOfflineBlocksTimer(Boolean notify)
	{
		Connection bdd = null;
		ResultSet resultat = null;
		PreparedStatement ps = null;
		String req = null;
		int count = 0;

		try
		{
			bdd = DatabaseManager.getConnection();

			Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());

			req = "SELECT count(id) as count FROM " + DatabaseManager.Tables.BLOCKS + " WHERE time <= ? AND server_id = ?";
			ps = bdd.prepareStatement(req);
			ps.setTimestamp(1, now);
			ps.setInt(2, SERVERID);
			resultat = ps.executeQuery();

			if(resultat.next())
			{
				count = resultat.getInt("count");
			}

			ps.close();
			resultat.close();

			req = "DELETE FROM " + DatabaseManager.Tables.BLOCKS + " WHERE time <= ? AND server_id = ?";
			ps = bdd.prepareStatement(req);
			ps.setTimestamp(1, now);
			ps.setInt(2, SERVERID);
			ps.execute();

			if(notify)
			{
				console.sendMessage(ChatColor.BLUE + Main.PNAME + " " + count + " old blocks timer deleted.");
			}

			ps.close();
			resultat.close();
		}
		catch(SQLException e)
		{
			Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + Main.PNAME + " Error while purging blocks timer data.");
			e.printStackTrace();
		}
	}

	// ######################################
	// Purge old blocks timer data on plugin
	// ######################################

	public void purgeOnlineBlocksTimer()
	{
		Date now = new java.sql.Date(System.currentTimeMillis());

		for(Map.Entry<String, Map<String, BlockTimer>> worlds : blocks.entrySet())
		{
			Set<Map.Entry<String, BlockTimer>> setOfEntries = worlds.getValue().entrySet();

			Iterator<Map.Entry<String, BlockTimer>> iterator = setOfEntries.iterator();

			while(iterator.hasNext())
			{
				Map.Entry<String, BlockTimer> entry = iterator.next();
				BlockTimer value = entry.getValue();

				if(value.getTime().before(now))
				{
					iterator.remove();
				}
			}
		}
	}

	// **************************************
	// **************************************
	// Load all data
	// **************************************
	// **************************************

	// ######################################
	// Get server id
	// ######################################

	public boolean makeServerId()
	{
		Connection bdd = null;
		ResultSet resultat = null;
		PreparedStatement ps = null;
		String req = null;

		try
		{
			bdd = DatabaseManager.getConnection();

			req = "SELECT id FROM " + DatabaseManager.common.SERVERS + " WHERE name = ?";
			ps = bdd.prepareStatement(req);
			ps.setString(1, ConfigManager.SERVERNAME);

			resultat = ps.executeQuery();

			if(resultat.next())
			{
				SERVERID = resultat.getInt(1);
			}
			else
			{
				ps.close();
				resultat.close();
				return false;
			}

			ps.close();
			resultat.close();
		}
		catch(SQLException e)
		{
			Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + Main.PNAME + " Error while getting server id.");
			e.printStackTrace();
		}
		return true;
	}

	// ######################################
	// Get worlds id
	// ######################################

	public boolean makeWorldsId()
	{
		Connection bdd = null;
		ResultSet resultat = null;
		PreparedStatement ps = null;
		String req = null;

		try
		{
			bdd = DatabaseManager.getConnection();

			for(World world : Bukkit.getWorlds())
			{
				blocks.put(world.getName(), new HashMap<>());
				furnaces.put(world.getName(), new HashMap<>());
				brewingStands.put(world.getName(), new HashMap<>());
				chunks.put(world.getName(), new HashMap<>());

				newChunks.put(world.getName(), new HashMap<>());

				req = "SELECT id FROM " + DatabaseManager.common.WORLDS + " WHERE server_id = ? AND name = ?";
				ps = bdd.prepareStatement(req);
				ps.setInt(1, SERVERID);
				ps.setString(2, world.getName());

				resultat = ps.executeQuery();

				if(resultat.next())
				{
					worlds.put(world.getName(), resultat.getInt(1));
				}
				else
				{
					ps.close();
					resultat.close();
					Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + Main.PNAME + " Error while getting worlds id.");
					return false;
				}

				ps.close();
				resultat.close();
			}
		}
		catch(SQLException e1)
		{
			Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + Main.PNAME + " Error while getting blocks timer data.");
			e1.printStackTrace();
		}
		return true;
	}

	// ######################################
	// Load blocks timer data
	// ######################################

	public boolean loadBlocksTimer()
	{
		Connection bdd = null;
		ResultSet resultat = null;
		PreparedStatement ps = null;
		String req = null;
		boolean hasResult = false;

		try
		{
			bdd = DatabaseManager.getConnection();

			req = "SELECT x, y, z, time, w.name AS world FROM " + DatabaseManager.Tables.BLOCKS + " b " + "LEFT JOIN " + DatabaseManager.common.WORLDS
					+ " w " + "ON b.world_id = w.id " + "WHERE time > ? AND b.server_id = ?";
			ps = bdd.prepareStatement(req);
			ps.setDate(1, new java.sql.Date(System.currentTimeMillis()));
			ps.setInt(2, SERVERID);
			resultat = ps.executeQuery();

			while(resultat.next())
			{
				String key = "" + resultat.getDouble("x") + "|" + resultat.getDouble("y") + "|" + resultat.getDouble("z");
				if(blocks.get(resultat.getString("world")) != null)
				{
					blocks.get(resultat.getString("world")).put(key, new BlockTimer(resultat.getDouble("x"), resultat.getDouble("y"), resultat.getDouble("z"), resultat.getTimestamp("time")));
				}
				hasResult = true;
			}

			if(hasResult)
			{
				resultat.last();
			}

			console.sendMessage(ChatColor.GREEN + Main.PNAME + " Loaded " + resultat.getRow() + " blocks timer.");

			ps.close();
			resultat.close();
		}
		catch(SQLException e1)
		{
			Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + Main.PNAME + " Error while getting blocks timer data.");
			e1.printStackTrace();
		}
		return true;
	}

	// ######################################
	// Load furnaces data
	// ######################################

	public boolean loadFurnaces()
	{
		Connection bdd = null;
		ResultSet resultat = null;
		PreparedStatement ps = null;
		String req = null;
		boolean hasResult = false;

		try
		{
			bdd = DatabaseManager.getConnection();

			req = "SELECT f.id AS id, x, y, z, f.player_id AS player_id, p.name AS name, w.name AS world FROM " + DatabaseManager.Tables.FURNACES
					+ " f " + "LEFT JOIN " + DatabaseManager.common.WORLDS + " w " + "ON f.world_id = w.id " + "LEFT JOIN "
					+ DatabaseManager.common.PLAYERS + " p " + "ON f.player_id = p.id " + "WHERE f.server_id = ?";
			ps = bdd.prepareStatement(req);
			ps.setInt(1, SERVERID);
			resultat = ps.executeQuery();

			while(resultat.next())
			{
				String key = "" + resultat.getDouble("x") + "|" + resultat.getDouble("y") + "|" + resultat.getDouble("z");
				if(furnaces.get(resultat.getString("world")) != null)
				{
					furnaces.get(resultat.getString("world")).put(key, new RegisteredFurnace(resultat.getInt("id"), resultat.getDouble("x"), resultat.getDouble("y"), resultat.getDouble("z"), resultat.getString("name"), resultat.getInt("player_id")));
				}
				hasResult = true;
			}

			if(hasResult)
			{
				resultat.last();
			}

			console.sendMessage(ChatColor.GREEN + Main.PNAME + " Loaded " + resultat.getRow() + " registered furnaces.");

			ps.close();
			resultat.close();
		}
		catch(SQLException e1)
		{
			Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + Main.PNAME + " Error while getting registered furnaces data.");
			e1.printStackTrace();
		}
		return true;
	}

	// ######################################
	// Load brewingStands data
	// ######################################

	public boolean loadBrewingStands()
	{
		Connection bdd = null;
		ResultSet resultat = null;
		PreparedStatement ps = null;
		String req = null;
		boolean hasResult = false;

		try
		{
			bdd = DatabaseManager.getConnection();

			req = "SELECT bs.id AS id, x, y, z, bs.player_id AS player_id, p.name AS name, w.name AS world FROM "
					+ DatabaseManager.Tables.BREWINGSTANDS + " bs " + "LEFT JOIN " + DatabaseManager.common.WORLDS + " w " + "ON bs.world_id = w.id "
					+ "LEFT JOIN " + DatabaseManager.common.PLAYERS + " p " + "ON bs.player_id = p.id " + "WHERE bs.server_id = ?";
			ps = bdd.prepareStatement(req);
			ps.setInt(1, SERVERID);
			resultat = ps.executeQuery();

			while(resultat.next())
			{
				String key = "" + resultat.getDouble("x") + "|" + resultat.getDouble("y") + "|" + resultat.getDouble("z");
				if(brewingStands.get(resultat.getString("world")) != null)
				{
					brewingStands.get(resultat.getString("world")).put(key, new RegisteredBrewingStand(resultat.getInt("id"), resultat.getDouble("x"), resultat.getDouble("y"), resultat.getDouble("z"), resultat.getString("name"), resultat.getInt("player_id")));
				}
				hasResult = true;
			}

			if(hasResult)
			{
				resultat.last();
			}

			console.sendMessage(ChatColor.GREEN + Main.PNAME + " Loaded " + resultat.getRow() + " registered brewing stands.");

			ps.close();
			resultat.close();
		}
		catch(SQLException e1)
		{
			Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + Main.PNAME + " Error while getting registered brewing stands data.");
			e1.printStackTrace();
		}
		return true;
	}

	// ######################################
	// Load chunks data
	// ######################################

	public boolean loadChunks()
	{
		Connection bdd = null;
		ResultSet resultat = null;
		PreparedStatement ps = null;
		String req = null;

		try
		{
			bdd = DatabaseManager.getConnection();

			req = "SELECT x, z, c.players AS players, w.name AS world FROM " + DatabaseManager.Tables.CHUNKS + " c " + "LEFT JOIN "
					+ DatabaseManager.common.WORLDS + " w " + "ON c.world_id = w.id " + "WHERE c.server_id = ?";
			ps = bdd.prepareStatement(req);
			ps.setInt(1, SERVERID);
			resultat = ps.executeQuery();

			while(resultat.next())
			{
				String key = "" + resultat.getDouble("x") + "|" + resultat.getDouble("z");
				if(chunks.get(resultat.getString("world")) != null)
				{
					chunks.get(resultat.getString("world")).put(key, new ExploredChunk(resultat.getDouble("x"), resultat.getDouble("z"), Arrays.asList(resultat.getString("players").split(","))));
				}
			}

			resultat.last();
			console.sendMessage(ChatColor.GREEN + Main.PNAME + " Loaded " + resultat.getRow() + " explored chunks.");

			ps.close();
			resultat.close();
		}
		catch(SQLException e1)
		{
			Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + Main.PNAME + " Error while getting explored chunks data.");
			e1.printStackTrace();
		}
		return true;
	}

	// **************************************
	// **************************************
	// Save all data
	// **************************************
	// **************************************

	// ######################################
	// Save all data
	// ######################################

	public void saveAll()
	{
		saveBlocksTimer();
		saveChunks();
	}

	// ######################################
	// Save blocks timer data
	// ######################################

	public void saveBlocksTimer()
	{
		if(blocks.size() > 0)
		{
			boolean ok = false;
			Connection bdd = null;
			PreparedStatement ps = null;
			String req = "INSERT INTO " + DatabaseManager.Tables.BLOCKS + " ( server_id, world_id, x, y, z, time) VALUES ";

			for(Map.Entry<String, Map<String, BlockTimer>> world : blocks.entrySet())
			{
				if(world.getValue().size() > 0)
				{
					Map<String, BlockTimer> saveBlocks = new HashMap<>(world.getValue());
					ok = true;
					for(Map.Entry<String, BlockTimer> block : saveBlocks.entrySet())
					{
						req += "(" + SERVERID + " , " + worlds.get(world.getKey()) + " , " + block.getValue().getX() + " , " + block.getValue().getY()
								+ " , " + block.getValue().getZ() + " , '" + block.getValue().getTime().toString() + "'),";
					}
				}
			}

			req = req.substring(0, req.length() - 1);
			req += " ON DUPLICATE KEY UPDATE time = VALUES(time)";
			try
			{
				bdd = DatabaseManager.getConnection();
				ps = bdd.prepareStatement(req);
				if(ok)
				{
					ps.executeUpdate();
				}
				ps.close();
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
		}
	}

	// ######################################
	// Save explored chunks data
	// ######################################

	public void saveChunks()
	{
		if(newChunks.size() > 0)
		{
			boolean ok = false;
			Connection bdd = null;
			PreparedStatement ps = null;

			String req = "INSERT INTO " + DatabaseManager.Tables.CHUNKS + " ( server_id, world_id, x, z, players) VALUES ";

			for(Map.Entry<String, Map<String, ExploredChunk>> world : newChunks.entrySet())
			{
				if(world.getValue().size() > 0)
				{
					ok = true;
					for(Map.Entry<String, ExploredChunk> chunk : world.getValue().entrySet())
					{
						req += "(" + SERVERID + " , " + worlds.get(world.getKey()) + " , " + chunk.getValue().getX() + " , " + chunk.getValue().getZ()
								+ " , '" + chunk.getValue().getPlayersToString() + "'),";
					}
					newChunks.get(world.getKey()).clear();
				}
			}

			req = req.substring(0, req.length() - 1);
			req += " ON DUPLICATE KEY UPDATE players = VALUES(players)";
			try
			{
				bdd = DatabaseManager.getConnection();
				ps = bdd.prepareStatement(req);
				if(ok)
				{
					ps.executeUpdate();
				}
				ps.close();
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
		}
	}

	// **************************************
	// **************************************
	// Register data
	// **************************************
	// **************************************

	// ######################################
	// registerBreakBlockTimer
	// ######################################

	public void registerBreakBlockTimer(Location location)
	{
		String key = "" + location.getX() + "|" + location.getY() + "|" + location.getZ();
		blocks.get(location.getWorld().getName()).put(key, new BlockTimer(location.getX(), location.getY(), location.getZ(), new java.sql.Timestamp(
				System.currentTimeMillis() + (ConfigManager.GLOBALBLOCKSTIMER * 1000))));
	}

	// ######################################
	// registerPlaceBlockTimer
	// ######################################

	public void registerPlaceBlockTimer(Location location, long blockTimer)
	{
		String key = "" + location.getX() + "|" + location.getY() + "|" + location.getZ();
		blocks.get(location.getWorld().getName()).put(key, new BlockTimer(location.getX(), location.getY(), location.getZ(), new java.sql.Timestamp(
				System.currentTimeMillis() + (blockTimer * 1000))));
	}

	// ######################################
	// registerFurnace
	// ######################################

	public void registerFurnace(Location location, NKWorker player)
	{
		queueManager.addToQueue(new Function()
		{
			@Override
			public Object apply(Object o)
			{
				String key = "" + location.getX() + "|" + location.getY() + "|" + location.getZ();

				player.addFurnace();

				int id = -1;

				Connection bdd = null;
				PreparedStatement ps = null;
				ResultSet resultat = null;

				String req = "INSERT INTO " + DatabaseManager.Tables.FURNACES + " ( player_id, server_id, world_id, x, y, z) "
						+ "VALUES ( ? , ? , ? , ? , ? , ? ) " + "ON DUPLICATE KEY UPDATE player_id = VALUES(player_id), id = LAST_INSERT_ID(id)";
				try
				{
					bdd = DatabaseManager.getConnection();
					ps = bdd.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);

					ps.setInt(1, player.getId());
					ps.setInt(2, SERVERID);
					ps.setInt(3, worlds.get(location.getWorld().getName()));
					ps.setDouble(4, location.getX());
					ps.setDouble(5, location.getY());
					ps.setDouble(6, location.getZ());

					ps.executeUpdate();
					resultat = ps.getGeneratedKeys();

					resultat.next();
					id = resultat.getInt(1);

					ps.close();
					resultat.close();

					furnaces.get(location.getWorld().getName()).put(key, new RegisteredFurnace(id, location.getX(), location.getY(), location.getZ(), player.getName(), player.getId()));
				}
				catch(SQLException e)
				{
					e.printStackTrace();
				}
				Bukkit.getPlayer(player.getUuid()).sendMessage(ChatColor.GREEN + "Four " + player.getNbFurnace() + " enregistré.");

				return null;
			}
		});
	}

	// ######################################
	// registerBrewingStand
	// ######################################

	public void registerBrewingStand(Location location, NKWorker player)
	{
		queueManager.addToQueue(new Function()
		{
			@Override
			public Object apply(Object o)
			{
				String key = "" + location.getX() + "|" + location.getY() + "|" + location.getZ();

				player.addBrewingStand();

				int id = -1;

				Connection bdd = null;
				PreparedStatement ps = null;
				ResultSet resultat = null;

				String req = "INSERT INTO " + DatabaseManager.Tables.BREWINGSTANDS + " ( player_id, server_id, world_id, x, y, z) "
						+ "VALUES ( ? , ? , ? , ? , ? , ? ) " + "ON DUPLICATE KEY UPDATE player_id = VALUES(player_id), id = LAST_INSERT_ID(id)";
				try
				{
					bdd = DatabaseManager.getConnection();
					ps = bdd.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);

					ps.setInt(1, player.getId());
					ps.setInt(2, SERVERID);
					ps.setInt(3, worlds.get(location.getWorld().getName()));
					ps.setDouble(4, location.getX());
					ps.setDouble(5, location.getY());
					ps.setDouble(6, location.getZ());

					ps.executeUpdate();
					resultat = ps.getGeneratedKeys();

					resultat.next();
					id = resultat.getInt(1);

					ps.close();
					resultat.close();

					brewingStands.get(location.getWorld().getName()).put(key, new RegisteredBrewingStand(id, location.getX(), location.getY(), location.getZ(), player.getName(), player.getId()));
				}
				catch(SQLException e)
				{
					e.printStackTrace();
				}
				Bukkit.getPlayer(player.getUuid()).sendMessage(ChatColor.GREEN + "Alambic " + player.getNbBrewingStand() + " enregistré.");

				return null;
			}
		});
	}

	// ######################################
	// registerChunk
	// ######################################

	public void registerChunk(Chunk chunk, String playerName)
	{
		String key = "" + chunk.getX() + "|" + chunk.getZ();
		if(chunks.get(chunk.getWorld().getName()).containsKey(key))
		{
			chunks.get(chunk.getWorld().getName()).get(key).addPlayer(playerName);
			newChunks.get(chunk.getWorld().getName()).put(key, chunks.get(chunk.getWorld().getName()).get(key));
		}
		else
		{
			List<String> playerNames = new ArrayList<String>();
			playerNames.add(playerName);
			chunks.get(chunk.getWorld().getName()).put(key, new ExploredChunk(chunk.getX(), chunk.getZ(), playerNames));
			newChunks.get(chunk.getWorld().getName()).put(key, chunks.get(chunk.getWorld().getName()).get(key));
		}

	}

	// ######################################
	// unregisterFurnace
	// ######################################

	public void unregisterFurnace(Location location, Player player)
	{
		String key = "" + location.getX() + "|" + location.getY() + "|" + location.getZ();
		String worldName = location.getWorld().getName();

		if(furnaces.get(worldName).containsKey(key))
		{
			RegisteredFurnace furnace = furnaces.get(worldName).get(key);

			queueManager.addToQueue(new Function()
			{
				@Override
				public Object apply(Object o)
				{
					Connection bdd = null;
					PreparedStatement ps = null;

					String req = "DELETE FROM " + DatabaseManager.Tables.FURNACES + " WHERE id = ?";

					try
					{
						bdd = DatabaseManager.getConnection();
						ps = bdd.prepareStatement(req);
						ps.setInt(1, furnace.getId());

						ps.execute();
						ps.close();
					}
					catch(SQLException e)
					{
						e.printStackTrace();
					}

					NKWorker nkWorker = playerManager.getPlayer(furnace.getPlayerName());
					if(nkWorker != null)
					{
						nkWorker.removeFurnace();
					}

					player.sendMessage(ChatColor.GREEN + "Vous avez détruit le four de " + furnace.getPlayerName() + ".");
					furnaces.get(worldName).remove(key);
					return null;
				}
			});
		}
	}

	// ######################################
	// unregisterBrewingStand
	// ######################################

	public void unregisterBrewingStand(Location location, Player player)
	{
		String key = "" + location.getX() + "|" + location.getY() + "|" + location.getZ();
		String worldName = location.getWorld().getName();

		if(brewingStands.get(worldName).containsKey(key))
		{
			RegisteredBrewingStand brewingStand = brewingStands.get(worldName).get(key);

			queueManager.addToQueue(new Function()
			{
				@Override
				public Object apply(Object o)
				{
					Connection bdd = null;
					PreparedStatement ps = null;

					String req = "DELETE FROM " + DatabaseManager.Tables.BREWINGSTANDS + " WHERE id = ?";

					try
					{
						bdd = DatabaseManager.getConnection();
						ps = bdd.prepareStatement(req);
						ps.setInt(1, brewingStand.getId());

						ps.executeUpdate();
						ps.close();
					}
					catch(SQLException e)
					{
						e.printStackTrace();
					}

					NKWorker nkWorker = playerManager.getPlayer(brewingStand.getPlayerName());
					if(nkWorker != null)
					{
						nkWorker.removeBrewingStand();
					}

					player.sendMessage(ChatColor.GREEN + "Vous avez détruit l'alambic de " + brewingStand.getPlayerName() + ".");
					brewingStands.get(worldName).remove(key);
					return null;
				}
			});
		}
	}

	// **************************************
	// **************************************
	// Check data
	// **************************************
	// **************************************

	// ######################################
	// checkFurnace
	// ######################################

	public String checkFurnace(Location location)
	{
		String key = "" + location.getX() + "|" + location.getY() + "|" + location.getZ();
		if(furnaces.get(location.getWorld().getName()).containsKey(key))
		{
			return furnaces.get(location.getWorld().getName()).get(key).getPlayerName();
		}
		return null;
	}

	// ######################################
	// checkBrewingStand
	// ######################################

	public String checkBrewingStand(Location location)
	{
		String key = "" + location.getX() + "|" + location.getY() + "|" + location.getZ();
		if(brewingStands.get(location.getWorld().getName()).containsKey(key))
		{
			return brewingStands.get(location.getWorld().getName()).get(key).getPlayerName();
		}
		return null;
	}

	// ######################################
	// checkBlockTimer
	// ######################################

	public Timestamp checkBlockTimer(Location location)
	{
		String key = "" + location.getX() + "|" + location.getY() + "|" + location.getZ();
		if(blocks.get(location.getWorld().getName()).containsKey(key))
		{
			if(blocks.get(location.getWorld().getName()).get(key).getTime().before(new java.sql.Date(System.currentTimeMillis())))
			{
				blocks.get(location.getWorld().getName()).remove(key);
				return null;
			}
			return blocks.get(location.getWorld().getName()).get(key).getTime();
		}
		return null;
	}

	// ######################################
	// checkChunk
	// ######################################

	public int checkChunk(Chunk chunk, String playerName)
	{
		String key = "" + chunk.getX() + "|" + chunk.getZ();
		if(chunks.get(chunk.getWorld().getName()).containsKey(key))
		{
			if(chunks.get(chunk.getWorld().getName()).get(key).hasPlayer(playerName))
			{
				return -1;
			}
			else
			{
				return chunks.get(chunk.getWorld().getName()).get(key).getPlayersAmount();
			}
		}
		return 0;
	}

	// ######################################
	// moveBlockTimer
	// ######################################

	public void moveBlockTimer(Location location, String blockFace)
	{
		double x = location.getX();
		double y = location.getY();
		double z = location.getZ();
		String key = "" + location.getX() + "|" + location.getY() + "|" + location.getZ();
		switch(blockFace)
		{
			case "NORTH":
				z--;
				break;
			case "SOUTH":
				z++;
				break;
			case "WEST":
				x--;
				break;
			case "EAST":
				x++;
				break;
			case "DOWN":
				y--;
				break;
			case "UP":
				y++;
				break;
			default:
				return;
		}
		String key2 = "" + x + "|" + y + "|" + z;
		blocks.get(location.getWorld().getName()).put(key2, new BlockTimer(x, y, z, blocks.get(location.getWorld().getName()).get(key).getTime()));
	}
}
