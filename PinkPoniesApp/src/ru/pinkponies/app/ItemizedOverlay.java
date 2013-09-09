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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * An overlay which displays arbitrary image markers on the map.
 */
public final class ItemizedOverlay {
	private final GoogleMap map;
	private final BitmapDescriptor bitmapDescriptor;

	private final Map<String, Marker> overlayItems = new HashMap<String, Marker>();

	public ItemizedOverlay(final GoogleMap map, final BitmapDescriptor bitmapDescriptor) {
		this.map = map;
		this.bitmapDescriptor = bitmapDescriptor;
	}

	public synchronized void addItem(final String name, final LatLng location) {
		if (this.overlayItems.containsKey(name)) {
			throw new IllegalArgumentException("Item named " + name + " already exists.");
		}
		Marker marker = this.map.addMarker(new MarkerOptions().position(location).icon(this.bitmapDescriptor));
		this.overlayItems.put(name, marker);
	}

	public synchronized void removeItem(final String name) {
		Marker marker = this.overlayItems.get(name);
		marker.remove();
		this.overlayItems.remove(name);
	}
}
