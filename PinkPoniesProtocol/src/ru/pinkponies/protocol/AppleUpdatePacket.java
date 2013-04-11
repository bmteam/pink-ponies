package ru.pinkponies.protocol;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class AppleUpdatePacket extends Packet {
	@Index(0)
	public long appleId;

	@Index(1)
	public Location location;

	/**
	 * The status of the apple. True means apple still exists, false means the apple disappeared.
	 */
	@Index(2)
	public boolean status;

	public AppleUpdatePacket() {
		super();
		this.appleId = -1;
		this.location = null;
		this.status = false;
	}

	public AppleUpdatePacket(final long appleId, final Location location, final boolean status) {
		super();
		this.appleId = appleId;
		this.location = location;
		this.status = status;
	}

	@Override
	public String toString() {
		return "AppleUpdate [appleId=" + this.appleId + ", " + this.location + ", status=" + this.status + "]";
	}
}
