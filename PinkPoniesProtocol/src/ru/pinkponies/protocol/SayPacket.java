/**
 * 
 */
package ru.pinkponies.protocol;

import org.msgpack.annotation.Message;

/**
 * @author alex
 * 
 */
@Message
public final class SayPacket extends Packet {
	public String text;

	public SayPacket() {
		super();
		this.text = "";
	}

	public SayPacket(String text) {
		super();
		this.text = text;
	}

	@Override
	public String toString() {
		return "SayPacket [text=" + text + "]";
	}
}
