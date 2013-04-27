/**
 * Copyright (c) 2013 Alexander Konovalov, Andrey Konovalov, Sergey Voronov, Vitaly Malyshev. All
 * rights reserved. Use of this source code is governed by a BSD-style license that can be found in
 * the LICENSE file.
 */

package ru.pinkponies.app;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.graphics.Point;
import android.graphics.drawable.Drawable;

/**
 * An overlay which displays arbitrary image markers on the map.
 * 
 * @author Vitaly Malyshev
 * 
 */
public final class MyItemizedOverlay extends ItemizedOverlay<OverlayItem> {
	/**
	 * The image list.
	 */
	private final List<OverlayItem> overlayItemList = new ArrayList<OverlayItem>();

	/**
	 * Creates a new itemized overlay.
	 * 
	 * @param pDefaultMarker
	 *            the default item marker
	 * @param pResourceProxy
	 *            the
	 */
	public MyItemizedOverlay(final Drawable defaultMarker, final ResourceProxy resourceProxy) {
		super(defaultMarker, resourceProxy);
	}

	/**
	 * Adds a new item to the image list.
	 * 
	 * @param point
	 *            the position of the new image
	 * @param title
	 *            the image title/id
	 */
	public synchronized void addItem(final GeoPoint point, final String title) {
		for (OverlayItem i : this.overlayItemList) {
			if (i.mTitle.equals(title)) {
				throw new IllegalArgumentException("Item named " + title + " already exists.");
			}
		}
		final OverlayItem newItem = new OverlayItem(title, title, point);
		this.overlayItemList.add(newItem);
		this.populate();
	}

	/**
	 * Removes an item with the given title/id.
	 * 
	 * @param title
	 *            the item title/id
	 */
	public synchronized void removeItem(final String title) {
		for (OverlayItem i : this.overlayItemList) {
			if (i.mTitle.equals(title)) {
				this.overlayItemList.remove(i);
				break;
			}
		}
		this.populate();
	}

	/**
	 * Resets image marker for the given title/id.
	 * 
	 * @param title
	 *            the item title/id
	 * @param newMarker
	 *            the new image
	 */
	public synchronized void resetItemMarker(final String title, final Drawable newMarker) {
		for (OverlayItem item : this.overlayItemList) {
			if (item.mTitle.equals(title)) {
				item.setMarker(newMarker);
				break;
			}
		}
		this.populate();
	}

	@Override
	public boolean onSnapToItem(final int x, final int y, final Point snapPoint, final IMapView mapView) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected OverlayItem createItem(final int i) {
		// TODO Auto-generated method stub
		return this.overlayItemList.get(i);
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return this.overlayItemList.size();
	}

}
