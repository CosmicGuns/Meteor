package net.velinquish.meteor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import lombok.Getter;
import lombok.Setter;
import net.velinquish.utils.Common;
import net.velinquish.utils.NBTEditor;
import net.velinquish.utils.VelinquishPlugin;
import net.velinquish.utils.lang.LangManager;

public class Meteor extends JavaPlugin implements Listener, VelinquishPlugin {

	@Getter
	private static Meteor instance;
	@Getter
	private String prefix;
	@Getter
	private String permission;
	private static boolean debug;
	@Getter
	private int maxMets;
	@Getter
	private int fallHeight;
	@Getter
	private String claimedMet;
	@Getter
	private boolean randomAngle;

	@Getter
	private YamlConfiguration config;
	private File configFile;

	@Getter
	private YamlConfiguration lang;
	private File langFile;

	@Getter
	private YamlConfiguration locations;
	private File locationsFile;

	@Getter
	private LangManager langManager;
	@Getter
	private LocationManager locationManager;

	private YamlConfiguration itemsConfig;
	private File itemsFile;

	@Setter
	@Getter
	private ItemStack metReward;

	private Map<Entity, BukkitTask> runningTasks = new LinkedHashMap<>();
	private Map<Block, BukkitTask> runningTasks2 = new LinkedHashMap<>();

	private Map<Entity, BukkitTask> checks = new HashMap<>();

	@Override
	public void onEnable() {
		instance = this;
		Common.setInstance(this);

		langManager = new LangManager();

		runningTasks = new LinkedHashMap<>();
		runningTasks2 = new LinkedHashMap<>();

		checks = new HashMap<>();

		try {
			loadFiles();
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}

		getServer().getPluginManager().registerEvents(this, this);
		Common.registerCommand(new CommandManager(getConfig().getString("main-command")));
	}

	@Override
	public void onDisable() {
		for (Block block : runningTasks2.keySet())
			block.setType(Material.AIR);
		instance = null;
	}

	public int getNumTasks() { return runningTasks.size() + runningTasks2.size(); }

	//Called when someone gets the meteor (as entity) or the meteor despawns
	public void removeMeteor(Entity e) {
		runningTasks.get(e).cancel();
		runningTasks.remove(e);
		if (checks.containsKey(e)) {
			checks.get(e).cancel();
			checks.remove(e);
		}
		checks.remove(e);
		e.remove();
	}

	//Called when the meteor breaks (on a slab or something) and a new one needs to be spawned
	public void addBrokenMeteor(Entity entity, Location loc) {
		checks.get(entity).cancel();

		debug("The meteor broke! Spawning a falling block in a safer place");

		@SuppressWarnings("deprecation")
		FallingBlock block = loc.getWorld().spawnFallingBlock(loc, 213, (byte) 6);
		//TODO Find a way to make the meteor persistent so it's not unloaded when players are far away
		block.setGravity(false);
		block.setVelocity(new Vector(0,0,0));
		block.setDropItem(false);
		block.setCustomName("meteor");
		NBTEditor.set(block, -2147483648, "Time");
		debug("Spawn location: " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ());

		BukkitTask task = runningTasks.get(entity);
		runningTasks.remove(entity);
		runningTasks.put(block, task);

		//Disabled because persistent spawning often crashes.
		//It sometimes breaks even when gravity is set to false and there's a vector
		//addCheck(block, Bukkit.getScheduler().runTaskTimer(this, () -> {
		//	if (!block.isValid())
		//		addBrokenMeteor(block, entity.getLocation()); //Either "loc" or "entity.getLocation()" - each one has its ups and downs
		//	else
		//		block.setVelocity(new Vector(0,0,0));
		//	//				addBrokenMeteor(block, block.getLocation()); //Bufix - Spawning replacement block high up after second call of this can spawn way higher than original location
		//}, 0, 1));
	}

	public boolean validBlock(Block block) { return runningTasks2.containsKey(block); }
	public void removeBlock(Block block) {
		block.setType(Material.AIR);

		runningTasks2.get(block).cancel();
		runningTasks2.remove(block);
	}

	public void addTask(FallingBlock block, BukkitTask task) {
		runningTasks.put(block, task);
	}

	public void addCheck(FallingBlock block, BukkitTask task) {
		checks.put(block, task);
	}

	//Stops the first task - called when the max meteor limit has been reached
	public void stopTask() {
		if (runningTasks2.isEmpty()) {
			Entity e = runningTasks.entrySet().iterator().next().getKey();
			removeMeteor(e);
			return;
		}

		Block b =  runningTasks2.entrySet().iterator().next().getKey();
		runningTasks2.get(b).cancel();
		b.setType(Material.AIR);
		runningTasks2.remove(b);
	}

	public void loadFiles() throws IOException, InvalidConfigurationException {
		configFile = new File(getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			configFile.getParentFile().mkdirs();
			saveResource("config.yml", false);
		}
		config = new YamlConfiguration();
		config.load(configFile);

		prefix = getConfig().getString("plugin-prefix");
		debug = getConfig().getBoolean("debug");
		permission = getConfig().getString("permission");
		randomAngle = getConfig().getBoolean("random-angle");
		maxMets = getConfig().getInt("max-meteors");
		fallHeight = getConfig().getInt("fall-height");
		if (getConfig().getBoolean("announcement-enabled"))
			claimedMet = getConfig().getString("claimed-meteor");

		langFile = new File(getDataFolder(), "lang.yml");
		if (!langFile.exists()) {
			langFile.getParentFile().mkdirs();
			saveResource("lang.yml", false);
		}
		lang = new YamlConfiguration();
		lang.load(langFile);

		langManager.clear();
		langManager.setPrefix(prefix);
		langManager.loadLang(lang);

		locationManager = new LocationManager();
		locationsFile = new File(getDataFolder(), "locations.yml");
		if (!locationsFile.exists()) {
			locationsFile.getParentFile().mkdirs();
			saveResource("locations.yml", false);
		}
		locations = new YamlConfiguration();
		locations.load(locationsFile);
		loadLocations();

		itemsFile = new File(getDataFolder(), "items.yml");
		if (!itemsFile.exists()) {
			itemsFile.getParentFile().mkdirs();
			saveResource("items.yml", false);
		}
		itemsConfig = new YamlConfiguration();
		itemsConfig.load(itemsFile);

		loadItems();
	}

	public void loadItems() {
		metReward = itemsConfig.getItemStack("meteor-reward");
	}

	public void saveItems(ItemStack item, String as) {
		itemsConfig.set(as, item);
		try {
			itemsConfig.save(itemsFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@EventHandler
	public void onRightClickMetEntity(PlayerInteractEntityEvent e) {
		if (e.getRightClicked().getType().equals(EntityType.FALLING_BLOCK) && runningTasks.containsKey(e.getRightClicked())) {

			for (ItemStack toDrop : e.getPlayer().getInventory().addItem(metReward).values())
				e.getPlayer().getWorld().dropItem(e.getPlayer().getLocation(), toDrop);

			removeMeteor(e.getRightClicked());

			if (claimedMet != null)
				Bukkit.getServer().broadcastMessage(Common.colorize(claimedMet.replaceAll("%player%", e.getPlayer().getName()).replaceAll("%prefix%", prefix)));
		}
	}

	@EventHandler
	public void onRightClickMeteor(PlayerInteractEvent e) {
		if (e.getClickedBlock() != null &&
				e.getClickedBlock().getType().equals(Material.MAGMA) &&
				runningTasks2.containsKey(e.getClickedBlock())) {

			removeBlock(e.getClickedBlock());

			for (ItemStack toDrop : e.getPlayer().getInventory().addItem(metReward).values())
				e.getPlayer().getWorld().dropItem(e.getPlayer().getLocation(), toDrop); //TODO Make reward method for these three methods

			if (claimedMet != null)
				Bukkit.getServer().broadcastMessage(Common.colorize(claimedMet.replaceAll("%player%", e.getPlayer().getName()).replaceAll("%prefix%", prefix)));
		}
	}

	// If "Time" NBT is set to -1, it creates the explosion in mid-air and sends the block flying up.
	@EventHandler
	public void onMeteorLand(EntityChangeBlockEvent e) {
		if (e.getEntityType() != EntityType.FALLING_BLOCK || !runningTasks.containsKey(e.getEntity()))
			return;

		checks.get(e.getEntity()).cancel();
		checks.remove(e.getEntity());

		Location loc = e.getBlock().getLocation();
		loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 4F, false, false);
		runningTasks2.put(e.getBlock(), runningTasks.get(e.getEntity()));
		runningTasks.remove(e.getEntity());
	}

	@SuppressWarnings("unchecked")
	public void loadLocations() {
		locationManager.setLocations((List<Location>) locations.getList("Locations"));
		locationManager.setDestinations((List<Location>) locations.getList("Destinations"));
	}

	//Write loc to file
	public void saveLocation(Location loc) {
		locationManager.addLocation(loc);
		List<Location> list = locationManager.getLocations();
		locations.set("Locations", list);
		try {
			locations.save(locationsFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveDestination(Location loc) {
		locationManager.addDestination(loc);
		List<Location> list = locationManager.getDestinations();
		locations.set("Destinations", list);
		try {
			locations.save(locationsFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void debug(String message) {
		if (debug == true)
			Common.log(message);
	}
}
