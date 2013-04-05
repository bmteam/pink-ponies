package ru.pinkponies.app;

import java.util.ArrayList;

import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.graphics.Point;
import android.graphics.drawable.Drawable;

public class MyItemizedOverlay extends ItemizedOverlay<OverlayItem> {
	// FIXME(alexknvl): Use HashMap to search for items faster.
	private ArrayList<OverlayItem> overlayItemList = new ArrayList<OverlayItem>();

	public MyItemizedOverlay(Drawable pDefaultMarker,
			ResourceProxy pResourceProxy) {
		super(pDefaultMarker, pResourceProxy);
		// TODO Auto-generated constructor stub
	}

	// FIXME(alexknvl): Do we really need `snippet` here?
	public synchronized void addItem(GeoPoint p, String title, String snippet) {
		OverlayItem newItem = new OverlayItem(title, snippet, p);
		// FIXME(alexknvl): Check that there is only one item with
		// the given name (title).
		overlayItemList.add(newItem);
		populate();
	}

	public synchronized void removeItem(String title) {
		for (OverlayItem i : overlayItemList) {
			if (i.mTitle.equals(title))
				overlayItemList.remove(i);
		}
	}

	public synchronized void resetItemMarker(String title, Drawable newMarker) {
		for (OverlayItem i : overlayItemList) {
			if (i.mTitle.equals(title))
				i.setMarker(newMarker);
		}
	}

	@Override
	public boolean onSnapToItem(int arg0, int arg1, Point arg2, IMapView arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected OverlayItem createItem(int arg0) {
		// TODO Auto-generated method stub
		return overlayItemList.get(arg0);
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return overlayItemList.size();
	}

}
