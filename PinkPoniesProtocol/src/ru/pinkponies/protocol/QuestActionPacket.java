/**
 * Copyright (c) 2013 Alexander Konovalov, Andrey Konovalov, Sergey Voronov, Vitaly Malyshev. All
 * rights reserved. Use of this source code is governed by a BSD-style license that can be found in
 * the LICENSE file.
 */

package ru.pinkponies.protocol;

import org.msgpack.annotation.Message;
import org.msgpack.annotation.OrdinalEnum;

@Message
public class QuestActionPacket extends Packet {
	@OrdinalEnum
	public enum Action {
		JOIN, START, LEAVE
	}

	public long questId;
	public Action action;

	public QuestActionPacket() {
		super();
		this.questId = -1;
		this.action = null;
	}

	public QuestActionPacket(final long questId, final Action action) {
		super();
		this.questId = questId;
		this.action = action;
	}

	@Override
	public String toString() {
		return "QuestAction [action=" + this.action + ", questId=" + this.questId + "]";
	}
}
