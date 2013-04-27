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
 * The login packet class.
 */
@Message
@Beans
public class LoginPacket extends Packet {
	/**
	 * The login.
	 */
	@Index(0)
	private String login;

	/**
	 * The password.
	 */
	@Index(1)
	private String password;

	/**
	 * Creates a new empty login packet, with login and password set to an empty string.
	 */
	public LoginPacket() {
		super();
		this.login = "";
		this.password = "";
	}

	/**
	 * Creates a new login packet with the given login and password.
	 * 
	 * @param login
	 *            the login
	 * @param password
	 *            the password
	 */
	public LoginPacket(final String login, final String password) {
		super();
		this.login = login;
		this.password = password;
	}

	/**
	 * @return the login
	 */
	public String getLogin() {
		return this.login;
	}

	/**
	 * @param login
	 *            the login to set
	 */
	public void setLogin(final String login) {
		this.login = login;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return this.password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(final String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "Login [login=" + this.login + ", password=" + this.password + "]";
	}
}
