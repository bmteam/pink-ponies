/**
 * Copyright (c) 2013 Alexander Konovalov, Andrey Konovalov, Sergey Voronov, Vitaly Malyshev. All
 * rights reserved. Use of this source code is governed by a BSD-style license that can be found in
 * the LICENSE file.
 */

package ru.pinkponies.protocol;

import org.msgpack.annotation.Beans;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;
import org.msgpack.annotation.OrdinalEnum;

@Message
@Beans
public class QuestUpdatePacket extends Packet {
	@OrdinalEnum
	public enum Status {
		APPEARED, DISAPPEARED, AVAILABLE, UNAVAILABLE, ACCEPTED, DECLINED, STARTED, FINISHED
	}

	@Index(0)
	private long questId;

	@Index(1)
	private Location location;

	@Index(2)
	private Status status;

	public QuestUpdatePacket() {
		super();
		this.questId = -1;
		this.location = null;
		this.status = null;
	}

	public QuestUpdatePacket(final long questId, final Location location, final Status status) {
		super();
		this.questId = questId;
		this.location = location;
		this.status = status;
	}

	public Status getStatus() {
		return this.status;
	}

	public void setStatus(final Status status) {
		this.status = status;
	}

	public long getQuestId() {
		return this.questId;
	}

	public void setQuestId(final long questId) {
		this.questId = questId;
	}

	public Location getLocation() {
		return this.location;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	public String toString() {
		return "QuestUpdate [questId=" + this.questId + ", " + this.location + ", status=" + this.status + "]";
	}
}
