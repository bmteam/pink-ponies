/**
 * Copyright (c) 2013 Alexander Konovalov, Andrey Konovalov, Sergey Voronov, Vitaly Malyshev. All
 * rights reserved. Use of this source code is governed by a BSD-style license that can be found in
 * the LICENSE file.
 */

package ru.pinkponies.protocol;

import org.msgpack.annotation.Message;

@Message
public class AppleUpdatePacket extends Packet {
	public long appleId;
	public Location location;
	public boolean status;

	public AppleUpdatePacket() {
		super();
		this.appleId = -1;
		this.location = null;
		this.status = false;
	}

	public AppleUpdatePacket(final long appleId, final Location location, final boolean status) {
		super();
		this.appleId = appleId;
		this.location = location;
		this.status = status;
	}

	@Override
	public String toString() {
		return "AppleUpdate [appleId=" + this.appleId + ", " + this.location + ", status=" + this.status + "]";
	}
}
