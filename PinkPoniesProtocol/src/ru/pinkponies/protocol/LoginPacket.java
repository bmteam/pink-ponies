/**
 * Copyright (c) 2013 Alexander Konovalov, Andrey Konovalov, Sergey Voronov, Vitaly Malyshev. All
 * rights reserved. Use of this source code is governed by a BSD-style license that can be found in
 * the LICENSE file.
 */

package ru.pinkponies.protocol;

import org.msgpack.annotation.Message;

@Message
public class LoginPacket extends Packet {
	public String login;
	public String password;

	public LoginPacket() {
		super();
		this.login = "";
		this.password = "";
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
