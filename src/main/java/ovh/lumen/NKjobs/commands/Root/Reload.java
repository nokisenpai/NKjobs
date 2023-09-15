package ovh.lumen.NKjobs.commands.Root;

import org.bukkit.command.CommandSender;
import ovh.lumen.NKblank.data.NKData;
import ovh.lumen.NKblank.enums.Messages;
import ovh.lumen.NKblank.enums.Permissions;
import ovh.lumen.NKblank.interfaces.SubCommand;
import ovh.lumen.NKblank.utils.MessageParser;

public class Reload implements SubCommand
{
	public Reload()
	{

	}

	public boolean execute(CommandSender sender, String[] args)
	{
		if(!hasReloadPermissions(sender))
		{
			sender.sendMessage(Messages.PERMISSION_MISSING.toString());

			return true;
		}

		NKData.PLUGIN.reload();

		MessageParser messageParser = new MessageParser(Messages.ROOT_RELOAD_MSG.toString());
		messageParser.addArg(NKData.PLUGIN_NAME);

		sender.sendMessage(messageParser.parse());

		return true;
	}

	private boolean hasReloadPermissions(CommandSender sender)
	{
		return sender.hasPermission(Permissions.ROOT_RELOAD_CMD.toString()) ||
				sender.hasPermission(Permissions.ADMIN.toString());
	}
}
