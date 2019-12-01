package be.noki_senpai.NKjobs.managers;

import be.noki_senpai.NKjobs.NKjobs;
import be.noki_senpai.NKjobs.data.*;
import be.noki_senpai.NKjobs.utils.CheckType;
import be.noki_senpai.NKjobs.utils.Formatter;
import com.google.common.collect.Multimap;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class JobManager
{
	private ConsoleCommandSender console = null;
	public Map<String, Job> jobs = new HashMap<>();
	public Map<String, Integer> items = new HashMap<>();

	//Material list
	public static List<String> material = new ArrayList<String>();

	public static int nbJobs = 0;

	public JobManager()
	{
		console = Bukkit.getConsoleSender();

		for(Material materialType : Material.values())
		{
			material.add(materialType.toString().toUpperCase());
		}
	}

	public void unloadJob()
	{
		jobs.clear();
	}

	@SuppressWarnings("unchecked") public boolean loadJob()
	{
		List<String> possibleActions = new ArrayList<String>();
		List<String> possibleColors = new ArrayList<String>();
		Connection bdd = null;
		PreparedStatement ps = null;

		possibleActions.add("BREAK"); // fait
		possibleActions.add("PLACE"); // fait
		possibleActions.add("KILL"); // fait
		possibleActions.add("FISH"); // fait
		possibleActions.add("CRAFT"); // fait
		possibleActions.add("SMELT"); // fait
		possibleActions.add("BREW"); // fait
		possibleActions.add("ENCHANT"); // fait
		//possibleActions.add("REPAIR"); // inutile
		possibleActions.add("BREED"); // fait
		possibleActions.add("TAME"); // fait
		possibleActions.add("MILK"); // fait
		//possibleActions.add("DYE"); // inutile
		possibleActions.add("SHEAR"); // fait
		possibleActions.add("EXPLORE"); // fait

		possibleColors.add("RED");
		possibleColors.add("YELLOW");
		possibleColors.add("GREEN");
		possibleColors.add("PURPLE");
		possibleColors.add("BLUE");
		possibleColors.add("PINK");
		possibleColors.add("WHITE");

		Yaml yaml = new Yaml();
		InputStream inputStream = null;

		try
		{
			// Load file restrictedBlocks.yaml
			inputStream = new FileInputStream(NKjobs.getPlugin().getDataFolder() + "/restrictedBlocks.yaml");
		}
		catch(FileNotFoundException e)
		{
			console.sendMessage(ChatColor.DARK_RED + NKjobs.PNAME + " Error while loading restrictedBlocks.yaml.");
			return false;
		}

		if(inputStream != null)
		{
			// Store yaml file as map objects
			Map<Object, Object> obj = yaml.load(inputStream);

			String error = "";

			// ######################################
			// Get blocks-timer list
			// ######################################

			// Check if "blocks-timer" key is on obj
			if(!(obj.containsKey("blocks-timer")))
			{
				error += "\n> Key 'blocks-timer' not found. Verify syntax of keys.";
			}

			// ######################################
			// Check config errors
			// ######################################

			// Check if there are errors
			if(!error.equals(""))
			{
				console.sendMessage(ChatColor.DARK_RED + NKjobs.PNAME + " Error while parsing restrictedBlocks.yaml." + error);
				return false;
			}

			// Get blocks timer list
			Map<String, Object> blocksTimer = (Map<String, Object>) obj.get("blocks-timer");

			// ######################################
			// Block timer
			// ######################################

			for(Map.Entry<String, Object> blockTimer : blocksTimer.entrySet())
			{
				error = "";

				String blockName = blockTimer.getKey();
				int timer = -1;

				// Check if timer is a number
				if(!CheckType.isNumber(String.valueOf(blockTimer.getValue())))
				{
					error += "\n> " + blockTimer.getKey() + " > Value " + blockTimer.getValue() + " is not a number. Please only use numbers.";
				}
				else
				{
					// Get timer
					timer = (int) blockTimer.getValue();
				}

				// Check if there are errors
				if(!error.equals(""))
				{
					console.sendMessage(ChatColor.DARK_RED + NKjobs.PNAME + " Error while parsing restrictedBlocks.yaml." + error);
					continue;
				}

				items.put(blockName, timer);
			}
			console.sendMessage(ChatColor.GREEN + NKjobs.PNAME + " Loaded " + items.size() + " timer specification from restrictedBlocks.yaml.");
		}
		else
		{
			console.sendMessage(ChatColor.DARK_RED + NKjobs.PNAME + " Error : inputStream is null");
			return false;
		}
















		try
		{
			// Load file jobConfig.yml
			inputStream = new FileInputStream(NKjobs.getPlugin().getDataFolder() + "/jobConfig.yaml");
		}
		catch(FileNotFoundException e)
		{
			console.sendMessage(ChatColor.DARK_RED + NKjobs.PNAME + " Error while loading jobConfig.yaml.");
			return false;
		}

		if(inputStream != null)
		{
			// Store yaml file as map objects
			Map<Object, Object> obj = yaml.load(inputStream);

			// For each entry (job id)
			for(Map.Entry<Object, Object> entry : obj.entrySet())
			{
				String error = "";

				int jobId = -1;
				String jobName = "";
				int jobLvlMax = -1;
				String color = "";
				double baseLeveling = -1;
				double ratioLeveling = -1;
				double ratioExp = -1;
				double ratioMoney = -1;

				// ######################################
				// All job data
				// ######################################

				// Get job data
				Map<String, Object> dataJob = (Map<String, Object>) entry.getValue();

				// ######################################
				// Job id
				// ######################################

				// Check if job id is a number
				if(!CheckType.isNumber(String.valueOf(entry.getKey())))
				{
					error += "\nJob id '" + entry.getKey() + "' is not a number. Please only use numbers.";
				}
				else
				{
					jobId = (int) entry.getKey();
				}

				// ######################################
				// Job name
				// ######################################

				// Check if "name" key is on dataJob
				if(!(dataJob.containsKey("name")))
				{
					error += "\nKey 'name' not found. Verify syntax of keys.";
				}
				else
				{
					// Get job name
					jobName = dataJob.get("name").toString().toLowerCase();

					// ######################################
					// Job lvl_max
					// ######################################

					// Check if "lvl_max" key is on dataJob
					if(!(dataJob.containsKey("lvl_max")))
					{
						error += "\n> " + jobName + " > Key 'lvl_max' not found. Verify syntax of keys.";
					}
					else
					{
						// Check if lvl_max is a number
						if(!CheckType.isNumber(String.valueOf(dataJob.get("lvl_max"))))
						{
							error += "\n> " + jobName + " > Value of key 'lvl_max' is not a number. Please only use numbers.";
						}
						else
						{
							// Get lvl max
							jobLvlMax = (int) dataJob.get("lvl_max");
						}
					}

					// ######################################
					// Job color
					// ######################################

					// Check if "color" key is on dataJob
					if(!(dataJob.containsKey("color")))
					{
						error += "\n> " + jobName + " > Key 'color' not found. Verify syntax of keys.";
					}
					else
					{
						// Check if "color" is a valid color
						if(!possibleColors.contains(String.valueOf(dataJob.get("color")).toUpperCase()))
						{
							error += "\n> " + jobName + " > '" + String.valueOf(dataJob.get("color"))
									+ "' is not a correct value for key 'color'. Possible values are " + possibleColors;
						}
						else
						{
							// Get color
							color = dataJob.get("color").toString().toUpperCase();
						}
					}

					// ######################################
					// Job equation_leveling
					// ######################################

					// Check if "equation_leveling" key is on dataJob
					if(!(dataJob.containsKey("equation_leveling")))
					{
						error += "\n> " + jobName + " > Key 'equation_leveling' not found. Verify syntax of keys.";
					}
					else
					{
						// Get equationLeveling object
						Map<Object, Object> equationLeveling = (Map<Object, Object>) dataJob.get("equation_leveling");

						// ######################################
						// Job base leveling
						// ######################################

						// Check if "base" key is on equationLeveling
						if(!(equationLeveling.containsKey("base")))
						{
							error += "\n> " + jobName + " > equation_leveling > Key 'base' not found. Verify syntax of keys.";
						}
						else
						{
							// Check if base is a number
							if(!CheckType.isNumber(String.valueOf(equationLeveling.get("base"))))
							{
								error += "\n> " + jobName + " > equation_leveling > Value of key 'base' is not a number. Please only use numbers.";
							}
							else
							{
								// Get base leveling
								baseLeveling = (double) ((Integer) equationLeveling.get("base")).intValue();
							}
						}

						// ######################################
						// Job ratio leveling
						// ######################################

						// Check if "ratio" key is on equationLeveling
						if(!(equationLeveling.containsKey("ratio")))
						{
							error += "\n> " + jobName + " > equation_leveling > Key 'ratio' not found. Verify syntax of keys.";
						}
						else
						{
							// Check if ratio is a number
							if(!CheckType.isNumber(String.valueOf(equationLeveling.get("ratio"))))
							{
								error += "\n> " + jobName + " > equation_leveling > Value of key 'ratio' is not a number. Please only use numbers.";
							}
							else
							{
								// Get ratio leveling
								ratioLeveling = (double) ((Integer) equationLeveling.get("ratio")).intValue();
							}
						}
					}

					// ######################################
					// Job equation_exp
					// ######################################

					// Check if "equation_exp" key is on dataJob
					if(!(dataJob.containsKey("equation_exp")))
					{
						error += "\n> " + jobName + " > Key 'equation_exp' not found. Verify syntax of keys.";
					}
					else
					{
						// Get equationExp object
						Map<Object, Object> equationExp = (Map<Object, Object>) dataJob.get("equation_exp");

						// ######################################
						// Job ratio exp
						// ######################################

						// Check if "ratio" key is on equationExp
						if(!(equationExp.containsKey("ratio")))
						{
							error += "\n> " + jobName + " > equation_exp > Key 'ratio' not found. Verify syntax of keys.";
						}
						else
						{
							// Check if ratio is a number
							if(!CheckType.isNumber(String.valueOf(equationExp.get("ratio"))))
							{
								error += "\n> " + jobName + " > equation_exp > Value of key 'ratio' is not a number. Please only use numbers.";
							}
							else
							{
								// Get ratio exp
								ratioExp = (double) ((Integer) equationExp.get("ratio")).intValue();
							}
						}
					}

					// ######################################
					// Job equation_money
					// ######################################

					// Check if "equation_money" key is on dataJob
					if(!(dataJob.containsKey("equation_money")))
					{
						error += "\n> " + jobName + " > Key 'equation_money' not found. Verify syntax of keys.";
					}
					else
					{
						// Get equationMoney object
						Map<Object, Object> equationMoney = (Map<Object, Object>) dataJob.get("equation_money");

						// ######################################
						// Job ratio money
						// ######################################

						// Check if "ratio" key is on equationMoney
						if(!(equationMoney.containsKey("ratio")))
						{
							error += "\n> " + jobName + " > equation_money > Key 'ratio' not found. Verify syntax of keys.";
						}
						else
						{
							// Check if ratio is a number
							if(!CheckType.isNumber(String.valueOf(equationMoney.get("ratio"))))
							{
								error += "\n> " + jobName + " > equation_money > Value of key 'ratio' is not a number. Please only use numbers.";
							}
							else
							{
								// Get ratio money
								ratioMoney = (double) ((Integer) equationMoney.get("ratio")).intValue();
							}
						}
					}

					// Check if "action" key is on dataJob
					if(!(dataJob.containsKey("action")))
					{
						error += "\n> " + jobName + " > Key 'action' not found. Verify syntax of keys.";
					}
				}

				// ######################################
				// Check config errors
				// ######################################

				// Check if there are errors
				if(!error.equals(""))
				{
					console.sendMessage(ChatColor.DARK_RED + NKjobs.PNAME + " Error while parsing jobConfig.yaml." + error);
					continue;
				}

				// ######################################
				// Store the job
				// ######################################

				// Store job data
				Job job = new Job(jobId, jobName, jobLvlMax, color, baseLeveling, ratioLeveling, ratioExp, ratioMoney);

				// ######################################
				// Job actions
				// ######################################

				Map<Object, Object> actions = (Map<Object, Object>) dataJob.get("action");

				for(Map.Entry<Object, Object> entryAction : actions.entrySet())
				{
					error = "";
					String actionName = "";
					List<JobItem> data = new ArrayList<JobItem>();

					// ######################################
					// action name
					// ######################################

					// Get action name
					Object action = entryAction.getKey();

					Map<Object, Object> dataAction_ = (Map<Object, Object>) entryAction.getValue();

					// Check if it is a valid action name
					if(!possibleActions.contains(action.toString().toUpperCase()))
					{
						error += "\n> " + jobName + " > '" + action.toString() + "' is not a correct value for key 'action'. Possible values are "
								+ possibleActions;
					}
					else
					{
						actionName = action.toString().toUpperCase();
					}

					// ######################################
					// Check config errors
					// ######################################

					// Check if there are errors
					if(!error.equals(""))
					{
						console.sendMessage(ChatColor.DARK_RED + NKjobs.PNAME + " Error while parsing jobConfig.yaml." + error);
						continue;
					}

					// ######################################
					// action data
					// ######################################

					for(Map.Entry<Object, Object> entryDataAction : dataAction_.entrySet())
					{
						error = "";
						String itemName = "";
						double itemBaseExp = -1;
						double itemBaseMoney = -1;
						// ######################################
						// action item name
						// ######################################

						// Get action item name
						itemName = String.valueOf(entryDataAction.getKey());
						// I don't check if item name is a valid Vanilla item name

						Map<String, Object> value_ = (Map<String, Object>) entryDataAction.getValue();

						// ######################################
						// action item base_exp
						// ######################################

						if(value_ == null)
						{
							console.sendMessage("" + ChatColor.DARK_RED + jobName + " > " + actionName + " > " + itemName + value_);
						}

						if(!(value_.containsKey("base_exp")))
						{
							error += "\n> " + jobName + " > " + actionName + " > " + itemName + " > Key 'base_exp' not found. Verify syntax of keys.";
						}
						else
						{
							// Check if base_exp is a number
							if(!CheckType.isNumber(String.valueOf(value_.get("base_exp"))))
							{
								error += "\n> " + jobName + " > " + actionName + " > " + itemName
										+ " > Value of key 'base_exp' is not a number. Please only use numbers.";
							}
							else
							{
								// Get base exp
								itemBaseExp = (double) value_.get("base_exp");
							}
						}

						// ######################################
						// action item base_money
						// ######################################

						if(!(value_.containsKey("base_money")))
						{
							error += "\n> " + jobName + " > " + actionName + " > " + itemName
									+ " > Key 'base_money' not found. Verify syntax of keys.";
						}
						else
						{
							// Check if base_money is a number
							if(!CheckType.isNumber(String.valueOf(value_.get("base_money"))))
							{
								error += "\n> " + jobName + " > " + actionName + " > " + itemName
										+ " > Value of key 'base_money' is not a number. Please only use numbers.";
							}
							else
							{
								// Get base money
								itemBaseMoney = (double) value_.get("base_money");
							}
						}

						// ######################################
						// Check config errors
						// ######################################

						// Check if there are errors
						if(!error.equals(""))
						{
							console.sendMessage(ChatColor.DARK_RED + NKjobs.PNAME + " Error while parsing jobConfig.yaml." + error);
							continue;
						}

						// ######################################
						// Store the item data
						// ######################################

						// Store item data
						data.add(new JobItem(itemName.toUpperCase(), itemBaseExp, itemBaseMoney));
					}

					// ######################################
					// Store the action data
					// ######################################

					// Store action data
					job.addAction(actionName, data);
				}

				// ######################################
				// Job rewards
				// ######################################

				if(dataJob.containsKey("reward"))
				{

					Map<Object, Object> rewards = (Map<Object, Object>) dataJob.get("reward");

					for(Map.Entry<Object, Object> entryReward : rewards.entrySet())
					{
						error = "";
						int rewardLevel = -1;
						List<ItemStack> items = new ArrayList<ItemStack>();
						List<String> commands = new ArrayList<String>();
						List<String> permissions = new ArrayList<String>();

						// ######################################
						// reward level
						// ######################################

						// Get reward level
						Object reward = entryReward.getKey();

						//Map<Object, Object> dataAction_ = (Map<Object, Object>) entryAction.getValue();

						// Check if reward level is a number
						if(!CheckType.isNumber(String.valueOf(reward)))
						{
							error += "\n> " + jobName + " > reward" + " > '" + reward.toString() + "' is not a number. Please only use numbers.";
						}
						else
						{
							// Get reward level
							rewardLevel = (int) reward;
						}

						// ######################################
						// Check config errors
						// ######################################

						// Check if there are errors
						if(!error.equals(""))
						{
							console.sendMessage(ChatColor.DARK_RED + NKjobs.PNAME + " Error while parsing jobConfig.yaml." + error);
							continue;
						}

						// ######################################
						// reward data
						// ######################################

						Map<String, Object> value_ = (Map<String, Object>) entryReward.getValue();

						// ######################################
						// items
						// ######################################

						if(value_ == null)
						{
							console.sendMessage("" + ChatColor.DARK_RED + jobName + " > reward > " + value_);
						}

						if(value_.containsKey("items"))
						{
							Map<String, Object> entryItems = (Map<String, Object>) value_.get("items");

							for(Map.Entry<String, Object> item_ : entryItems.entrySet())
							{
								Map<String, Object> item = (Map<String, Object>) item_.getValue();
								if(!(item.containsKey("id")))
								{
									continue;
								}

								String id = (String) item.get("id");
								String name = "";
								Map<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();

								boolean unbreakable = false;
								boolean preventLost = false;

								if(item.containsKey("name"))
								{
									name = (String) item.get("name");
								}

								if(item.containsKey("enchantments"))
								{
									Map<String, Object> entryEnchantments = (Map<String, Object>) item.get("enchantments");
									for(Map.Entry<String, Object> enchant : entryEnchantments.entrySet())
									{
										// Check if the value is a number
										if(CheckType.isNumber(String.valueOf(enchant.getValue())))
										{
											enchantments.put(Enchantment.getByName(enchant.getKey().toUpperCase()), (int) enchant.getValue());
										}
									}
								}

								if(item.containsKey("unbreakable"))
								{
									unbreakable = (boolean) item.get("unbreakable");
								}

								if(item.containsKey("prevent_lost"))
								{
									preventLost = (boolean) item.get("prevent_lost");
								}

								Material material = Material.matchMaterial(id.toUpperCase());
								if(material == null)
								{
									continue;
								}

								ItemStack itemStack = new ItemStack(material, 1);

								if(itemStack == null)
								{
									continue;
								}

								if(enchantments.size() != 0)
								{
									itemStack.addUnsafeEnchantments(enchantments);
								}

								ItemMeta itemMeta = itemStack.getItemMeta();

								if(itemMeta == null)
								{
									continue;
								}

								if(!name.equals(""))
								{
									itemMeta.setDisplayName(name);
								}

								if(unbreakable)
								{
									itemMeta.setUnbreakable(true);
								}

								if(preventLost)
								{
									itemMeta.getPersistentDataContainer().set(new NamespacedKey(NKjobs.getPlugin(), "NKprotect"), PersistentDataType.INTEGER, 1);
								}

								itemStack.setItemMeta(itemMeta);

								items.add(itemStack);
							}
						}

						// ######################################
						// Store the reward data
						// ######################################

						// Store reward data
						job.addReward(rewardLevel, new Reward(items, commands, permissions));
					}
				}









				// ######################################
				// Store the job
				// ######################################

				// Store the job data
				jobs.put(jobName, job);
			}

			console.sendMessage(ChatColor.GREEN + NKjobs.PNAME + " Loaded " + jobs.size() + " job(s) from jobConfig.yaml.");

			// ######################################
			// Store job name to SQL database
			// ######################################

			String req = "INSERT INTO " + DatabaseManager.table.JOBS + " (id, name, old) VALUES ";
			for(Map.Entry<String, Job> entry : jobs.entrySet())
			{
				req += "(" + entry.getValue().id + ", '" + entry.getValue().name + "', false),";
			}

			req = req.substring(0, req.length() - 1);
			req += " ON DUPLICATE KEY UPDATE name = VALUES(name), old = false;";

			try
			{
				// Get bdd connection
				bdd = DatabaseManager.getConnection();

				// Put all job in database old
				ps = bdd.prepareStatement("UPDATE " + DatabaseManager.table.JOBS + " SET old = true");
				ps.executeUpdate();
				ps.close();

				// Execute "req" request
				ps = bdd.prepareStatement(req);
				ps.executeUpdate();
				ps.close();

				bdd.close();
			}
			catch(SQLException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Get number of job
			nbJobs = jobs.size();
		}
		else
		{
			console.sendMessage(ChatColor.DARK_RED + NKjobs.PNAME + " Error : inputStream is null");
			return false;
		}


		for(Map.Entry<String, Job> entry : jobs.entrySet())
		{
			console.sendMessage(ChatColor.BLUE + entry.getValue().name);
			for(Map.Entry<Integer, Reward> entry2 : entry.getValue().getRewards().entrySet())
			{
				console.sendMessage(""+ChatColor.BLUE + entry2.getKey());
				console.sendMessage(""+ChatColor.BLUE + entry2.getValue().getItems());
			}

		}


		return true;
	}

	// ######################################
	//	Top jobs
	// ######################################

	public Map<String, PlayerJob> getTop(String jobName, int page)
	{
		if(page >= 1)
		{
			Map<String, PlayerJob> topJob = new LinkedHashMap<String, PlayerJob>();

			Connection bdd = null;
			PreparedStatement ps = null;
			ResultSet resultat = null;
			String req = null;

			try
			{
				bdd = DatabaseManager.getConnection();

				req = "SELECT p.name as name, lvl, xp, xp_goal FROM " + DatabaseManager.table.PLAYER_JOBS + " LEFT JOIN "
						+ DatabaseManager.table.PLAYERS + " p ON p.id = player_id ORDER BY lvl, xp DESC WHERE job_id = ? LIMIT ?, 10";
				ps = bdd.prepareStatement(req);
				ps.setInt(1, jobs.get(jobName).id);
				ps.setInt(2, (page - 1) * 10);

				resultat = ps.executeQuery();

				while(resultat.next())
				{
					topJob.put(resultat.getString("name"), new PlayerJob(0, jobName, resultat.getInt("lvl"), resultat.getDouble("xp"), resultat.getDouble("xp_goal"), 0, null, null));
				}

				ps.close();
				resultat.close();
				return topJob;
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}

	// ######################################
	// executeBreak
	// ######################################

	public void executeBreak(NKPlayer player, String item, int itemData)
	{
		if(itemData >= 0)
		{
			for(Map.Entry<String, PlayerJob> entry : player.getJobs().entrySet())
			{
				String jobName = entry.getValue().name;
				if(jobs.get(jobName).getActions().containsKey("BREAK"))
				{
					for(JobItem data : jobs.get(jobName).getActions().get("BREAK"))
					{
						if(data.getName().equals(item + "-" + itemData) || data.getName().equals(item))
						{
							rewardPlayer(player, jobName, data.getMoney(), data.getExp());
						}
					}
				}
			}
		}
		else
		{
			for(Map.Entry<String, PlayerJob> entry : player.getJobs().entrySet())
			{
				String jobName = entry.getValue().name;
				if(jobs.get(jobName).getActions().containsKey("BREAK"))
				{
					for(JobItem data : jobs.get(jobName).getActions().get("BREAK"))
					{
						if(data.getName().equals(item))
						{
							rewardPlayer(player, jobName, data.getMoney(), data.getExp());
						}
					}
				}
			}
		}
	}

	// ######################################
	// executePlace
	// ######################################

	public void executePlace(NKPlayer player, String item, int itemData)
	{
		if(itemData >= 0)
		{
			for(Map.Entry<String, PlayerJob> entry : player.getJobs().entrySet())
			{
				String jobName = entry.getValue().name;
				if(jobs.get(jobName).getActions().containsKey("PLACE"))
				{
					for(JobItem data : jobs.get(jobName).getActions().get("PLACE"))
					{
						if(data.getName().equals(item + "-" + itemData) || data.getName().equals(item))
						{
							rewardPlayer(player, jobName, data.getMoney(), data.getExp());
						}
					}
				}
			}
		}
		else
		{
			for(Map.Entry<String, PlayerJob> entry : player.getJobs().entrySet())
			{
				String jobName = entry.getValue().name;
				if(jobs.get(jobName).getActions().containsKey("PLACE"))
				{
					for(JobItem data : jobs.get(jobName).getActions().get("PLACE"))
					{
						if(data.getName().equals(item))
						{
							rewardPlayer(player, jobName, data.getMoney(), data.getExp());
						}
					}
				}
			}
		}
	}

	// ######################################
	// executeKill
	// ######################################

	public void executeKill(NKPlayer player, String entity, int entityData, boolean fromSpawner)
	{
		if(entityData >= 0)
		{
			for(Map.Entry<String, PlayerJob> entry : player.getJobs().entrySet())
			{
				String jobName = entry.getValue().name;
				if(jobs.get(jobName).getActions().containsKey("KILL"))
				{
					for(JobItem data : jobs.get(jobName).getActions().get("KILL"))
					{
						if(data.getName().equals(entity + "-" + entityData) || data.getName().equals(entity))
						{
							double money = data.getMoney();
							double xp = data.getExp();
							if(fromSpawner)
							{
								money *= ConfigManager.RATIOFROMSPAWNER;
								xp *= ConfigManager.RATIOFROMSPAWNER;
							}
							rewardPlayer(player, jobName, money, xp);
						}
					}
				}
			}
		}
		else
		{
			for(Map.Entry<String, PlayerJob> entry : player.getJobs().entrySet())
			{
				String jobName = entry.getValue().name;
				if(jobs.get(jobName).getActions().containsKey("KILL"))
				{
					for(JobItem data : jobs.get(jobName).getActions().get("KILL"))
					{
						double money = data.getMoney();
						double xp = data.getExp();
						if(fromSpawner)
						{
							money *= ConfigManager.RATIOFROMSPAWNER;
							xp *= ConfigManager.RATIOFROMSPAWNER;
						}
						if(data.getName().equals(entity))
						{
							rewardPlayer(player, jobName, money, xp);
						}
					}
				}
			}
		}
	}

	// ######################################
	// executeFish
	// ######################################

	public void executeFish(NKPlayer player, String entity)
	{
		for(Map.Entry<String, PlayerJob> entry : player.getJobs().entrySet())
		{
			String jobName = entry.getValue().name;
			if(jobs.get(jobName).getActions().containsKey("FISH"))
			{
				for(JobItem data : jobs.get(jobName).getActions().get("FISH"))
				{
					if(data.getName().equals(entity))
					{
						rewardPlayer(player, jobName, data.getMoney(), data.getExp());
					}
				}
			}
		}
	}

	// ######################################
	// executeCraft
	// ######################################

	public void executeCraft(NKPlayer player, String craft, int amount)
	{
		for(Map.Entry<String, PlayerJob> entry : player.getJobs().entrySet())
		{
			String jobName = entry.getValue().name;
			if(jobs.get(jobName).getActions().containsKey("CRAFT"))
			{
				for(JobItem data : jobs.get(jobName).getActions().get("CRAFT"))
				{
					if(data.getName().equals(craft))
					{
						rewardPlayer(player, jobName, data.getMoney()*amount, data.getExp()*amount);
					}
				}
			}
		}
	}

	// ######################################
	// executeSmelt
	// ######################################

	public void executeSmelt(NKPlayer player, String smelt)
	{
		for(Map.Entry<String, PlayerJob> entry : player.getJobs().entrySet())
		{
			String jobName = entry.getValue().name;
			if(jobs.get(jobName).getActions().containsKey("SMELT"))
			{
				for(JobItem data : jobs.get(jobName).getActions().get("SMELT"))
				{
					if(data.getName().equals(smelt))
					{
						rewardPlayer(player, jobName, data.getMoney(), data.getExp());
					}
				}
			}
		}
	}

	// ######################################
	// executeBrew
	// ######################################

	public void executeBrew(NKPlayer player, String brew, int amount)
	{
		for(Map.Entry<String, PlayerJob> entry : player.getJobs().entrySet())
		{
			String jobName = entry.getValue().name;
			if(jobs.get(jobName).getActions().containsKey("BREW"))
			{
				for(JobItem data : jobs.get(jobName).getActions().get("BREW"))
				{
					if(data.getName().equals(brew))
					{
						rewardPlayer(player, jobName, data.getMoney()*amount, data.getExp()*amount);
					}
				}
			}
		}
	}

	// ######################################
	// executeEnchant
	// ######################################

	public void executeEnchant(NKPlayer player, Map<Enchantment, Integer> enchantments)
	{
		for(Map.Entry<String, PlayerJob> entry : player.getJobs().entrySet())
		{
			String jobName = entry.getValue().name;
			if(jobs.get(jobName).getActions().containsKey("ENCHANT"))
			{
				for(JobItem data : jobs.get(jobName).getActions().get("ENCHANT"))
				{
					for(Map.Entry<Enchantment, Integer> enchant : enchantments.entrySet())
					{
						if(data.getName().equals(enchant.getKey().getKey().getKey() + "-" + enchant.getValue())
								|| data.getName().equals(enchant.getKey().getKey().getKey()))
						{
							rewardPlayer(player, jobName, data.getMoney(), data.getExp());
						}
					}
				}
			}
		}
	}

	/*// ######################################
	// executeRepair
	// ######################################

	public void executeRepair(NKPlayer player, String item)
	{
		for(Map.Entry<String, PlayerJob> entry : player.getJobs().entrySet())
		{
			String jobName = entry.getValue().name;
			if(jobs.get(jobName).getActions().containsKey("REPAIR"))
			{
				for(JobItem data : jobs.get(jobName).getActions().get("REPAIR"))
				{
					if(data.getName().equals(item))
					{
						rewardPlayer(player, jobName, data.getMoney(), data.getExp());
					}
				}
			}
		}
	}*/

	// ######################################
	// executeBreed
	// ######################################

	public void executeBreed(NKPlayer player, String entity)
	{
		for(Map.Entry<String, PlayerJob> entry : player.getJobs().entrySet())
		{
			String jobName = entry.getValue().name;
			if(jobs.get(jobName).getActions().containsKey("BREED"))
			{
				for(JobItem data : jobs.get(jobName).getActions().get("BREED"))
				{
					if(data.getName().equals(entity))
					{
						rewardPlayer(player, jobName, data.getMoney(), data.getExp());
					}
				}
			}
		}
	}

	// ######################################
	// executeTame
	// ######################################

	public void executeTame(NKPlayer player, String entity)
	{
		for(Map.Entry<String, PlayerJob> entry : player.getJobs().entrySet())
		{
			String jobName = entry.getValue().name;
			if(jobs.get(jobName).getActions().containsKey("TAME"))
			{
				for(JobItem data : jobs.get(jobName).getActions().get("TAME"))
				{
					if(data.getName().equals(entity))
					{
						rewardPlayer(player, jobName, data.getMoney(), data.getExp());
					}
				}
			}
		}
	}

	// ######################################
	// executeMilk
	// ######################################

	public void executeMilk(NKPlayer player, String entity)
	{
		for(Map.Entry<String, PlayerJob> entry : player.getJobs().entrySet())
		{
			String jobName = entry.getValue().name;
			if(jobs.get(jobName).getActions().containsKey("MILK"))
			{
				for(JobItem data : jobs.get(jobName).getActions().get("MILK"))
				{
					if(data.getName().equals(entity))
					{
						rewardPlayer(player, jobName, data.getMoney(), data.getExp());
					}
				}
			}
		}
	}

	// ######################################
	// executeDye
	// ######################################

	/*public void executeDye(NKPlayer player, String entity)
	{
		for(Map.Entry<String, PlayerJob> entry : player.getJobs().entrySet())
		{
			String jobName = entry.getValue().name;
			if(jobs.get(jobName).getActions().containsKey("DYE"))
			{
				for(JobItem data : jobs.get(jobName).getActions().get("DYE"))
				{
					if(data.getName().equals(entity))
					{
						rewardPlayer(player, jobName, data.getMoney(), data.getExp());
					}
				}
			}
		}
	}*/

	// ######################################
	// executeShear
	// ######################################

	public void executeShear(NKPlayer player, String entity)
	{
		for(Map.Entry<String, PlayerJob> entry : player.getJobs().entrySet())
		{
			String jobName = entry.getValue().name;
			if(jobs.get(jobName).getActions().containsKey("SHEAR"))
			{
				for(JobItem data : jobs.get(jobName).getActions().get("SHEAR"))
				{
					if(data.getName().equals(entity))
					{
						rewardPlayer(player, jobName, data.getMoney(), data.getExp());
					}
				}
			}
		}
	}

	// ######################################
	// executeExplore
	// ######################################

	public void executeExplore(NKPlayer player, int amount)
	{
		for(Map.Entry<String, PlayerJob> entry : player.getJobs().entrySet())
		{
			String jobName = entry.getValue().name;
			if(jobs.get(jobName).getActions().containsKey("EXPLORE"))
			{
				boolean rewarded = false;
				for(JobItem data : jobs.get(jobName).getActions().get("EXPLORE"))
				{
					if(data.getName().equals("EXPLORE-" + amount))
					{
						rewarded = true;
						rewardPlayer(player, jobName, data.getMoney(), data.getExp());
					}
				}
				if(!rewarded)
				{
					rewardPlayer(player, jobName, ConfigManager.MINEXPLOREMONEY, ConfigManager.MINEXPLOREEXP);
				}
			}
		}
	}

	public void rewardPlayer(NKPlayer player, String jobName, Double money, Double exp)
	{
		PlayerJob job = player.getJobs().get(jobName);

		player.addTmpMoney(jobs.get(jobName).equationMoney(money, job.lvl));
		player.addTmpExp(jobs.get(jobName).equationExp(exp, job.lvl));

		job.addExp(jobs.get(jobName).equationExp(exp, job.lvl));

		while(job.xp >= job.xpGoal && job.lvl <= jobs.get(jobName).getLvlMax())
		{
			job.lvl++;
			job.xp -= job.xpGoal;
			job.xpGoal = jobs.get(jobName).equationLeveling(job.lvl);

			congratPlayerBroadcast(player.getName(), jobName, job.lvl);
			verifyReward(player.getUuid(), jobs.get(jobName), job.lvl);
		}
	}

	public void addXp(NKPlayer player, String jobName, Double exp)
	{
		PlayerJob job = player.getJobs().get(jobName);

		job.xp += exp;
		job.xpTotal += exp;

		while(job.xp >= job.xpGoal )
		{
			job.lvl++;
			job.xp -= job.xpGoal;
			job.xpGoal = jobs.get(jobName).equationLeveling(job.lvl);

			congratPlayerBroadcast(player.getName(), jobName, job.lvl);
		}
	}

	public void removeXp(NKPlayer player, String jobName, Double exp)
	{
		PlayerJob job = player.getJobs().get(jobName);

		job.xp -= exp;
		job.xpTotal -= exp;

		while(job.xp < 0)
		{
			job.lvl--;
			job.xpGoal = jobs.get(jobName).equationLeveling(job.lvl);
			job.xp += job.xpGoal;
		}
	}

	public void setXp(NKPlayer player, String jobName, Double exp)
	{
		PlayerJob job = player.getJobs().get(jobName);

		job.xpTotal += (exp - job.xp);
		job.xp = exp;

		while(job.xp >= job.xpGoal)
		{
			job.lvl++;
			job.xp -= job.xpGoal;
			job.xpGoal = jobs.get(jobName).equationLeveling(job.lvl);

			congratPlayerBroadcast(player.getName(), jobName, job.lvl);
		}
	}

	public void addLevel(NKPlayer player, String jobName, int lvl)
	{
		PlayerJob job = player.getJobs().get(jobName);

		if(job.lvl + lvl <= jobs.get(jobName).getLvlMax())
		{
			job.lvl += lvl;
		}
		else
		{
			job.lvl = jobs.get(jobName).getLvlMax();
		}
		congratPlayerBroadcast(player.getName(), jobName, job.lvl);
	}

	public void removeLevel(NKPlayer player, String jobName, int lvl)
	{
		PlayerJob job = player.getJobs().get(jobName);

		if(job.lvl - lvl >= 0)
		{
			job.lvl -= lvl;
		}
		else
		{
			job.lvl = 0;
		}
	}

	public void setLevel(NKPlayer player, String jobName, int lvl)
	{
		PlayerJob job = player.getJobs().get(jobName);

		if(lvl <= jobs.get(jobName).getLvlMax())
		{
			job.lvl = lvl;
		}
		else
		{
			job.lvl = jobs.get(jobName).getLvlMax();
		}
		congratPlayerBroadcast(player.getName(), jobName, job.lvl);
	}

	public void congratPlayerBroadcast(String playerName, String jobName, int lvl)
	{
		if(lvl == 50 || lvl == 100)
		{
			Bukkit.broadcastMessage(ChatColor.GREEN + playerName + " est " + ChatColor.BOLD + jobName + ChatColor.RESET + ChatColor.GREEN + " niveau "
					+ ChatColor.BOLD + lvl + ChatColor.RESET + ChatColor.GREEN + " !");
		}
	}

	public void verifyReward(UUID uuid, Job job, int lvl)
	{
		if(job.getRewards().containsKey(lvl))
		{
			Bukkit.getPlayer(uuid).getInventory().addItem(job.getRewards().get(lvl).getItems().get(0));
		}
	}

	public String getJobInfo(String jobName, int page, int jobLevel)
	{
		String jobInfo = "";
		int lineNumber = 0; //max 16
		int begin = (page-1) * 16;

		Job job = jobs.get(jobName);

		int count = 0;

		int nbItems = 0;
		for(Map.Entry<String, List<JobItem>> entry : job.getActions().entrySet())
		{
			boolean added = false;

			if(lineNumber < 16)
			{
				for(JobItem jobItem : entry.getValue())
				{
					if(count >= begin && lineNumber <= 15)
					{
						if(!added && lineNumber < 16)
						{
							added = true;
							jobInfo += "\n" + ChatColor.BOLD + ChatColor.GREEN + entry.getKey() + ChatColor.RESET;
						}
						jobInfo += "\n  " + ChatColor.GRAY + TranslationManager.translate(jobItem.getName()) + " -> " + ChatColor.GREEN + NKjobs.getPlugin().getEconomy().format(job.equationMoney(jobItem.getMoney(), jobLevel)) + "  " + ChatColor.GREEN + Formatter.formatMoney(job.equationExp(jobItem.getExp(), jobLevel)) + " XP" + ChatColor.RESET;
						lineNumber++;
					}
					count++;
					if(lineNumber >= 16)
					{
						break;
					}
				}
			}
			nbItems += entry.getValue().size();
		}
		if(jobInfo.equals(""))
		{
			jobInfo += "Il n'y a rien sur cette page.";
		}
		jobInfo += "\n" + ChatColor.BOLD + "--- <<<   " + ChatColor.GREEN + page + " / " + (int) Math.ceil(nbItems/16.0) + ChatColor.RESET + ChatColor.BOLD + "   >>> ---";

		return jobInfo;
	}
}
