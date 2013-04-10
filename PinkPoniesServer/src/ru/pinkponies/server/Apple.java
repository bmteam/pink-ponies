package ru.pinkponies.server;

import ru.pinkponies.protocol.Location;

public final class Apple {
	private long id;
	private Location location;

	public Apple(final long id, final Location location) {
		this.id = id;
		this.location = location;
	}

	public long getID() {
		return id;
	}

	public void setID(final long id) {
		this.id = id;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}
}
