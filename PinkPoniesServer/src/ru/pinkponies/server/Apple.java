/**
 * Copyright (c) 2013 Alexander Konovalov, Andrey Konovalov, Sergey Voronov, Vitaly Malyshev. All
 * rights reserved. Use of this source code is governed by a BSD-style license that can be found in
 * the LICENSE file.
 */

package ru.pinkponies.server;

import ru.pinkponies.protocol.Location;

/**
 * The apple class.
 */
public final class Apple extends Entity {
	/**
	 * Creates a new apple with the given id and location.
	 * 
	 * @param id
	 *            the apple id
	 * @param location
	 *            the apple location
	 */
	public Apple(final long id, final Location location) {
		super(id, location);
	}

	@Override
	public String toString() {
		return "Apple [id=" + this.getId() + ", " + this.getLocation() + "]";
	}
}
