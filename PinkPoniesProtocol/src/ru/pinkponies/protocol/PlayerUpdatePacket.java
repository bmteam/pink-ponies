/**
 * Copyright (c) 2013 Alexander Konovalov, Andrey Konovalov, Sergey Voronov, Vitaly Malyshev. All
 * rights reserved. Use of this source code is governed by a BSD-style license that can be found in
 * the LICENSE file.
 */

package ru.pinkponies.protocol;

import org.msgpack.annotation.Beans;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * The location update packet class.
 */
@Message
@Beans
public final class PlayerUpdatePacket extends Packet {
	/**
	 * The id of the client whose location is being updated.
	 */
	@Index(0)
	private long clientId;

	/**
	 * The client location.
	 */
	@Index(1)
	private Location location;

	/**
	 * Creates a new empty location update packet with client id set to -1, longitude, latitude and
	 * altitude set to zero.
	 */
	public PlayerUpdatePacket() {
		super();
		this.clientId = -1;
		this.setLocation(new Location());
	}

	/**
	 * Creates a new location update packet with the given client id and location.
	 * 
	 * @param clientId
	 *            the client id
	 * @param location
	 *            the location of the client
	 */
	public PlayerUpdatePacket(final long clientId, final Location location) {
		super();
		this.clientId = clientId;
		this.setLocation(location);
	}

	/**
	 * @return the client id
	 */
	public long getClientId() {
		return this.clientId;
	}

	/**
	 * @param clientId
	 *            the client id to set
	 */
	public void setClientId(final long clientId) {
		this.clientId = clientId;
	}

	@Override
	public String toString() {
		return "LocationUpdate [clientID=" + this.clientId + ", " + this.getLocation() + "]";
	}

	/**
	 * @return the location
	 */
	public Location getLocation() {
		return this.location;
	}

	/**
	 * @param location
	 *            the location to set
	 */
	public void setLocation(final Location location) {
		this.location = location;
	}
}
