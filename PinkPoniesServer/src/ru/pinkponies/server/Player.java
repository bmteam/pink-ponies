/**
 * Copyright (c) 2013 Alexander Konovalov, Andrey Konovalov, Sergey Voronov, Vitaly Malyshev. All
 * rights reserved. Use of this source code is governed by a BSD-style license that can be found in
 * the LICENSE file.
 */

package ru.pinkponies.server;

import java.nio.channels.SocketChannel;

import ru.pinkponies.protocol.Location;

/**
 * The player class.
 */
public final class Player extends Entity {
	/**
	 * The socket channel associated with this player.
	 */
	private final SocketChannel channel;

	/**
	 * Creates a new player class with the given id, location and socket channel.
	 * 
	 * @param id
	 *            the player id
	 * @param location
	 *            the player location
	 * @param channel
	 *            the channel associated with this player
	 */
	public Player(final long id, final Location location, final SocketChannel channel) {
		super(id, location);
		this.channel = channel;
	}

	/**
	 * Returns the socket channel associated with this player.
	 * 
	 * @return the socket channel associated with this player.
	 */
	public SocketChannel getChannel() {
		return this.channel;
	}

	@Override
	public String toString() {
		return "Player [id=" + this.getId() + ", " + this.getLocation() + "]";
	}
}
