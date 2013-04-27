/**
 * Copyright (c) 2013 Alexander Konovalov, Andrey Konovalov, Sergey Voronov, Vitaly Malyshev. All
 * rights reserved. Use of this source code is governed by a BSD-style license that can be found in
 * the LICENSE file.
 */

package ru.pinkponies.server;

import ru.pinkponies.protocol.Location;

/**
 * The entity class.
 */
public class Entity {
	/**
	 * The id of the entity.
	 */
	private final long id;

	/**
	 * The location of the entity.
	 */
	private Location location;

	/**
	 * Creates a new entity with the given id and location.
	 * 
	 * @param id
	 *            the entity id
	 * @param location
	 *            the entity location
	 */
	public Entity(final long id, final Location location) {
		this.id = id;
		this.location = location;
	}

	/**
	 * Returns the id of this entity.
	 * 
	 * @return the id of this entity
	 */
	public final long getId() {
		return this.id;
	}

	/**
	 * Returns this entity's location.
	 * 
	 * @return this entity's location.
	 */
	public final Location getLocation() {
		return this.location;
	}

	/**
	 * Sets this entity's location.
	 * 
	 * @param location
	 *            the location
	 */
	public final void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	public String toString() {
		return "Entity [id=" + this.id + ", " + this.location + "]";
	}
}
