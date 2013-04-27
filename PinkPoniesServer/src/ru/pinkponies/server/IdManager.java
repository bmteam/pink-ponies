/**
 * Copyright (c) 2013 Alexander Konovalov, Andrey Konovalov, Sergey Voronov, Vitaly Malyshev. All
 * rights reserved. Use of this source code is governed by a BSD-style license that can be found in
 * the LICENSE file.
 */

package ru.pinkponies.server;

/**
 * The id manager which provides 'unique' ids.
 */
public final class IdManager {
	/**
	 * The next unique id.
	 */
	private long nextUniqueId;

	/**
	 * Creates a new id manager.
	 */
	public IdManager() {
	}

	/**
	 * Returns a new unique id.
	 * 
	 * @return a new unique id.
	 */
	public long newId() {
		return this.nextUniqueId++;
	}
}
