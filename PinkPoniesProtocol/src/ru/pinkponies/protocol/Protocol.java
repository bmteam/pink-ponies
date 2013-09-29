/**
 * Copyright (c) 2013 Alexander Konovalov, Andrey Konovalov, Sergey Voronov, Vitaly Malyshev. All
 * rights reserved. Use of this source code is governed by a BSD-style license that can be found in
 * the LICENSE file.
 */

package ru.pinkponies.protocol;

import java.io.IOException;
import java.io.InvalidClassException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import org.msgpack.MessagePack;
import org.msgpack.packer.BufferPacker;
import org.msgpack.unpacker.BufferUnpacker;

/**
 * This class provides methods related to packet packing / unpacking.
 */
public final class Protocol {
	private static final byte LOGIN_PACKET = 0;
	private static final byte CLIENT_OPTIONS_PACKET = 1;
	private static final byte SAY_PACKET = 2;
	private static final byte LOCATION_UPDATE_PACKET = 3;
	private static final byte APPLE_UPDATE_PACKET = 4;
	private static final byte QUEST_UPDATE_PACKET = 5;
	private static final byte QUEST_ACTION_PACKET = 7;

	/**
	 * The underlying serialization and deserialization API.
	 */
	private final MessagePack messagePack;

	/**
	 * Create a new protocol class. Registers packet classes, generating fast class-specific
	 * serializers / deserializers.
	 */
	public Protocol() {
		this.messagePack = new MessagePack();
		this.messagePack.register(LoginPacket.class);
		this.messagePack.register(ClientOptionsPacket.class);
		this.messagePack.register(SayPacket.class);
		this.messagePack.register(PlayerUpdatePacket.class);
		this.messagePack.register(AppleUpdatePacket.class);
		this.messagePack.register(QuestUpdatePacket.class);
		this.messagePack.register(QuestActionPacket.class);
	}

	/** Packs packet into a byte array. */
	public byte[] pack(final Packet packet) throws IOException {
		if (packet == null) {
			throw new NullPointerException("Packet can not be null.");
		}

		final BufferPacker packer = this.messagePack.createBufferPacker();

		if (packet instanceof LoginPacket) {
			packer.write(LOGIN_PACKET);
			packer.write(packet);
		} else if (packet instanceof ClientOptionsPacket) {
			packer.write(CLIENT_OPTIONS_PACKET);
			packer.write(packet);
		} else if (packet instanceof SayPacket) {
			packer.write(SAY_PACKET);
			packer.write(packet);
		} else if (packet instanceof PlayerUpdatePacket) {
			packer.write(LOCATION_UPDATE_PACKET);
			packer.write(packet);
		} else if (packet instanceof AppleUpdatePacket) {
			packer.write(APPLE_UPDATE_PACKET);
			packer.write(packet);
		} else if (packet instanceof QuestUpdatePacket) {
			packer.write(QUEST_UPDATE_PACKET);
			packer.write(packet);
		} else if (packet instanceof QuestActionPacket) {
			packer.write(QUEST_ACTION_PACKET);
			packer.write(packet);
		} else {
			throw new InvalidClassException("Unknown packet type.");
		}

		return packer.toByteArray();
	}

	/** Packs packet into a byte buffer. */
	public void pack(final Packet packet, final ByteBuffer buffer) throws IOException, BufferOverflowException {
		if (packet == null) {
			throw new NullPointerException("Packet can not be null.");
		}

		buffer.put(this.pack(packet));
	}

	/** Unpacks a packet from the byte array. */
	public Packet unpack(final BufferUnpacker unpacker) throws IOException {
		final byte type = unpacker.readByte();

		if (type == LOGIN_PACKET) {
			return unpacker.read(LoginPacket.class);
		} else if (type == CLIENT_OPTIONS_PACKET) {
			return unpacker.read(ClientOptionsPacket.class);
		} else if (type == SAY_PACKET) {
			return unpacker.read(SayPacket.class);
		} else if (type == LOCATION_UPDATE_PACKET) {
			return unpacker.read(PlayerUpdatePacket.class);
		} else if (type == APPLE_UPDATE_PACKET) {
			return unpacker.read(AppleUpdatePacket.class);
		} else if (type == QUEST_UPDATE_PACKET) {
			return unpacker.read(QuestUpdatePacket.class);
		} else if (type == QUEST_ACTION_PACKET) {
			return unpacker.read(QuestActionPacket.class);
		} else {
			// FIXME(alexknvl): check if its the right type of exception
			throw new InvalidClassException("Unknown packet type.");
		}
	}

	/** Unpacks a packet from the byte array. */
	public Packet unpack(final byte[] data) throws IOException {
		final BufferUnpacker unpacker = this.messagePack.createBufferUnpacker(data);
		final Packet packet = this.unpack(unpacker);
		return packet;
	}

	/** Unpacks a packet from the byte buffer. */
	public Packet unpack(final ByteBuffer buffer) throws IOException {
		final BufferUnpacker unpacker = this.messagePack.createBufferUnpacker(buffer);
		final Packet packet = this.unpack(unpacker);
		return packet;
	}
}
