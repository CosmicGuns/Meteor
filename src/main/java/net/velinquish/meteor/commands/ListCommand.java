package net.velinquish.meteor.commands;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import net.velinquish.meteor.Meteor;
import net.velinquish.utils.AnyCommand;
import net.velinquish.utils.Common;

public class ListCommand extends AnyCommand {

	private Meteor plugin = Meteor.getInstance();

	@Override
	protected void run(CommandSender sender, String[] args, boolean silent) {
		checkPermission(plugin.getPermission().replaceAll("%action%", "list"));
		List<Location> list = plugin.getLocationManager().getLocations();
		if (list == null)
			returnTell(plugin.getLangManager().getNode("no-locations"));

		tell(plugin.getLangManager().getNode("locations-list-heading"));
		for (Location loc : plugin.getLocationManager().getLocations())
			tell(plugin.getLangManager().getNode("locations-list-item").replace(Common.map(
					"%x%", "" + loc.getBlockX(),
					"%y%", "" + loc.getBlockY(),
					"%z%", "" + loc.getBlockZ(),
					"%world%", loc.getWorld().getName()
					)));
		tell(plugin.getLangManager().getNode("destinations-list-heading"));
		if (plugin.getLocationManager().getDestinations() != null)
			for (Location loc : plugin.getLocationManager().getDestinations())
				tell(plugin.getLangManager().getNode("locations-list-item").replace(Common.map(
						"%x%", "" + loc.getBlockX(),
						"%y%", "" + loc.getBlockY(),
						"%z%", "" + loc.getBlockZ(),
						"%world%", loc.getWorld().getName()
						)));
	}

}
