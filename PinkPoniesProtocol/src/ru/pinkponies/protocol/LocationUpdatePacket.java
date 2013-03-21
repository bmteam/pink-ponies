package ru.pinkponies.protocol;

import org.msgpack.annotation.Message;

@Message
public class LocationUpdatePacket extends Packet {	
	public float longitude;
	public float latitude;
	public float height;

	public LocationUpdatePacket() {
		super();
		this.longitude = 0;
		this.latitude = 0;
		this.height = 0;
	}
	
	public LocationUpdatePacket(float longitude, float latitude, float height) {
		super();
		this.longitude = longitude;
		this.latitude = latitude;
		this.height = height;
	}
	
	@Override
	public String toString() {
		return "LocationUpdate [longitude=" + longitude + ", latitude="
				+ latitude + ", height=" + height + "]";
	}
}
