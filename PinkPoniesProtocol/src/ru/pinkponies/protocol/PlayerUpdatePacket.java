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
	// The id of the client whose location is being updated.\
	@Index(0)
	public long clientId;

	// The client location.
	@Index(1)
	public Location location;

	// Creates a new empty location update packet with client id set to -1, longitude, latitude and
	// altitude set to zero.
	public PlayerUpdatePacket() {
		super();
		this.clientId = -1;
		this.location = new Location(0, 0, 0);
	}

	// Creates a new location update packet with the given client id and location.
	public PlayerUpdatePacket(final long clientId, final Location location) {
		super();
		this.clientId = clientId;
		this.location = location;
	}

	@Override
	public String toString() {
		return "LocationUpdate [clientID=" + this.clientId + ", " + this.location + "]";
	}
}
