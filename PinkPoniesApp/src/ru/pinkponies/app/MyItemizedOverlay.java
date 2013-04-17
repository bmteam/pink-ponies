/**
 * Copyright (c) 2013 Alexander Konovalov, Andrey Konovalov, Sergey Voronov, Vitaly Malyshev. All
 * rights reserved. Use of this source code is governed by a BSD-style license that can be found in
 * the LICENSE file.
 */

package ru.pinkponies.app;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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
	 * The class wide logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(MyItemizedOverlay.class.getName());

	/**
	 * The image list.
	 */
	private final List<OverlayItem> overlayItemList = new ArrayList<OverlayItem>();

	/**
	 * Creates a new itemized overlay.
	 * 
	 * @param pDefaultMarker the default item marker.
	 * @param pResourceProxy the 
	 */
	public MyItemizedOverlay(final Drawable pDefaultMarker, final ResourceProxy pResourceProxy) {
		super(pDefaultMarker, pResourceProxy);
	}

	/**
	 * Adds a new item to the image list.
	 * 
	 * @param position
	 *            the position of the new image.
	 * @param title
	 *            the image title/id.
	 */
	public synchronized void addItem(final GeoPoint position, final String title) {
		for (OverlayItem i : this.overlayItemList) {
			if (i.mTitle.equals(title)) {
				LOGGER.info("Item named " + title + " already exists.");
			}

			throw new IllegalArgumentException("Item named " + title + " already exists.");
		}
		final OverlayItem newItem = new OverlayItem(title, title, position);
		this.overlayItemList.add(newItem);
		this.populate();
	}

	/**
	 * Removes an item with the given title/id.
	 * 
	 * @param title
	 *            the item title/id.
	 */
	public synchronized void removeItem(final String title) {
		for (OverlayItem i : this.overlayItemList) {
			if (i.mTitle.equals(title)) {
				this.overlayItemList.remove(i);
			}
		}
	}

	/**
	 * Resets image marker for the given title/id.
	 * 
	 * @param title
	 *            the item title/id.
	 * @param newMarker
	 *            the new image
	 */
	public synchronized void resetItemMarker(final String title, final Drawable newMarker) {
		for (OverlayItem i : this.overlayItemList) {
			if (i.mTitle.equals(title)) {
				i.setMarker(newMarker);
			}
		}
	}

	@Override
	public boolean onSnapToItem(final int arg0, final int arg1, final Point arg2, final IMapView arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected OverlayItem createItem(final int arg0) {
		// TODO Auto-generated method stub
		return this.overlayItemList.get(arg0);
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return this.overlayItemList.size();
	}

}
