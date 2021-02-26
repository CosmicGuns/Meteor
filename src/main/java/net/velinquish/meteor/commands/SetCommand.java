package net.velinquish.meteor.commands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import net.velinquish.meteor.Meteor;
import net.velinquish.utils.AnyCommand;

public class SetCommand extends AnyCommand {

	private Meteor plugin = Meteor.getInstance();

	/**
	 * The only location that can go here is where the meteor will ACTUALLY land.
	 * The listener and the meteor deleter need to be able to find it.
	 */
	@Override
	protected void run(CommandSender sender, String[] args, boolean silent) {
		checkPermission(plugin.getPermission());
		checkArgs(2, plugin.getLangManager().getNode("command-set-usage"));

		Location loc = getLocation(2, plugin.getLangManager().getNode("command-set-usage"), plugin.getLangManager().getNode("command-set-console-usage"));

		//TODO Set destination - check arg /met set destination <loc>
		if ("destination".equals(args[1]))
			plugin.saveDestination(loc);
		else if ("spawn".equals(args[1]))
			plugin.saveLocation(loc);
		else
			returnTell(plugin.getLangManager().getNode("command-set-usage"));
		tell(plugin.getLangManager().getNode("location-set"));
	}

}
