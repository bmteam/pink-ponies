package ru.pinkponies.protocol;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class ClientOptionsPacket extends Packet {
	@Index(0)
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
