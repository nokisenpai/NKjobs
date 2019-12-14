package be.noki_senpai.NKjobs.listeners;

import be.noki_senpai.NKjobs.managers.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.BrewingStand;
import org.bukkit.block.data.type.Furnace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.*;
import org.bukkit.material.CocoaPlant;

import java.sql.Timestamp;

public class JobsListener implements Listener
{
	private PlayerManager playerManager = null;
	private JobManager jobManager = null;
	private DataRegisterManager dataRegisterManager = null;

	public JobsListener(PlayerManager playerManager, JobManager jobManager, DataRegisterManager dataRegisterManager)
	{
		this.playerManager = playerManager;
		this.jobManager = jobManager;
		this.dataRegisterManager = dataRegisterManager;
	}

	// ######################################
	// BREAK
	// ######################################

	@EventHandler(priority = EventPriority.LOW) public void onBlockBreak(BlockBreakEvent event)
	{
		if(event.isCancelled())
		{
			return;
		}

		if(event.getPlayer().getGameMode().equals(GameMode.SURVIVAL))
		{
			Block block = event.getBlock();

			if(block.getBlockData() instanceof Furnace)
			{
				dataRegisterManager.unregisterFurnace(block.getLocation(), event.getPlayer());
			}

			if(block.getBlockData() instanceof BrewingStand)
			{
				dataRegisterManager.unregisterBrewingStand(block.getLocation(), event.getPlayer());
			}

			dataRegisterManager.registerBreakBlockTimer(block.getLocation());

			jobManager.executeBreak(playerManager.getPlayer(event.getPlayer().getName()), block.getBlockData().getMaterial().toString(), exeptionBlock(block, false), dataRegisterManager.checkBlockTimer(block.getLocation()));
		}
	}

	// ######################################
	// PLACE
	// ######################################

	@EventHandler(priority = EventPriority.LOW) public void onBlockPlace(BlockPlaceEvent event)
	{
		if(event.isCancelled())
		{
			return;
		}

		if(event.getPlayer().getGameMode().equals(GameMode.SURVIVAL))
		{
			Block block = event.getBlock();

			if(event.getBlock().getBlockData() instanceof Furnace && playerManager.getPlayer(event.getPlayer().getName()).getNbFurnace() < ConfigManager.MAXREGISTEREDFURNACES)
			{
				dataRegisterManager.registerFurnace(block.getLocation(), playerManager.getPlayer(event.getPlayer().getName()));
			}

			if(event.getBlock().getBlockData() instanceof BrewingStand && playerManager.getPlayer(event.getPlayer().getName()).getNbBrewingStand() < ConfigManager.MAXREGISTEREDBREWINGSTANDS)
			{
				dataRegisterManager.registerBrewingStand(block.getLocation(), playerManager.getPlayer(event.getPlayer().getName()));
			}

			long timer = ConfigManager.GLOBALBLOCKSTIMER;
			if(jobManager.items.containsKey(block.getBlockData().getMaterial().toString().toUpperCase()))
			{
				timer = jobManager.items.get(block.getBlockData().getMaterial().toString().toUpperCase());
			}

			dataRegisterManager.registerPlaceBlockTimer(block.getLocation(), timer);

			jobManager.executePlace(playerManager.getPlayer(event.getPlayer().getName()), block.getBlockData().getMaterial().toString(), exeptionBlock(block, false), dataRegisterManager.checkBlockTimer(block.getLocation()));
		}
	}

	// ######################################
	// KILL
	// ######################################

	@EventHandler(priority = EventPriority.LOW) public void onKill(EntityDeathEvent event)
	{
		LivingEntity entity = event.getEntity();
		if(entity.getKiller() != null)
		{
			if(entity.getKiller().getGameMode().equals(GameMode.SURVIVAL))
			{
				if(entity instanceof PigZombie && !entity.getScoreboardTags().contains(ConfigManager.HITPIGMEN))
				{
					return;
				}
				jobManager.executeKill(playerManager.getPlayer(entity.getKiller().getName()), entity.getType().name().toUpperCase(), exeptionEntity(entity), entity.getScoreboardTags().contains(ConfigManager.FROMSPAWNER));
			}
		}
	}

	// ######################################
	// FISH
	// ######################################

	@EventHandler(priority = EventPriority.LOW) public void onFish(PlayerFishEvent event)
	{
		if(event.isCancelled())
		{
			return;
		}

		if(event.getState() != PlayerFishEvent.State.CAUGHT_FISH)
		{
			return;
		}

		Item item = (Item) event.getCaught();
		if(item != null)
		{
			if(event.getPlayer().getGameMode().equals(GameMode.SURVIVAL))
			{
				jobManager.executeFish(playerManager.getPlayer(event.getPlayer().getName()), item.getItemStack().getType().name());
			}
		}
	}

	// ######################################
	// CRAFT
	// ######################################

	@EventHandler(priority = EventPriority.LOW) public void onCraft(CraftItemEvent event)
	{
		if(event.isCancelled())
		{
			return;
		}

		Player player = (Player) event.getWhoClicked();
		if(player.getGameMode().equals(GameMode.SURVIVAL))
		{
			int itemsChecked = 0;
			int possibleCreations = 1;
			if(event.isShiftClick())
			{
				for(ItemStack item : event.getInventory().getMatrix())
				{
					if(item != null && !item.getType().equals(Material.AIR))
					{
						if(itemsChecked == 0)
						{
							possibleCreations = item.getAmount();
						}
						else
						{
							possibleCreations = Math.min(possibleCreations, item.getAmount());
						}
						itemsChecked++;
					}
				}
			}
			int amount = (event.getInventory().getResult().getAmount() * possibleCreations);
			int finalAmount = (event.getInventory().getResult().getAmount() * possibleCreations);

			int empty = 0;
			for(int i = 8; i >= 0; i--)
			{
				if(amount < 0)
				{
					break;
				}
				ItemStack item = player.getInventory().getItem(i);
				if(item == null)
				{
					empty++;
					continue;
				}
				if(item.isSimilar(event.getRecipe().getResult()))
				{
					if(item.getAmount() < item.getMaxStackSize())
					{
						amount -= (item.getMaxStackSize() - item.getAmount());
					}
				}
			}
			for(int i = 35; i >= 9; i--)
			{
				if(amount < 0)
				{
					break;
				}
				ItemStack item = player.getInventory().getItem(i);
				if(item == null)
				{
					empty++;
					continue;
				}
				if(item.isSimilar(event.getRecipe().getResult()))
				{
					if(item.getAmount() < item.getMaxStackSize())
					{
						amount -= (item.getMaxStackSize()-item.getAmount());
					}
				}
			}
			if(empty >= (int) Math.ceil(1.0 * amount / event.getInventory().getResult().getMaxStackSize()))
			{
				amount = 0;
			}
			if(amount > 0)
			{
				finalAmount -= amount;
			}

			jobManager.executeCraft(playerManager.getPlayer(player.getName()), event.getRecipe().getResult().getType().toString(), finalAmount);
		}
	}

	// ######################################
	// SMELT
	// ######################################

	@EventHandler(priority = EventPriority.LOW) public void onSmelt(FurnaceSmeltEvent event)
	{
		if(event.isCancelled())
		{
			return;
		}

		String playerName = dataRegisterManager.checkFurnace(event.getBlock().getLocation());
		if(playerName == null)
		{
			return;
		}
		if(Bukkit.getPlayer(playerName) == null)
		{
			return;
		}
		if(playerManager.getPlayer(playerName) == null)
		{
			return;
		}
		if(Bukkit.getPlayer(playerName).getGameMode().equals(GameMode.SURVIVAL))
		{
			jobManager.executeSmelt(playerManager.getPlayer(playerName), event.getResult().getType().toString());
		}
	}

	// ######################################
	// BREW
	// ######################################

	@EventHandler(priority = EventPriority.LOW) public void onBrew(BrewEvent event)
	{
		if(event.isCancelled())
		{
			return;
		}

		String playerName = dataRegisterManager.checkBrewingStand(event.getBlock().getLocation());
		if(playerName == null)
		{
			return;
		}
		if(Bukkit.getPlayer(playerName) == null)
		{
			return;
		}
		if(playerManager.getPlayer(playerName) == null)
		{
			return;
		}
		if(Bukkit.getPlayer(playerName).getGameMode().equals(GameMode.SURVIVAL))
		{
			int amount = 0;
			if(event.getContents().getItem(0) != null)
			{
				amount++;
			}
			if(event.getContents().getItem(1) != null)
			{
				amount++;
			}
			if(event.getContents().getItem(2) != null)
			{
				amount++;
			}
			jobManager.executeBrew(playerManager.getPlayer(playerName), event.getContents().getIngredient().getType().name(), amount);
		}
	}

	// ######################################
	// ENCHANT
	// ######################################

	@EventHandler(priority = EventPriority.LOW) public void onEnchant(EnchantItemEvent event)
	{
		if(event.isCancelled())
		{
			return;
		}

		Player player = (Player) event.getEnchanter();
		if(player.getGameMode().equals(GameMode.SURVIVAL))
		{
			jobManager.executeEnchant(playerManager.getPlayer(player.getName()), event.getEnchantsToAdd());
		}
	}

	// ######################################
	// REPAIR
	// ######################################

	/*@EventHandler(priority = EventPriority.LOW) public void onRepair(InventoryClickEvent event)
	{
		// check whether the event has been cancelled by another plugin
		if(event.isCancelled())
		{
			return;
		}
		Player player = (Player) event.getWhoClicked();

		// Check if player is in survival mode
		if(!player.getGameMode().equals(GameMode.SURVIVAL))
		{
			return;
		}
		Inventory inv = event.getInventory();

		// see if we are talking about an anvil here
		if(!(inv instanceof AnvilInventory))
		{
			return;
		}
		AnvilInventory anvil = (AnvilInventory) inv;
		InventoryView view = event.getView();
		int rawSlot = event.getRawSlot();

		// compare raw slot to the inventory view to make sure we are in the upper inventory
		if(rawSlot != view.convertSlot(rawSlot))
		{
			return;
		}

		// 2 = result slot
		if(rawSlot != 2)
		{
			return;
		}

		// item in the result slot
		ItemStack item = event.getCurrentItem();

		// check if there is an item in the result slot
		if(item == null)
		{
			return;
		}
		player.sendMessage(item.getType().name());

		ItemMeta meta = item.getItemMeta();

		// meta data could be null
		if(meta == null)
		{
			return;
		}
		player.sendMessage(meta.toString());
		// get the repairable interface to obtain the repair cost
		if(!(meta instanceof Repairable))
		{
			return;
		}
		Repairable repairable = (Repairable) meta;

		// can the player afford to repair the item
		if(player.getLevel() >= repairable.getRepairCost())
		{
			jobManager.executeRepair(playerManager.getPlayer(player.getName()), item.getType().name());
		}
	}*/

	// ######################################
	// BREED
	// ######################################

	@EventHandler(priority = EventPriority.LOW) public void onBreed(EntityBreedEvent event)
	{
		if(event.isCancelled())
		{
			return;
		}

		Player player = (Player) event.getBreeder();
		if(player.getGameMode().equals(GameMode.SURVIVAL))
		{
			jobManager.executeBreed(playerManager.getPlayer(player.getName()), event.getEntity().getType().name().toUpperCase());
		}
	}

	// ######################################
	// TAME
	// ######################################

	@EventHandler(priority = EventPriority.LOW) public void onTame(EntityTameEvent event)
	{
		if(event.isCancelled())
		{
			return;
		}

		Player player = (Player) event.getOwner();
		if(player.getGameMode().equals(GameMode.SURVIVAL))
		{
			jobManager.executeTame(playerManager.getPlayer(player.getName()), event.getEntity().getType().name().toUpperCase());
		}
	}

	// ######################################
	// MILK
	// ######################################

	@EventHandler(priority = EventPriority.LOW) public void onMilk(PlayerInteractEntityEvent event)
	{
		if(event.isCancelled())
		{
			return;
		}

		Player player = (Player) event.getPlayer();
		if(player.getGameMode().equals(GameMode.SURVIVAL))
		{
			if(event.getRightClicked() instanceof Cow && player.getItemInHand().getType().equals(Material.BUCKET))
			{
				jobManager.executeMilk(playerManager.getPlayer(player.getName()), "COW");
			}
		}
	}

	// ######################################
	// DYE
	// ######################################

	/*@EventHandler(priority = EventPriority.LOW) public void onDye(PlayerInteractEntityEvent event)
	{
		Player player = (Player) event.getPlayer();
		if(player.getGameMode().equals(GameMode.SURVIVAL))
		{

		}
	}*/

	// ######################################
	// SHEAR
	// ######################################

	@EventHandler(priority = EventPriority.LOW) public void onShear(PlayerShearEntityEvent event)
	{
		if(event.isCancelled())
		{
			return;
		}

		Player player = (Player) event.getPlayer();
		if(player.getGameMode().equals(GameMode.SURVIVAL))
		{
			jobManager.executeShear(playerManager.getPlayer(player.getName()), event.getEntity().getType().name().toUpperCase());
		}
	}

	// ######################################
	// EXPLORE
	// ######################################

	@EventHandler(priority = EventPriority.LOW) public void onPlayerMove(PlayerMoveEvent event)
	{
		if(event.isCancelled())
		{
			return;
		}

		if(event.getFrom().getChunk().equals(event.getTo().getChunk()))
		{
			Player player = (Player) event.getPlayer();

			if(player.getGameMode().equals(GameMode.SURVIVAL))
			{
				int amount = dataRegisterManager.checkChunk(event.getTo().getChunk(), player.getName());
				if(amount == -1)
				{
					return;
				}
				if(jobManager.executeExplore(playerManager.getPlayer(player.getName()), amount))
				{
					dataRegisterManager.registerChunk(event.getTo().getChunk(), player.getName());
				}
			}
		}

	}

	// ######################################
	// Tag entities from spawner
	// ######################################

	@EventHandler public void onEntitySpawn(SpawnerSpawnEvent e)
	{
		e.getEntity().addScoreboardTag(ConfigManager.FROMSPAWNER);
	}

	// ######################################
	// Tag pigmen on hit by player
	// ######################################

	@EventHandler public void onEntitySpawn(EntityDamageByEntityEvent e)
	{
		if(e.getEntity() instanceof PigZombie)
		{
			e.getEntity().addScoreboardTag(ConfigManager.HITPIGMEN);
		}
	}

	@SuppressWarnings("deprecation") public int exeptionBlock(Block block, boolean place)
	{
		int i = -1;
		if(place)
		{
			switch(block.getBlockData().getMaterial().toString())
			{
				case "WHEAT":
				case "CARROTS":
				case "POTATOES":
				case "NETHER_WART":
				case "BEETROOTS":
				case "COCOA":
				case "CHORUS_FLOWER":
					i = 0;
					break;
				default:
					break;
			}
		}
		else
		{
			switch(block.getBlockData().getMaterial().toString())
			{
				case "WHEAT":
				case "CARROTS":
				case "POTATOES":
				case "NETHER_WART":
				case "BEETROOTS":
				case "CHORUS_FLOWER":
					i = (int) block.getData();
					break;
				case "COCOA":
					i = ((CocoaPlant) block.getState().getData()).getSize().ordinal();
				default:
					break;
			}
		}
		return i;
	}

	public int exeptionEntity(LivingEntity entity)
	{
		int i = -1;

		switch(entity.getType().name().toUpperCase())
		{
			case "SLIME":
				i = ((Slime) entity).getSize();
				break;
			case "MAGMA_CUBE":
				i = ((MagmaCube) entity).getSize();
				break;
			default:
				break;
		}

		return i;
	}
}
