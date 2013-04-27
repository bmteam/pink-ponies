/**
 * Copyright (c) 2013 Alexander Konovalov, Andrey Konovalov, Sergey Voronov, Vitaly Malyshev. All
 * rights reserved. Use of this source code is governed by a BSD-style license that can be found in
 * the LICENSE file.
 */

package ru.pinkponies.server;

import ru.pinkponies.protocol.Location;

/**
 * The quest class.
 */
public final class Quest extends Entity {
	/**
	 * Creates a new quest with the given id and location.
	 * 
	 * @param id
	 *            the quest id
	 * @param location
	 *            the quest location
	 */
	public Quest(final long id, final Location location) {
		super(id, location);
	}

	@Override
	public String toString() {
		return "Quest [id=" + this.getId() + ", " + this.getLocation() + "]";
	}
}
