package net.velinquish.meteor.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

import net.velinquish.meteor.Meteor;
import net.velinquish.utils.AnyCommand;

public class SpawnCommand extends AnyCommand {

	private Meteor plugin = Meteor.getInstance();

	@SuppressWarnings("deprecation")
	@Override
	protected void run(CommandSender sender, String[] args, boolean silent) { //TODO implement despawn
		checkPermission(plugin.getPermission());

		List<Location> list = plugin.getLocationManager().getLocations();
		List<Location> destinations = plugin.getLocationManager().getDestinations();

		if (list == null || list.isEmpty())
			returnTell(plugin.getLangManager().getNode("no-locations"));

		Location loc = list.get((int) (Math.random() * list.size())); //land location
		if (plugin.getFallHeight() != -1)
			loc.setY(plugin.getFallHeight());

		if (plugin.getMaxMets() != -1 && plugin.getNumTasks() >= plugin.getMaxMets())
			plugin.stopTask();

		FallingBlock block = loc.getWorld().spawnFallingBlock(loc, 213, (byte) 6);
		block.setDropItem(false);
		block.setCustomName("meteor");
		Vector direction = null;

		if (destinations != null && !destinations.isEmpty()) {
			int i = (int) (Math.random() * destinations.size());
			Meteor.debug("Using set destination " + (i + 1) + " out of " + destinations.size());
			Location desti = destinations.get(i);

			block.setGravity(false);

			direction = desti.toVector().subtract(loc.toVector());
			direction.normalize().multiply(plugin.getConfig().getDouble("speed"));

		} else if (plugin.isRandomAngle()) {
			block.setGravity(plugin.getConfig().getBoolean("gravity"));

			direction = new Vector(0.0D + Math.random() - Math.random(), - Math.random(), 0.0D + Math.random() - Math.random()).multiply(plugin.getConfig().getDouble("speed"));
		}
		block.setVelocity(direction);
		plugin.addTask(block, Bukkit.getScheduler().runTaskTimer(plugin, () -> {
			block.getWorld().playEffect(block.getLocation(), Effect.MOBSPAWNER_FLAMES, 2004);
			block.getWorld().playEffect(block.getLocation(), Effect.SMOKE, 2000, 4);
		}, plugin.getConfig().getInt("delay"), plugin.getConfig().getInt("frequency")));

		plugin.addCheck(block, Bukkit.getScheduler().runTaskTimer(plugin, () -> {
			if (!block.isValid()) {
				plugin.addBrokenMeteor(block, block.getLocation()); //TODO Remove the location part - it's unused, that was just for testing
				loc.getWorld().createExplosion(block.getLocation().getX(), block.getLocation().getY(), block.getLocation().getZ(), 4F, false, false);
			}
		}, 0, 1));

		//Auto-despawn - doesn't work because if it's replaced, it's not the same entity
		//		if (plugin.getConfig().getInt("despawn-time") > 0)
		//			Bukkit.getScheduler().runTaskLater(plugin, () -> {
		//				if (block.isValid())
		//					plugin.removeMeteor(block);
		//				else {
		//					Block metBlock = block.getLocation().getBlock();
		//					if (plugin.validBlock(metBlock))
		//						plugin.removeBlock(metBlock);
		//					else
		//						return;
		//				}
		//				String msg = plugin.getConfig().getString("despawned-meteor");
		//				if (msg != null && !msg.equals(""))
		//					Bukkit.broadcastMessage(msg);
		//			}, plugin.getConfig().getInt("despawn-time"));
		//
		tell(plugin.getLangManager().getNode("meteor-spawned"));
	}

}
