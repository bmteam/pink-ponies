package ru.pinkponies.protocol;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class LocationUpdatePacket extends Packet {
	@Index(0)
	public String clientId;

	@Index(1)
	public Location location;

	public LocationUpdatePacket() {
		super();
		this.clientId = null;
		this.location = null;
	}

	public LocationUpdatePacket(final String clientId, final Location location) {
		super();
		this.clientId = clientId;
		this.location = location;
	}

	@Override
	public String toString() {
		return "LocationUpdate [clientID=" + this.clientId + ", " + this.location + "]";
	}
}
