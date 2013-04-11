package ru.pinkponies.protocol;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class LoginPacket extends Packet {
	@Index(0)
	public long clientId;

	public LoginPacket() {
		super();
	}

	public LoginPacket(final long clientId) {
		super();
		this.clientId = clientId;
	}

	@Override
	public String toString() {
		return "Login [clientId=" + this.clientId + "]";
	}
}
