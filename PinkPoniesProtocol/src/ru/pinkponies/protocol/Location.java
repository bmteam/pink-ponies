/**
 * Copyright (c) 2013 Alexander Konovalov, Andrey Konovalov, Sergey Voronov, Vitaly Malyshev. All
 * rights reserved. Use of this source code is governed by a BSD-style license that can be found in
 * the LICENSE file.
 */

package ru.pinkponies.protocol;

import java.util.Random;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

// Location class.
// @see http://www.movable-type.co.uk/scripts/latlong.html
@Message
public final class Location {
	// The average radius of the Earth in meters.
	private static final double EARTH_AVERAGE_RADIUS = 6371000;

	// The longitude, measured in radians.
	@Index(0)
	public double longitude;

	// The latitude, measured in radians.
	@Index(1)
	public double latitude;

	// The altitude, measured in meters.
	@Index(2)
	public double altitude;

	public Location() {
		this.longitude = 0;
		this.latitude = 0;
		this.altitude = 0;
	}

	public Location(final double longitude, final double latitude, final double altitude) {
		this.longitude = longitude;
		this.latitude = latitude;
		this.altitude = altitude;
	}

	// Finds the distance between this point and some other point on the globe.
	public double distanceTo(final Location that) {
		double latitudeDifference = this.latitude - that.latitude;
		double longitudeDifference = this.longitude - that.longitude;

		double a = Math.sin(latitudeDifference / 2) * Math.sin(latitudeDifference / 2)
				+ Math.sin(longitudeDifference / 2) * Math.sin(longitudeDifference / 2) * Math.cos(this.latitude)
				* Math.cos(that.latitude);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double d = EARTH_AVERAGE_RADIUS * c;

		return d;
	}

	// Finds the initial bearing (forward azimuth) to some other point.
	public double bearingTo(final Location that) {
		double longitudeDifference = this.longitude - that.longitude;

		double y = Math.sin(longitudeDifference) * Math.cos(that.latitude);
		double x = Math.cos(this.latitude) * Math.sin(that.latitude) - Math.sin(this.latitude)
				* Math.cos(that.latitude) * Math.cos(longitudeDifference);
		return Math.atan2(y, x);
	}

	// Calculate a point that is the specified distance and bearing away from this point.
	public Location moveBy(final double distanceInMeters, final double bearingInRadians) {
		// Convert distance to angular distance.
		double distance = distanceInMeters / EARTH_AVERAGE_RADIUS;

		double newLatitude = Math.asin(Math.sin(this.latitude) * Math.cos(distance) + Math.cos(this.latitude)
				* Math.sin(distance) * Math.cos(bearingInRadians));
		double newLongitude = this.longitude
				+ Math.atan2(Math.sin(bearingInRadians) * Math.sin(distance) * Math.cos(this.latitude),
						Math.cos(distance) - Math.sin(this.latitude) * Math.sin(newLatitude));
		newLongitude = (newLongitude + 3 * Math.PI) % (2 * Math.PI) - Math.PI; // normalise to
																				// -180..+180

		return new Location(newLongitude, newLatitude, this.altitude);
	}

	public Location randomLocationInCircle(final Random random, final double radiusInMeters) {
		double radius = radiusInMeters / EARTH_AVERAGE_RADIUS;

		if (radius >= Math.PI) {
			throw new IllegalArgumentException("Radius has to be smaller than half the circumference of the Earth.");
		}

		double distanceInMeters = Math.acos(1 - (1 - Math.cos(radius)) * random.nextDouble()) * EARTH_AVERAGE_RADIUS;
		double bearingInDegrees = random.nextDouble() * 2 * Math.PI;
		return this.moveBy(distanceInMeters, bearingInDegrees);
	}

	@Override
	public String toString() {
		return "Location [longitude=" + this.longitude + ", latitude=" + this.latitude + ", altitude=" + this.altitude
				+ "]";
	}
}
