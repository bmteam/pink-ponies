package ru.pinkponies.server;

import java.nio.channels.SocketChannel;

import ru.pinkponies.protocol.Location;

public final class Player extends Entity {
	private final SocketChannel channel;

	public Player(final long id, final Location location, final SocketChannel channel) {
		super(id, location);
		this.channel = channel;
	}

	public SocketChannel getChannel() {
		return this.channel;
	}

	@Override
	public String toString() {
		return "Player [id=" + this.getId() + ", " + this.getLocation() + "]";
	}
}
