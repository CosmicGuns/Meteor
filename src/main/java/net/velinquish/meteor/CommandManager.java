package net.velinquish.meteor;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.velinquish.meteor.commands.ListCommand;
import net.velinquish.meteor.commands.ReloadCommand;
import net.velinquish.meteor.commands.SetCommand;
import net.velinquish.meteor.commands.SetItemCommand;
import net.velinquish.meteor.commands.SpawnCommand;
import net.velinquish.meteor.commands.VersionCommand;
import net.velinquish.utils.AnyCommand;

public class CommandManager extends Command {

	private Meteor plugin = Meteor.getInstance();

	public CommandManager(String name) {
		super(name);
		setAliases(plugin.getConfig().getStringList("plugin-aliases"));
		setDescription("Main command for GuardNPC");
	}

	@Override
	public final boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (args.length > 0)
			if ("spawn".equalsIgnoreCase(args[0])) {
				handle(new SpawnCommand(), sender, args);
				return true;
			} else if ("set".equalsIgnoreCase(args[0])) {
				handle(new SetCommand(), sender, args);
				return true;
			} else if ("setitem".equalsIgnoreCase(args[0])) {
				new SetItemCommand().execute(sender, args, false);
				return true;
			} else if ("list".equalsIgnoreCase(args[0])) {
				new ListCommand().execute(sender, args, false);
				return true;
			} else if ("reload".equalsIgnoreCase(args[0])) {
				handle(new ReloadCommand(), sender, args);
				return true;
			} else if ("ver".equalsIgnoreCase(args[0]) || "version".equalsIgnoreCase(args[0]) || "about".equalsIgnoreCase(args[0])) {
				new VersionCommand().execute(sender, args, false);
				return true;
				//TODO implement SpawnCommand w/ all necessary params
			} //TODO add /guard edit <type> <arg> - in game editor - only one for now will be /guard edit <type> setegg
		plugin.getLangManager().getNode("command-message").execute(sender);

		return false;
	}

	public void handle(AnyCommand cmd, CommandSender sender, String[] args) {
		cmd.execute(sender, args, Arrays.asList(args).contains("-s"));
	}
}
