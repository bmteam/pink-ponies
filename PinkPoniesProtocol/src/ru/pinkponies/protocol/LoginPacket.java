package ru.pinkponies.protocol;

import org.msgpack.annotation.Message;

@Message
public class LoginPacket extends Packet {
	public String clientID;

	public LoginPacket() {
		super();
	}

	public LoginPacket(String clientID) {
		super();
		this.clientID = clientID;
	}

	@Override
	public String toString() {
		return "Login [clientID=" + clientID + "]";
	}
}
