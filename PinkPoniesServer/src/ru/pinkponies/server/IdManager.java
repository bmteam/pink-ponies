package ru.pinkponies.server;

public final class IdManager {
	private long maxId;

	public IdManager() {
		this.maxId = 0;
	}

	public final long newId() {
		return this.maxId++;
	}
}
