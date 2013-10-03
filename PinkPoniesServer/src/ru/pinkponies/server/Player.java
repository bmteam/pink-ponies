/**
 * Copyright (c) 2013 Alexander Konovalov, Andrey Konovalov, Sergey Voronov, Vitaly Malyshev. All
 * rights reserved. Use of this source code is governed by a BSD-style license that can be found in
 * the LICENSE file.
 */

package ru.pinkponies.server;

import java.nio.channels.SocketChannel;

import ru.pinkponies.protocol.Location;

public final class Player extends Entity {
	private final SocketChannel channel;
	private Quest quest;
	private String name;

	public Player(final long id, final Location location, final SocketChannel channel) {
		super(id, location);
		this.channel = channel;
		this.quest = null;
		this.name = "DEFAULT TEST NAME";
	}

	public void setName(final String playerName) {
		this.name = playerName;
	}

	public String getName() {
		return this.name;
	}

	public boolean isNameChanged() {
		return this.name != "DEFAULT TEST NAME";
	}

	public SocketChannel getChannel() {
		return this.channel;
	}

	public Quest getQuest() {
		return this.quest;
	}

	public void setQuest(final Quest quest) {
		this.quest = quest;
	}

	@Override
	public String toString() {
		return "Player [id=" + this.getId() + ", " + this.getLocation() + "]";
	}
}
