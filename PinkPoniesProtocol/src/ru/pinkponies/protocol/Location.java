/**
 * Copyright (c) 2013 Alexander Konovalov, Andrey Konovalov, Sergey Voronov, Vitaly Malyshev. All
 * rights reserved. Use of this source code is governed by a BSD-style license that can be found in
 * the LICENSE file.
 */

package ru.pinkponies.protocol;

/**
 * Location class.
 */
public final class Location {
	/**
	 * The average radius of the Earth in meters.
	 * 
	 * @see http://en.wikipedia.org/wiki/Earth
	 */
	private static final double EARTH_AVERAGE_RADIUS = 6371000;

	/**
	 * The longitude, measured in radians.
	 */
	private double longitude;
	/**
	 * The latitude, measured in radians.
	 */
	private double latitude;
	/**
	 * The altitude, measured in meters.
	 */
	private double altitude;

	/**
	 * Creates a new location, initializing longitude, latitude and altitude with zeroes.
	 */
	public Location() {
		super();
	}

	/**
	 * Creates a new location with the given longitude, latitude and altitude.
	 * 
	 * @param longitude
	 *            The longitude in degrees.
	 * @param latitude
	 *            The latitude in degrees.
	 * @param altitude
	 *            The altitude in meters.
	 */
	public Location(final double longitude, final double latitude, final double altitude) {
		super();
		this.longitude = longitude;
		this.latitude = latitude;
		this.altitude = altitude;
	}

	/**
	 * Finds the distance between this point and some other point on the globe.
	 * 
	 * @see http://www.movable-type.co.uk/scripts/latlong.html
	 * @param other
	 *            The location to which this method finds the distance.
	 * @return The distance between two points in meters.
	 */
	public double distanceTo(final Location other) {
		final double latitudeDifference = this.latitude - other.latitude;
		final double longitudeDifference = this.longitude - other.longitude;

		final double a = Math.sin(latitudeDifference / 2) * Math.sin(latitudeDifference / 2)
				+ Math.sin(longitudeDifference / 2) * Math.sin(longitudeDifference / 2) * Math.cos(this.latitude)
				* Math.cos(other.latitude);
		final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		final double d = EARTH_AVERAGE_RADIUS * c;

		return d;
	}

	/**
	 * Finds the altitude difference between this point and some other point on the globe.
	 * 
	 * @see http://www.movable-type.co.uk/scripts/latlong.html
	 * @param other
	 *            other location.
	 * @return the altitude difference between two points in meters.
	 */
	public double altitudeDifference(final Location other) {
		return this.altitude - other.altitude;
	}

	/**
	 * Finds the initial bearing (forward azimuth) to some other point.
	 * 
	 * @see http://www.movable-type.co.uk/scripts/latlong.html
	 * @param other
	 *            other location.
	 * @return the initial bearing (forward azimuth) to another location point.
	 */
	public double forwardAzimuthAngle(final Location other) {
		final double longitudeDifference = this.longitude - other.longitude;

		final double y = Math.sin(longitudeDifference) * Math.cos(other.latitude);
		final double x = Math.cos(this.latitude) * Math.sin(other.latitude) - Math.sin(this.latitude)
				* Math.cos(other.latitude) * Math.cos(longitudeDifference);
		return Math.atan2(y, x);
	}

	/**
	 * Returns the longitude.
	 * 
	 * @return the longitude
	 */
	public double getLongitude() {
		return this.longitude / Math.PI * 180;
	}

	/**
	 * Sets the longitude.
	 * 
	 * @param longitude
	 *            the longitude to set
	 */
	public void setLongitude(final double longitude) {
		this.longitude = longitude / 180 * Math.PI;
	}

	/**
	 * Returns the latitude.
	 * 
	 * @return the latitude
	 */
	public double getLatitude() {
		return this.latitude / Math.PI * 180;
	}

	/**
	 * Sets the latitude.
	 * 
	 * @param latitude
	 *            the latitude to set
	 */
	public void setLatitude(final double latitude) {
		this.latitude = latitude / 180 * Math.PI;
	}

	/**
	 * Returns the altitude.
	 * 
	 * @return the altitude
	 */
	public double getAltitude() {
		return this.altitude;
	}

	/**
	 * Sets the altitude.
	 * 
	 * @param altitude
	 *            the altitude to set
	 */
	public void setAltitude(final double altitude) {
		this.altitude = altitude;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Location [longitude=" + this.longitude + ", latitude=" + this.latitude + ", altitude=" + this.altitude
				+ "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(this.altitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.latitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.longitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		final Location other = (Location) obj;
		if (Double.doubleToLongBits(this.altitude) != Double.doubleToLongBits(other.altitude)) {
			return false;
		}
		if (Double.doubleToLongBits(this.latitude) != Double.doubleToLongBits(other.latitude)) {
			return false;
		}
		if (Double.doubleToLongBits(this.longitude) != Double.doubleToLongBits(other.longitude)) {
			return false;
		}
		return true;
	}

}
