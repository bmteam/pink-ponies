package ru.pinkponies.app;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.graphics.Point;
import android.graphics.drawable.Drawable;

public class MyItemizedOverlay extends ItemizedOverlay<OverlayItem> {
	private final static Logger logger = Logger.getLogger(MyItemizedOverlay.class.getName());

	private final ArrayList<OverlayItem> overlayItemList = new ArrayList<OverlayItem>();

	public MyItemizedOverlay(final Drawable pDefaultMarker, final ResourceProxy pResourceProxy) {
		super(pDefaultMarker, pResourceProxy);
		// TODO Auto-generated constructor stub
	}

	public synchronized void addItem(final GeoPoint p, final String title) {
		for (OverlayItem i : this.overlayItemList) {
			if (i.mTitle.equals(title)) {
				logger.info("Item named " + title + " already exists.");
				throw new IllegalArgumentException();
			}
		}
		OverlayItem newItem = new OverlayItem(title, title, p);
		this.overlayItemList.add(newItem);
		this.populate();
	}

	public synchronized void removeItem(final String title) {
		for (OverlayItem i : this.overlayItemList) {
			if (i.mTitle.equals(title)) {
				this.overlayItemList.remove(i);
			}
		}
	}

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
