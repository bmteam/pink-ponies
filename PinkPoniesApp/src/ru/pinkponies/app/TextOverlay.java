package ru.pinkponies.app;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

public class TextOverlay extends Overlay {
	private final Paint rectPaint = new Paint();
	private final Paint textPaint = new Paint();
	private GeoPoint position;
	private String text;
	
	public TextOverlay(final Context ctx, final MapView mapView) {
        this(ctx, mapView, new DefaultResourceProxyImpl(ctx));
	}
	
	public TextOverlay(final Context ctx, final MapView mapView, final ResourceProxy resourceProxy) {
		super(resourceProxy);
	}

	public void setPosition(GeoPoint geoPoint) {
		this.position = geoPoint;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	@Override
	protected void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {
		if (shadow) {
			return;
		}
		
		Projection projection = mapView.getProjection();
		
		Point positionPoint = new Point();
		projection.toMapPixels(position, positionPoint);
		
		textPaint.setARGB(0xFF, 0x00, 0x00, 0x00);
		textPaint.setAntiAlias(true);
		textPaint.setTextSize(12);
		canvas.drawText(text, positionPoint.x, positionPoint.y, textPaint);
	}

}
