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
	/**
	 * LoginPacket id.
	 */
	private static final byte LOGIN_PACKET = 0;
	/**
	 * ClientOptionsPacket id.
	 */
	private static final byte CLIENT_OPTIONS_PACKET = 1;
	/**
	 * SayPacket id.
	 */
	private static final byte SAY_PACKET = 2;
	/**
	 * LocationUpdatePacket id.
	 */
	private static final byte LOCATION_UPDATE_PACKET = 3;
	/**
	 * AppleUpdatePacket id.
	 */
	private static final byte APPLE_UPDATE_PACKET = 4;

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
		this.messagePack.register(LocationUpdatePacket.class);
		this.messagePack.register(AppleUpdatePacket.class);
	}

	/**
	 * Packs packet into a byte array.
	 * 
	 * @param packet
	 *            the packet
	 * @return the serialized packet data
	 * @throws IOException
	 *             if there was some problem serializing packet.
	 */
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
		} else if (packet instanceof LocationUpdatePacket) {
			packer.write(LOCATION_UPDATE_PACKET);
			packer.write(packet);
		} else if (packet instanceof AppleUpdatePacket) {
			packer.write(APPLE_UPDATE_PACKET);
			packer.write(packet);
		} else {
			throw new InvalidClassException("Unknown packet type.");
		}

		return packer.toByteArray();
	}

	/**
	 * Packs packet into a byte buffer.
	 * 
	 * @param packet
	 *            the packet
	 * @param buffer
	 *            the buffer to which the packet is to be serialized.
	 * @throws IOException
	 *             if there was some problem serializing packet.
	 * @throws BufferOverflowException
	 *             if there was not enough space in the buffer to store the packed packet data.
	 */
	public void pack(final Packet packet, final ByteBuffer buffer) throws IOException, BufferOverflowException {
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
		} else if (packet instanceof LocationUpdatePacket) {
			packer.write(LOCATION_UPDATE_PACKET);
			packer.write(packet);
		} else if (packet instanceof AppleUpdatePacket) {
			packer.write(APPLE_UPDATE_PACKET);
			packer.write(packet);
		} else {
			throw new InvalidClassException("Unknown packet type.");
		}

		buffer.put(packer.toByteArray());
	}

	/**
	 * Unpacks a packet from the byte array.
	 * 
	 * @param data
	 *            the serialized packet data
	 * @return the packet
	 * @throws IOException
	 *             if there was some problem unpacking packet.
	 */
	public Packet unpack(final byte[] data) throws IOException {
		final BufferUnpacker unpacker = this.messagePack.createBufferUnpacker(data);

		final byte type = unpacker.readByte();

		if (type == LOGIN_PACKET) {
			return unpacker.read(LoginPacket.class);
		} else if (type == CLIENT_OPTIONS_PACKET) {
			return unpacker.read(ClientOptionsPacket.class);
		} else if (type == SAY_PACKET) {
			return unpacker.read(SayPacket.class);
		} else if (type == LOCATION_UPDATE_PACKET) {
			return unpacker.read(LocationUpdatePacket.class);
		} else if (type == APPLE_UPDATE_PACKET) {
			return unpacker.read(AppleUpdatePacket.class);
		} else {
			// FIXME(alexknvl): check if its the right type of exception
			throw new InvalidClassException("Unknown packet type.");
		}
	}

	/**
	 * Unpacks a packet from the byte buffer.
	 * 
	 * @param buffer
	 *            the buffer from which the packet should be unpacked
	 * @return the packet
	 * @throws IOException
	 *             if there was some problem unpacking packet.
	 */
	public Packet unpack(final ByteBuffer buffer) throws IOException {
		// FIXME(alexknvl): wtf, there is no way to read directly from
		// ByteBuffer?
		final BufferUnpacker unpacker = this.messagePack.createBufferUnpacker(buffer.array());

		final byte type = unpacker.readByte();
		Packet result = null;

		if (type == LOGIN_PACKET) {
			result = unpacker.read(LoginPacket.class);
		} else if (type == CLIENT_OPTIONS_PACKET) {
			result = unpacker.read(ClientOptionsPacket.class);
		} else if (type == SAY_PACKET) {
			result = unpacker.read(SayPacket.class);
		} else if (type == LOCATION_UPDATE_PACKET) {
			result = unpacker.read(LocationUpdatePacket.class);
		} else if (type == APPLE_UPDATE_PACKET) {
			result = unpacker.read(AppleUpdatePacket.class);
		} else {
			// FIXME(alexknvl): check if its the right type of exception
			throw new InvalidClassException("Unknown packet type.");
		}

		final int totalRead = unpacker.getReadByteCount();
		buffer.position(buffer.position() + totalRead);

		return result;
	}
}
