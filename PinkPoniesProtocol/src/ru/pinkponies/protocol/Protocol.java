package ru.pinkponies.protocol;

import java.io.IOException;
import java.io.InvalidClassException;

import org.msgpack.MessagePack;
import org.msgpack.packer.BufferPacker;
import org.msgpack.unpacker.BufferUnpacker;

public final class Protocol {
	private static final byte LOGIN_PACKET = 0;
	private static final byte LOCATION_UPDATE_PACKET = 1;
	
	private MessagePack messagePack;
	
	public Protocol() {
		messagePack = new MessagePack();
		messagePack.register(LocationUpdate.class);
		messagePack.register(Login.class);
	}
	
	public byte[] pack(Packet packet) throws IOException {
		if (packet == null) {
			throw new NullPointerException("Packet can not be null.");
		}
		
		BufferPacker packer = messagePack.createBufferPacker();
		
		if (packet instanceof Login) {
			packer.write(LOGIN_PACKET);
			packer.write((Login) packet);
		} else if (packet instanceof LocationUpdate) {
			packer.write(LOCATION_UPDATE_PACKET);
			packer.write((Login) packet);
		} else {
			throw new InvalidClassException("Unknown packet type.");
		}
		
		return packer.toByteArray();
	}
	
	public Packet unpack(byte[] data) throws IOException {
		BufferUnpacker unpacker = messagePack.createBufferUnpacker(data);
		
		byte type = unpacker.readByte();
		
		if (type == LOGIN_PACKET) {
			return unpacker.read(Login.class);
		} else if (type == LOCATION_UPDATE_PACKET) {
			return unpacker.read(LocationUpdate.class);
		} else {
			// FIXME(alexknvl): check if its the right type of exception
			throw new InvalidClassException("Unknown packet type.");
		}
	}
}
