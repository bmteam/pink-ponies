package ru.pinkponies.protocol;

import org.msgpack.annotation.Message;

@Message
public class LocationUpdatePacket extends Packet {	
	public double altitude;
	public double latitude;
	public double longitude;

	public LocationUpdatePacket() {
		super();
		this.longitude = 0;
		this.latitude = 0;
		this.altitude = 0;
	}
	
	public LocationUpdatePacket(double longitude, double latitude, double altitude) {
		super();
		this.longitude = longitude;
		this.latitude = latitude;
		this.altitude = altitude;
	}
	
	@Override
	public String toString() {
		return "LocationUpdate [longitude=" + longitude + ", latitude="
				+ latitude + ", altitude=" + altitude + "]";
	}
}
