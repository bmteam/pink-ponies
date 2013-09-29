/**
 * Copyright (c) 2013 Alexander Konovalov, Andrey Konovalov, Sergey Voronov, Vitaly Malyshev. All
 * rights reserved. Use of this source code is governed by a BSD-style license that can be found in
 * the LICENSE file.
 */

package ru.pinkponies.server;

import java.util.HashMap;
import java.util.Map;

import ru.pinkponies.protocol.Location;

public final class Quest extends Entity {
	public enum Status {
		AVAILABLE, STARTED, FINISHED
	}

	private Status status;

	private final Map<Long, Apple> apples = new HashMap<Long, Apple>();

	private final Map<Long, Player> participants = new HashMap<Long, Player>();
	private final Map<Long, Player> potentialParticipants = new HashMap<Long, Player>();

	public Quest(final long id, final Location location) {
		super(id, location);
		this.status = Status.AVAILABLE;
	}

	public Status getStatus() {
		return this.status;
	}

	public Status setStatus(final Status status) {
		return this.status = status;
	}

	public void addApple(final Apple apple) {
		this.apples.put(apple.getId(), apple);
	}

	public void removeApple(final long appleId) {
		this.apples.remove(appleId);
	}

	public Map<Long, Apple> getApples() {
		return this.apples;
	}

	public void addParticipant(final Player player) {
		this.participants.put(player.getId(), player);
	}

	public void removeParticipant(final Player player) {
		this.participants.remove(player.getId());
	}

	public Map<Long, Player> getParticipants() {
		return this.participants;
	}

	public boolean isParticipant(final Player player) {
		return this.participants.get(player.getId()) != null;
	}

	public void addPotentialParticipant(final Player player) {
		this.potentialParticipants.put(player.getId(), player);
	}

	public void removePotentialParticipant(final Player player) {
		this.potentialParticipants.remove(player.getId());
	}

	public Map<Long, Player> getPotentialParticipants() {
		return this.potentialParticipants;
	}

	public boolean isPotentialParticipant(final Player player) {
		return this.potentialParticipants.get(player.getId()) != null;
	}

	@Override
	public String toString() {
		return "Quest [id=" + this.getId() + ", " + this.getLocation() + "]";
	}
}
