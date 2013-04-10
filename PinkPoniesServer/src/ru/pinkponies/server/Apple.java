package ru.pinkponies.server;

import ru.pinkponies.protocol.Location;

public final class Apple {
	private String id;
	private Location location;

	public Apple(String id, Location location) {
		this.id = id;
		this.location = location;
	}

	public String getID() {
		return id;
	}

	public void setID(String id) {
		this.id = id;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

}
