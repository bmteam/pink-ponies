/**
 * Copyright (c) 2013 Alexander Konovalov, Andrey Konovalov, Sergey Voronov, Vitaly Malyshev. All
 * rights reserved. Use of this source code is governed by a BSD-style license that can be found in
 * the LICENSE file.
 */

package ru.pinkponies.protocol;

import org.msgpack.annotation.Message;

@Message
public class QuestActionPacket extends Packet {
	public static final int JOIN = 0;
	public static final int START = 1;
	public static final int LEAVE = 2;

	public long questId;
	public int action;

	public QuestActionPacket() {
		super();
		this.questId = -1;
		this.action = -1;
	}

	public QuestActionPacket(final long questId, final int action) {
		super();
		this.questId = questId;
		this.action = action;
	}

	@Override
	public String toString() {
		return "QuestAction [action=" + this.action + ", questId=" + this.questId + "]";
	}
}
