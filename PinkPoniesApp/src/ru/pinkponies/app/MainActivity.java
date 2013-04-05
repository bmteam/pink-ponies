package ru.pinkponies.app;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.PathOverlay;

import ru.pinkponies.protocol.LocationUpdatePacket;
import ru.pinkponies.app.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity implements LocationListener {
	private final static Logger logger = Logger.getLogger(MainActivity.class
			.getName());

	// FIXME(alexknvl): SERVICE_DELAY should come first.
	// FIXME(alexknvl): Group fields according to their meaning: GUI, 
	//   networking, other fields.
	private TextOverlay textOverlay;
	private static final int SERVICE_DELAY = 1000;

	private LocationManager locationManager;

	private NetworkingThread networkingThread;

	private MapController mapController;

	private String login = "";
	private String password = "";

	public Handler messageHandler;

	// FIXME(alexknvl): Add private to these fields.
	GeoPoint myPoint = new GeoPoint(55929563, 37523862);
	PathOverlay myPath = null;
	MyLocationOverlay myLocationOverlay = null;
	MyItemizedOverlay myPersonOverlay = null;
	MyItemizedOverlay myAppleOverlay = null;

	private MapView mapView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		logger.info("MainActivity:Initializing...");

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		login = extras.getString("login");
		password = extras.getString("password");

		setContentView(R.layout.activity_main);

		messageHandler = new MessageHandler(this);

		networkingThread = new NetworkingThread(this);
		networkingThread.start();

		// FIXME(alexknvl): Initialize gui before networking thread.
		mapView = (MapView) findViewById(R.id.MainActivityMapview);
		mapController = mapView.getController();
		myLocationOverlay = new MyLocationOverlay(this, mapView);
		myPath = new PathOverlay(Color.GREEN, this);
		mapView.getOverlays().add(myPath);
		textOverlay = new TextOverlay(this, mapView);
		textOverlay.setPosition(new GeoPoint(55.9, 37.5));
		textOverlay.setText("Hello, world!");
		mapView.getOverlays().add(textOverlay);

		mapView.setBuiltInZoomControls(true);
		mapView.setMultiTouchControls(true);
		mapView.getOverlays().add(myLocationOverlay);
		mapView.postInvalidate();

		myLocationOverlay.runOnFirstFix(new Runnable() {
			public void run() {
				mapView.getController().animateTo(
						myLocationOverlay.getMyLocation());
			}
		});

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 1000, 1, this);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				1000, 1, this);

		// FIXME(alexknvl): Group GUI initialization code together.
		mapController.setZoom(7);

		// FIXME(alexknvl): Comments should start with a capital letter and
		//   and be complete sentences with a dot at the end.
		// add Person overlay
		Drawable marker = getResources().getDrawable(R.drawable.person);
		int markerWidth = marker.getIntrinsicWidth();
		int markerHeight = marker.getIntrinsicHeight();
		marker.setBounds(0, markerHeight, markerWidth, 0);

		ResourceProxy resourceProxy = new DefaultResourceProxyImpl(
				getApplicationContext());

		myPersonOverlay = new MyItemizedOverlay(marker, resourceProxy);
		mapView.getOverlays().add(myPersonOverlay);

		// add Apple overlay
		marker = getResources().getDrawable(R.drawable.apple);
		markerWidth = marker.getIntrinsicWidth();
		markerHeight = marker.getIntrinsicHeight();
		marker.setBounds(0, markerHeight, markerWidth, 0);

		resourceProxy = new DefaultResourceProxyImpl(getApplicationContext());

		myAppleOverlay = new MyItemizedOverlay(marker, resourceProxy);
		mapView.getOverlays().add(myAppleOverlay);

		// player 1
		// myPersonOverlay.addItem(myPoint, "player1", "player1");
		// apples
		GeoPoint applePoint1 = new GeoPoint(myPoint.getLatitudeE6() + 20000,
				myPoint.getLongitudeE6() + 10000);
		myAppleOverlay.addItem(applePoint1, "Apple1", "Apple1");
		GeoPoint applePoint2 = new GeoPoint(myPoint.getLatitudeE6() + 14000,
				myPoint.getLongitudeE6() - 10000);
		myAppleOverlay.addItem(applePoint2, "Apple2", "Apple2");
		GeoPoint applePoint3 = new GeoPoint(myPoint.getLatitudeE6() - 7000,
				myPoint.getLongitudeE6() + 10000);
		myAppleOverlay.addItem(applePoint3, "Apple3", "Apple3");
		myAppleOverlay.addItem(myPoint, "Apple4", "Apple4");

		/*
		 * path final PathOverlay p1Path = new PathOverlay(Color.RED, this);
		 * p1Path.addPoint(applePoint1); p1Path.addPoint(applePoint2);
		 * p1Path.addPoint(applePoint3); mapView.getOverlays().add(p1Path);
		 * 
		 * // magic button final Button button = (Button)
		 * findViewById(R.id.button1);
		 * 
		 * button.setOnClickListener(new Button.OnClickListener() { public void
		 * onClick(View v){ myPoint = new GeoPoint(myPoint.getLatitudeE6() +
		 * 10000, myPoint.getLongitudeE6() + 10000);
		 * myPersonOverlay.removeItem("player1");
		 * myPersonOverlay.addItem(myPoint, "player1", "player1");
		 * //p1Path.addPoint(myPoint); //Drawable
		 * marker=getResources().getDrawable(R.drawable.shit);
		 * //myAppleOverlay.resetItemMarker("Apple3", marker);
		 * mapView.invalidate(); } });
		 */

		logger.info("MainActivity:Initialized!");
	}

	@Override
	protected void onResume() {
		super.onResume();
		myLocationOverlay.enableMyLocation();
		myLocationOverlay.enableFollowLocation();
	}

	@Override
	protected void onPause() {
		super.onPause();
		myLocationOverlay.disableMyLocation();
		myLocationOverlay.disableFollowLocation();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("zoomLevel", mapView.getZoomLevel());
	}

	protected void onRestoreInstanceState(Bundle outState) {
		logger.info("MainActivity:onSaveInstanceState");
		super.onRestoreInstanceState(outState);
		outState.getInt("zoomLevel");
		mapController.setZoom(outState.getInt("zoomLevel"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onLogoutClick(View view) {
		goToLoginActivity(view);
		MainActivity.this.finish();
	}

	private void onMessageFromNetworkingThread(Object message) {
		logger.info("NT: " + message.toString());
		if (message.equals("initialized")) {
			sendMessageToNetworkingThread("connect");
			sendMessageToNetworkingThread("service");
		} else if (message.equals("connected")) {
			sendMessageToNetworkingThread("login");

			new Timer().scheduleAtFixedRate(new TimerTask() {

				@Override
				public void run() {
					sendMessageToNetworkingThread("service");
				}

			}, 0, SERVICE_DELAY);
		} else if (message instanceof LocationUpdatePacket) {
			LocationUpdatePacket packet = (LocationUpdatePacket) message;
			logger.info(packet.clientID + "!" + Build.DISPLAY);
			if (!(packet.clientID).equals(Build.DISPLAY)) {
				GeoPoint point = new GeoPoint(packet.latitude, packet.longitude);
				myPersonOverlay.removeItem(packet.clientID);
				myPersonOverlay
						.addItem(point, packet.clientID, packet.clientID);
			}
		}
	}

	private void sendMessageToNetworkingThread(Object message) {
		Message msg = networkingThread.messageHandler.obtainMessage();
		msg.obj = message;
		networkingThread.messageHandler.sendMessage(msg);
	}

	public static class MessageHandler extends Handler {
		private WeakReference<MainActivity> activity;

		MessageHandler(MainActivity mainActivity) {
			activity = new WeakReference<MainActivity>(mainActivity);
		}

		@Override
		public void handleMessage(Message msg) {
			activity.get().onMessageFromNetworkingThread(msg.obj);
		}
	}

	public void goToLoginActivity(View view) {
		Intent intent = new Intent(MainActivity.this, LoginActivity.class);
		startActivity(intent);
		this.onDestroy();
	}

	@Override
	public void onLocationChanged(Location location) {
		String clientID = Build.DISPLAY;
		double longitude = location.getLongitude();
		double latitude = location.getLatitude();
		double altitude = location.getAltitude();
		GeoPoint point = new GeoPoint(latitude, longitude);
		myPath.addPoint(point);
		if (point.distanceTo(myPoint) < 20) {
			Drawable marker = getResources().getDrawable(R.drawable.shit);
			myAppleOverlay.resetItemMarker("Apple4", marker);
		}

		textOverlay.setText("" + point.distanceTo(myPoint));

		sendMessageToNetworkingThread(new LocationUpdatePacket(clientID,
				longitude, latitude, altitude));
		logger.info("Location updated.");
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
	}

}