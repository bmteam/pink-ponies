package ru.pinkponies.server;

import ru.pinkponies.protocol.Location;

public final class Apple extends Entity {
	public Apple(final long id, final Location location) {
		super(id, location);
	}

	@Override
	public String toString() {
		return "Apple [id=" + this.getId() + ", " + this.getLocation() + "]";
	}
}
