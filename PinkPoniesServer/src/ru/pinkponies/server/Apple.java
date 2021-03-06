/**
 * Copyright (c) 2013 Alexander Konovalov, Andrey Konovalov, Sergey Voronov, Vitaly Malyshev. All
 * rights reserved. Use of this source code is governed by a BSD-style license that can be found in
 * the LICENSE file.
 */

package ru.pinkponies.server;

import ru.pinkponies.protocol.Location;

public final class Apple extends Entity {
	/**
	 * The id of the quest to which apple relates.
	 */
	private final long questId;

	public Apple(final long id, final Location location, final long questId) {
		super(id, location);
		this.questId = questId;
	}

	@Override
	public String toString() {
		return "Apple [id=" + this.getId() + ", questId=" + this.questId + ", " + this.getLocation() + "]";
	}
}
