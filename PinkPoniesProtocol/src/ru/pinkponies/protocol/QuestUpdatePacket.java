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
 * The quest update packet class.
 */
@Message
@Beans
public class QuestUpdatePacket extends Packet {
	/**
	 * The id of the quest.
	 */
	@Index(0)
	private long questId;

	/**
	 * The location of the quest.
	 */
	@Index(1)
	private Location location;

	/**
	 * The status of the quest. True means quest still exists, false means the quest disappeared.
	 */
	@Index(2)
	private boolean status;

	/**
	 * Creates a new quest update packet with quest id set to -1, location set to null and status
	 * set to false.
	 */
	public QuestUpdatePacket() {
		super();
		this.questId = -1;
		this.location = null;
		this.status = false;
	}

	/**
	 * Creates a new quest update packet with the given quest id, location and status.
	 * 
	 * @param questId
	 *            the quest id
	 * @param location
	 *            the quest location
	 * @param status
	 *            the status
	 */
	public QuestUpdatePacket(final long questId, final Location location, final boolean status) {
		super();
		this.questId = questId;
		this.location = location;
		this.status = status;
	}

	/**
	 * @return the status
	 */
	public boolean getStatus() {
		return this.status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(final boolean status) {
		this.status = status;
	}

	/**
	 * @return the questId
	 */
	public long getQuestId() {
		return this.questId;
	}

	/**
	 * @param questId
	 *            the questId to set
	 */
	public void setQuestId(final long questId) {
		this.questId = questId;
	}

	/**
	 * @return the location
	 */
	public Location getLocation() {
		return this.location;
	}

	/**
	 * @param location
	 *            the location to set
	 */
	public void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	public String toString() {
		return "QuestUpdate [questId=" + this.questId + ", " + this.location + ", status=" + this.status + "]";
	}
}
