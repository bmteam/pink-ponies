/**
 * Copyright (c) 2013 Alexander Konovalov, Andrey Konovalov, Sergey Voronov, Vitaly Malyshev. All
 * rights reserved. Use of this source code is governed by a BSD-style license that can be found in
 * the LICENSE file.
 */

package ru.pinkponies.protocol;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * The apple update packet class.
 */
@Message
public class AppleUpdatePacket extends Packet {
	/**
	 * The id of the apple.
	 */
	@Index(0)
	private final long appleId;

	/**
	 * The location of the apple.
	 */
	@Index(1)
	private final Location location;

	/**
	 * The status of the apple. True means apple still exists, false means the apple disappeared.
	 */
	@Index(2)
	private boolean status;

	/**
	 * Creates a new apple update packet with apple id set to -1, location set to null and status
	 * set to false.
	 */
	public AppleUpdatePacket() {
		super();
		this.appleId = -1;
		this.location = null;
		this.status = false;
	}

	/**
	 * Creates a new apple update packet with the given apple id, location and status.
	 * 
	 * @param appleId
	 *            the apple id
	 * @param location
	 *            the apple location
	 * @param status
	 *            the status
	 */
	public AppleUpdatePacket(final long appleId, final Location location, final boolean status) {
		super();
		this.appleId = appleId;
		this.location = location;
		this.status = status;
	}

	/**
	 * @return the status
	 */
	public boolean getStatus() {
		return this.status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(final boolean status) {
		this.status = status;
	}

	/**
	 * @return the appleId
	 */
	public long getAppleId() {
		return this.appleId;
	}

	/**
	 * @return the location
	 */
	public Location getLocation() {
		return this.location;
	}

	@Override
	public String toString() {
		return "AppleUpdate [appleId=" + this.appleId + ", " + this.location + ", status=" + this.status + "]";
	}
}
