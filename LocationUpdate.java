package ru.pinkponies.protocol;

public final class LocationUpdate {
	public static long PACKET_ID = 1;
	
	public float longitude;
	public float latitude;
	
	public LocationUpdate(float longitude, float latitude) {
		this.longitude = longitude;
		this.latitude = latitude;
	}
}
