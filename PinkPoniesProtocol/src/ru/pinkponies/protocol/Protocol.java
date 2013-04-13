package ru.pinkponies.protocol;

import java.io.IOException;
import java.io.InvalidClassException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

import org.msgpack.MessagePack;
import org.msgpack.packer.BufferPacker;
import org.msgpack.unpacker.BufferUnpacker;

public final class Protocol {
	private static final byte LOGIN_PACKET = 0;
	private static final byte LOCATION_UPDATE_PACKET = 1;
	private static final byte SAY_PACKET = 2;
	private static final byte APPLE_UPDATE_PACKET = 3;
	private static final byte CLIENT_OPTIONS_PACKET = 4;

	private final MessagePack messagePack;

	public Protocol() {
		this.messagePack = new MessagePack();
		this.messagePack.register(LocationUpdatePacket.class);
		this.messagePack.register(LoginPacket.class);
		this.messagePack.register(SayPacket.class);
		this.messagePack.register(AppleUpdatePacket.class);
		this.messagePack.register(ClientOptionsPacket.class);
	}

	public byte[] pack(final Packet packet) throws IOException {
		if (packet == null) {
			throw new NullPointerException("Packet can not be null.");
		}

		BufferPacker packer = this.messagePack.createBufferPacker();

		if (packet instanceof LoginPacket) {
			packer.write(LOGIN_PACKET);
			packer.write(packet);
		} else if (packet instanceof LocationUpdatePacket) {
			packer.write(LOCATION_UPDATE_PACKET);
			packer.write(packet);
		} else if (packet instanceof SayPacket) {
			packer.write(SAY_PACKET);
			packer.write(packet);
		} else if (packet instanceof AppleUpdatePacket) {
			packer.write(APPLE_UPDATE_PACKET);
			packer.write(packet);
		} else if (packet instanceof ClientOptionsPacket) {
			packer.write(CLIENT_OPTIONS_PACKET);
			packer.write(packet);
		} else {
			throw new InvalidClassException("Unknown packet type.");
		}

		return packer.toByteArray();
	}

	public void pack(final Packet packet, final ByteBuffer buffer) throws IOException, BufferOverflowException {
		if (packet == null) {
			throw new NullPointerException("Packet can not be null.");
		}

		BufferPacker packer = this.messagePack.createBufferPacker();

		if (packet instanceof LoginPacket) {
			packer.write(LOGIN_PACKET);
			packer.write(packet);
		} else if (packet instanceof LocationUpdatePacket) {
			packer.write(LOCATION_UPDATE_PACKET);
			packer.write(packet);
		} else if (packet instanceof SayPacket) {
			packer.write(SAY_PACKET);
			packer.write(packet);
		} else if (packet instanceof AppleUpdatePacket) {
			packer.write(APPLE_UPDATE_PACKET);
			packer.write(packet);
		} else if (packet instanceof ClientOptionsPacket) {
			packer.write(CLIENT_OPTIONS_PACKET);
			packer.write(packet);
		} else {
			throw new InvalidClassException("Unknown packet type.");
		}

		buffer.put(packer.toByteArray());
	}

	public Packet unpack(final byte[] data) throws IOException {
		BufferUnpacker unpacker = this.messagePack.createBufferUnpacker(data);

		byte type = unpacker.readByte();

		if (type == LOGIN_PACKET) {
			return unpacker.read(LoginPacket.class);
		} else if (type == LOCATION_UPDATE_PACKET) {
			return unpacker.read(LocationUpdatePacket.class);
		} else if (type == SAY_PACKET) {
			return unpacker.read(SayPacket.class);
		} else if (type == APPLE_UPDATE_PACKET) {
			return unpacker.read(AppleUpdatePacket.class);
		} else if (type == CLIENT_OPTIONS_PACKET) {
			return unpacker.read(ClientOptionsPacket.class);
		} else {
			// FIXME(alexknvl): check if its the right type of exception
			throw new InvalidClassException("Unknown packet type.");
		}
	}

	public Packet unpack(final ByteBuffer buffer) throws IOException {
		// FIXME(alexknvl): wtf, there is no way to read directly from
		// ByteBuffer?
		BufferUnpacker unpacker = this.messagePack.createBufferUnpacker(buffer.array());

		byte type = unpacker.readByte();
		Packet result = null;

		if (type == LOGIN_PACKET) {
			result = unpacker.read(LoginPacket.class);
		} else if (type == LOCATION_UPDATE_PACKET) {
			result = unpacker.read(LocationUpdatePacket.class);
		} else if (type == SAY_PACKET) {
			result = unpacker.read(SayPacket.class);
		} else if (type == APPLE_UPDATE_PACKET) {
			result = unpacker.read(AppleUpdatePacket.class);
		} else if (type == CLIENT_OPTIONS_PACKET) {
			result = unpacker.read(ClientOptionsPacket.class);
		} else {
			// FIXME(alexknvl): check if its the right type of exception
			throw new InvalidClassException("Unknown packet type.");
		}

		int totalRead = unpacker.getReadByteCount();
		buffer.position(buffer.position() + totalRead);

		return result;
	}
}
