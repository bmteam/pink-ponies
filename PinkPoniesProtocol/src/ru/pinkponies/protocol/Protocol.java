package ru.pinkponies.protocol;

import java.io.IOException;
import java.io.InvalidClassException;

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
}
