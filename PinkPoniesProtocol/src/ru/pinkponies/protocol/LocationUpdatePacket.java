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
	
	public LocationUpdatePacket(String clientID, double longitude, double latitude, double altitude) {
		super();
		this.clientID = clientID;
		this.longitude = longitude;
		this.latitude = latitude;
		this.altitude = altitude;
	}
	
	@Override
	public String toString() {
		return "LocationUpdate [clientID=" + clientID + ", longitude=" + longitude +
				", latitude=" + latitude + ", altitude=" + altitude + "]";
	}
}
