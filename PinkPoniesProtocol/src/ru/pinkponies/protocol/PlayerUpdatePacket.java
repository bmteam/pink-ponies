/**
 * Copyright (c) 2013 Alexander Konovalov, Andrey Konovalov, Sergey Voronov, Vitaly Malyshev. All
 * rights reserved. Use of this source code is governed by a BSD-style license that can be found in
 * the LICENSE file.
 */

package ru.pinkponies.protocol;

import org.msgpack.annotation.Message;

@Message
public class PlayerUpdatePacket extends Packet {
	public long playerId;
	public String playerName;
	public Location location;

	public PlayerUpdatePacket() {
		super();
		this.playerId = -1;
		this.location = new Location(0, 0, 0);
		this.playerName = "UNKNOWN";
	}

	public PlayerUpdatePacket(final long playerId, final String playerName, final Location location) {
		super();
		this.playerId = playerId;
		this.playerName = playerName;
		this.location = location;

	}

	@Override
	public String toString() {
		return "LocationUpdate [playerID=" + this.playerId + ", name=" + this.playerName + ", " + this.location + "]";
	}
}
