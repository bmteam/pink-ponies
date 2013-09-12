/**
 * Copyright (c) 2013 Alexander Konovalov, Andrey Konovalov, Sergey Voronov, Vitaly Malyshev. All
 * rights reserved. Use of this source code is governed by a BSD-style license that can be found in
 * the LICENSE file.
 */

package ru.pinkponies.app;

import java.util.HashMap;
import java.util.Map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.graphics.Color;

/**
 * An overlay which displays arbitrary image markers on the map.
 */
public final class MapOverlay {
	private final GoogleMap map;

	private final Map<String, Marker> markers = new HashMap<String, Marker>();
	private final Map<String, Circle> circles = new HashMap<String, Circle>();

	public MapOverlay(final GoogleMap map) {
		this.map = map;
	}

	public synchronized void addMarker(final String name, final LatLng location, final BitmapDescriptor bitmapDescriptor) {
		if (this.markers.containsKey(name)) {
			throw new IllegalArgumentException("Item named " + name + " already exists.");
		}
		Marker marker = this.map.addMarker(new MarkerOptions().position(location).icon(bitmapDescriptor));
		this.markers.put(name, marker);
	}

	public synchronized void removeMarker(final String name) {
		Marker marker = this.markers.get(name);
		if (marker != null) {
			marker.remove();
			this.markers.remove(name);
		}
	}

	public synchronized void addCircle(final String name, final LatLng location, final double radius, final int color) {
		if (this.circles.containsKey(name)) {
			throw new IllegalArgumentException("Circle named " + name + " already exists.");
		}
		Circle circle = this.map.addCircle(new CircleOptions().center(location).radius(radius).strokeWidth(2)
				.strokeColor(Color.BLACK).fillColor(color));
		this.circles.put(name, circle);
	}

	public synchronized void removeCircle(final String name) {
		Circle circle = this.circles.get(name);
		if (circle != null) {
			circle.remove();
			this.circles.remove(name);
		}
	}
}
