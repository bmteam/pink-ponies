package ru.pinkponies.protocol;

import org.msgpack.annotation.Message;

@Message
public class LocationUpdate extends Packet {
	public static long PACKET_ID = 1;
	
	public float longitude;
	public float latitude;
	public float height;

	public LocationUpdate() {
		this.longitude = 0;
		this.latitude = 0;
		this.height = 0;
	}
	
	public LocationUpdate(float longitude, float latitude, float height) {
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
