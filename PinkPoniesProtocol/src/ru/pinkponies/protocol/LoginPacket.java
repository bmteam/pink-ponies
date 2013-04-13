package ru.pinkponies.protocol;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class LoginPacket extends Packet {
	@Index(0)
	public String login;

	@Index(1)
	public String password;

	public LoginPacket() {
		super();
	}

	public LoginPacket(final String login, final String password) {
		super();
		this.login = login;
		this.password = password;
	}

	@Override
	public String toString() {
		return "Login [login=" + this.login + ", password=" + this.password + "]";
	}
}
