/**
 * Copyright (c) 2013 Alexander Konovalov, Andrey Konovalov, Sergey Voronov, Vitaly Malyshev. All
 * rights reserved. Use of this source code is governed by a BSD-style license that can be found in
 * the LICENSE file.
 */

package ru.pinkponies.protocol;

import org.msgpack.annotation.Message;

@Message
public class ClientOptionsPacket extends Packet {
	public long clientId;

	public ClientOptionsPacket() {
		super();
	}

	public ClientOptionsPacket(final long clientId) {
		super();
		this.clientId = clientId;
	}

	@Override
	public String toString() {
		return "ClientOptions [clientId=" + this.clientId + "]";
	}
}
