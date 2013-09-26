/**
 * Copyright (c) 2013 Alexander Konovalov, Andrey Konovalov, Sergey Voronov, Vitaly Malyshev. All
 * rights reserved. Use of this source code is governed by a BSD-style license that can be found in
 * the LICENSE file.
 */

package ru.pinkponies.protocol;

import org.msgpack.annotation.Message;

@Message
public class QuestUpdatePacket extends Packet {
	public static final int APPEARED = 0;
	public static final int DISAPPEARED = 1;
	public static final int AVAILABLE = 2;
	public static final int UNAVAILABLE = 3;
	public static final int ACCEPTED = 4;
	public static final int DECLINED = 5;
	public static final int STARTED = 6;
	public static final int FINISHED = 7;

	public long questId;
	public Location location;
	public int status;

	public QuestUpdatePacket() {
		super();
		this.questId = -1;
		this.location = null;
		this.status = -1;
	}

	public QuestUpdatePacket(final long questId, final Location location, final int status) {
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
