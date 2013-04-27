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
 * The client options packet class.
 */
@Message
@Beans
public class ClientOptionsPacket extends Packet {
	/**
	 * The client id.
	 */
	@Index(0)
	private long clientId;

	/**
	 * Creates a new empty client options packet with client id set to zero.
	 */
	public ClientOptionsPacket() {
		super();
	}

	/**
	 * Creates a new client options packet with the given client id.
	 * 
	 * @param clientId
	 *            the client id
	 */
	public ClientOptionsPacket(final long clientId) {
		super();
		this.clientId = clientId;
	}

	/**
	 * @return the clientId
	 */
	public long getClientId() {
		return this.clientId;
	}

	/**
	 * @param clientId
	 *            the clientId to set
	 */
	public void setClientId(final long clientId) {
		this.clientId = clientId;
	}

	@Override
	public String toString() {
		return "ClientOptions [clientId=" + this.clientId + "]";
	}
}
