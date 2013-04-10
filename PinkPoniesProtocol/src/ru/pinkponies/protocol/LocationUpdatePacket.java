package ru.pinkponies.protocol;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class LocationUpdatePacket extends Packet {
	@Index(0)
	public String clientID;

	@Index(1)
	public double altitude;

	@Index(2)
	public double latitude;

	@Index(3)
	public double longitude;

	public LocationUpdatePacket() {
		super();
		this.clientID = "";
		this.longitude = 0;
		this.latitude = 0;
		this.altitude = 0;
	}

	public LocationUpdatePacket(final String clientID, final double longitude, final double latitude,
			final double altitude) {
		super();
		this.clientID = clientID;
		this.longitude = longitude;
		this.latitude = latitude;
		this.altitude = altitude;
	}

	@Override
	public String toString() {
		return "LocationUpdate [clientID=" + this.clientID + ", longitude=" + this.longitude + ", latitude="
				+ this.latitude + ", altitude=" + this.altitude + "]";
	}
}
