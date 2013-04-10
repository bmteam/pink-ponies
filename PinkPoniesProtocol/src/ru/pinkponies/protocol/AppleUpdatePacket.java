package ru.pinkponies.protocol;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class AppleUpdatePacket extends Packet {
	@Index(0)
	public long appleId;

	@Index(1)
	public double altitude;

	@Index(2)
	public double latitude;

	@Index(3)
	public double longitude;

	/**
	 * The status of the apple. True means apple still exists, false means the apple disappeared.
	 */
	@Index(4)
	public boolean status;

	public AppleUpdatePacket() {
		super();
		this.appleId = -1;
		this.longitude = 0;
		this.latitude = 0;
		this.altitude = 0;
	}

	public AppleUpdatePacket(final long appleId, final double longitude, final double latitude, final double altitude,
			final boolean status) {
		super();
		this.appleId = appleId;
		this.longitude = longitude;
		this.latitude = latitude;
		this.altitude = altitude;
		this.status = status;
	}

	@Override
	public String toString() {
		return "AppleUpdate [appleId=" + this.appleId + ", longitude=" + this.longitude + ", latitude=" + this.latitude
				+ ", altitude=" + this.altitude + ", status=" + this.status + "]";
	}
}
