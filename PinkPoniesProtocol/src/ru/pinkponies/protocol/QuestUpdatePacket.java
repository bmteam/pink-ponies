/**
 * Copyright (c) 2013 Alexander Konovalov, Andrey Konovalov, Sergey Voronov, Vitaly Malyshev. All
 * rights reserved. Use of this source code is governed by a BSD-style license that can be found in
 * the LICENSE file.
 */

package ru.pinkponies.protocol;

import org.msgpack.annotation.Message;
import org.msgpack.annotation.OrdinalEnum;

@Message
public class QuestUpdatePacket extends Packet {
	@OrdinalEnum
	public enum Status {
		APPEARED, DISAPPEARED, AVAILABLE, UNAVAILABLE, ACCEPTED, DECLINED, STARTED, FINISHED
	}

	public long questId;
	public Location location;
	public Status status;

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

	@Override
	public String toString() {
		return "QuestUpdate [questId=" + this.questId + ", " + this.location + ", status=" + this.status + "]";
	}
}
