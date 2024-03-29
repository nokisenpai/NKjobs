package be.noki_senpai.NKjobs.managers;

import be.noki_senpai.NKjobs.NKjobs;
import be.noki_senpai.NKjobs.data.Job;
import be.noki_senpai.NKjobs.utils.CheckType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TranslationManager
{
	private ConsoleCommandSender console = null;

	private static Map<String, String> items = new HashMap<>();

	public TranslationManager()
	{
		console = Bukkit.getConsoleSender();
	}

	public void unloadTranslation()
	{
		items.clear();
	}

	public boolean loadTranslation()
	{
		Yaml yaml = new Yaml();
		InputStream inputStream = null;

		try
		{
			// Load file fr_items.yaml
			inputStream = new FileInputStream(NKjobs.getPlugin().getDataFolder() + "/fr_items.yaml");
		}
		catch(FileNotFoundException e)
		{
			console.sendMessage(ChatColor.DARK_RED + NKjobs.PNAME + " Error while loading fr_items.yaml.");
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
			if(!(obj.containsKey("translations")))
			{
				error += "\n> Key 'translations' not found. Verify syntax of keys.";
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
			Map<String, Object> translations = (Map<String, Object>) obj.get("translations");

			// ######################################
			// Block timer
			// ######################################

			for(Map.Entry<String, Object> translation : translations.entrySet())
			{
				String blockID = translation.getKey();
				String blockTranslated = String.valueOf(translation.getValue());

				items.put(blockID, blockTranslated);
			}
			return true;
		}
		else
		{
			console.sendMessage(ChatColor.DARK_RED + NKjobs.PNAME + " Error : inputStream is null");
			return false;
		}
	}

	public static String translate(String item)
	{
		if(items.containsKey(item))
		{
			return items.get(item);
		}
		return item;
	}
}
