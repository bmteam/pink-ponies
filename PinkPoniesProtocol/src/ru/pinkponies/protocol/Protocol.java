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

	private MessagePack messagePack;

	public Protocol() {
		messagePack = new MessagePack();
		messagePack.register(LocationUpdatePacket.class);
		messagePack.register(LoginPacket.class);
		messagePack.register(SayPacket.class);
	}

	public byte[] pack(Packet packet) throws IOException {
		if (packet == null) {
			throw new NullPointerException("Packet can not be null.");
		}

		BufferPacker packer = messagePack.createBufferPacker();

		if (packet instanceof LoginPacket) {
			packer.write(LOGIN_PACKET);
			packer.write(packet);
		} else if (packet instanceof LocationUpdatePacket) {
			packer.write(LOCATION_UPDATE_PACKET);
			packer.write(packet);
		} else if (packet instanceof SayPacket) {
			packer.write(SAY_PACKET);
			packer.write(packet);
		} else {
			throw new InvalidClassException("Unknown packet type.");
		}

		return packer.toByteArray();
	}

	public void pack(Packet packet, ByteBuffer buffer) throws IOException,
			BufferOverflowException {
		if (packet == null) {
			throw new NullPointerException("Packet can not be null.");
		}

		BufferPacker packer = messagePack.createBufferPacker();

		if (packet instanceof LoginPacket) {
			packer.write(LOGIN_PACKET);
			packer.write(packet);
		} else if (packet instanceof LocationUpdatePacket) {
			packer.write(LOCATION_UPDATE_PACKET);
			packer.write(packet);
		} else if (packet instanceof SayPacket) {
			packer.write(SAY_PACKET);
			packer.write(packet);
		} else {
			throw new InvalidClassException("Unknown packet type.");
		}

		buffer.put(packer.toByteArray());
	}

	public Packet unpack(byte[] data) throws IOException {
		BufferUnpacker unpacker = messagePack.createBufferUnpacker(data);

		byte type = unpacker.readByte();

		if (type == LOGIN_PACKET) {
			return unpacker.read(LoginPacket.class);
		} else if (type == LOCATION_UPDATE_PACKET) {
			return unpacker.read(LocationUpdatePacket.class);
		} else if (type == SAY_PACKET) {
			return unpacker.read(SayPacket.class);
		} else {
			// FIXME(alexknvl): check if its the right type of exception
			throw new InvalidClassException("Unknown packet type.");
		}
	}

	public Packet unpack(ByteBuffer buffer) throws IOException {
		// FIXME(alexknvl): wtf, there is no way to read directly from
		// ByteBuffer?
		BufferUnpacker unpacker = messagePack.createBufferUnpacker(buffer
				.array());

		byte type = unpacker.readByte();
		Packet result = null;

		if (type == LOGIN_PACKET) {
			result = unpacker.read(LoginPacket.class);
		} else if (type == LOCATION_UPDATE_PACKET) {
			result = unpacker.read(LocationUpdatePacket.class);
		} else if (type == SAY_PACKET) {
			result = unpacker.read(SayPacket.class);
		} else {
			// FIXME(alexknvl): check if its the right type of exception
			throw new InvalidClassException("Unknown packet type.");
		}

		int totalRead = unpacker.getReadByteCount();
		buffer.position(buffer.position() + totalRead);

		return result;
	}
}
