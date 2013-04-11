package ru.pinkponies.server;

import ru.pinkponies.protocol.Location;

public class Entity {
	private final long id;
	private Location location;

	public Entity(final long id, final Location location) {
		this.id = id;
		this.location = location;
	}

	public long getId() {
		return this.id;
	}

	public Location getLocation() {
		return this.location;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Entity [id=" + this.id + ", " + this.location + "]";
	}
}
