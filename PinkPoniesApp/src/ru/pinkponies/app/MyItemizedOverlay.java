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

public class MyItemizedOverlay extends ItemizedOverlay<OverlayItem> {
	private final List<OverlayItem> overlayItemList = new ArrayList<OverlayItem>();

	public MyItemizedOverlay(final Drawable defaultMarker, final ResourceProxy resourceProxy) {
		super(defaultMarker, resourceProxy);
		// TODO Auto-generated constructor stub
	}

	public synchronized void addItem(final GeoPoint point, final String title) {
		for (OverlayItem i : this.overlayItemList) {
			if (i.mTitle.equals(title)) {
				throw new IllegalArgumentException("Item named " + title + " already exists.");
			}
		}
		OverlayItem newItem = new OverlayItem(title, title, point);
		this.overlayItemList.add(newItem);
		this.populate();
	}

	public synchronized void removeItem(final String title) {
		for (OverlayItem i : this.overlayItemList) {
			if (i.mTitle.equals(title)) {
				this.overlayItemList.remove(i);
				break;
			}
		}
		this.populate();
	}

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
