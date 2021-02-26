package net.velinquish.meteor;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

import lombok.Getter;
import lombok.Setter;

public class LocationManager {
	@Setter
	@Getter
	private List<Location> locations;
	@Setter
	@Getter
	private List<Location> destinations;

	public LocationManager() {
		locations = new ArrayList<>();
	}

	public void addLocation(Location loc) {
		if (locations == null)
			locations = new ArrayList<>();
		locations.add(loc);
	}

	public void addDestination(Location loc) {
		if (destinations == null)
			destinations = new ArrayList<>();
		destinations.add(loc);
	}
}
