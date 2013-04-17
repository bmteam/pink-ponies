/**
 * Copyright (c) 2013 Alexander Konovalov, Andrey Konovalov, Sergey Voronov, Vitaly Malyshev. All
 * rights reserved. Use of this source code is governed by a BSD-style license that can be found in
 * the LICENSE file.
 */

package ru.pinkponies.protocol;

import org.msgpack.annotation.Beans;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

/**
 * The location update packet class.
 */
@Message
@Beans
public final class LocationUpdatePacket extends Packet {
	/**
	 * The id of the client whose location is being updated.
	 */
	@Index(0)
	private String clientID;

	/**
	 * The altitude of the player.
	 */
	@Index(1)
	private double altitude;

	/**
	 * The latitude of the player.
	 */
	@Index(2)
	private double latitude;

	/**
	 * The longitude of the player.
	 */
	@Index(3)
	private double longitude;

	/**
	 * Creates a new empty location update packet with clientID set to empty string, longitude,
	 * latitude and altitude set to zero.
	 */
	public LocationUpdatePacket() {
		super();
		this.clientID = "";
		this.longitude = 0;
		this.latitude = 0;
		this.altitude = 0;
	}

	/**
	 * Creates a new location update packet with the given clientID, longitude, latitude and
	 * altitude.
	 * 
	 * @param clientID
	 *            the client id
	 * @param longitude
	 *            the longitude
	 * @param latitude
	 *            the latitude
	 * @param altitude
	 *            the altitude
	 */
	public LocationUpdatePacket(final String clientID, final double longitude, final double latitude,
			final double altitude) {
		super();
		this.clientID = clientID;
		this.longitude = longitude;
		this.latitude = latitude;
		this.altitude = altitude;
	}

	/**
	 * @return the clientID
	 */
	public String getClientID() {
		return this.clientID;
	}

	/**
	 * @param clientID
	 *            the clientID to set
	 */
	public void setClientID(final String clientID) {
		this.clientID = clientID;
	}

	/**
	 * @return the altitude
	 */
	public double getAltitude() {
		return this.altitude;
	}

	/**
	 * @param altitude
	 *            the altitude to set
	 */
	public void setAltitude(final double altitude) {
		this.altitude = altitude;
	}

	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return this.latitude;
	}

	/**
	 * @param latitude
	 *            the latitude to set
	 */
	public void setLatitude(final double latitude) {
		this.latitude = latitude;
	}

	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return this.longitude;
	}

	/**
	 * @param longitude
	 *            the longitude to set
	 */
	public void setLongitude(final double longitude) {
		this.longitude = longitude;
	}

	@Override
	public String toString() {
		return "LocationUpdate [clientID=" + this.clientID + ", longitude=" + this.longitude + ", latitude="
				+ this.latitude + ", altitude=" + this.altitude + "]";
	}
}
