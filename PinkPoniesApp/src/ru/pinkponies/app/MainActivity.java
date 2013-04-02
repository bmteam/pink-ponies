package ru.pinkponies.app;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;

import ru.pinkponies.protocol.LocationUpdatePacket;
import ru.pinkponiesapp.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
	// FIXME(alexknvl): Constants should be declared as `static final`.
	private int SERVICE_DELAY = 1000;

	// FIXME(alexknvl): Declare logger before any other fields.
	private final static Logger logger = Logger.getLogger(MainActivity.class
			.getName());

	private LocationManager locationManager;

	private NetworkingThread networkingThread;

	private MapController mapController;

	private String login = "";
	private String password = "";

	// FIXME(alexknvl): private?
	public Handler messageHandler;
	// FIXME(alexknvl): private?
	MyLocationOverlay myLocationOverlay = null;

	private MapView mapView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			logger.info("Initializing...");

			// FIXME(alexknvl): Super should always come first in the method,
			// and in this case preferably outside the try-catch
			super.onCreate(savedInstanceState);

			// FIXME(alexknvl): Reorder code lines so that consecutive lines
			// are logically connected (i.e. first get activity data, then
			// initialize the GUI, or vice-versa).
			Intent intent = getIntent();
			setContentView(R.layout.activity_main);

			Bundle extras = intent.getExtras();
			login = extras.getString("login");
			password = extras.getString("password");

			mapView = (MapView) findViewById(R.id.MainActivityMapview);
			mapView.setBuiltInZoomControls(true);
			mapView.setMultiTouchControls(true);

			mapController = mapView.getController();

			messageHandler = new MessageHandler(this);

			networkingThread = new NetworkingThread(this);
			networkingThread.start();

			myLocationOverlay = new MyLocationOverlay(this, mapView);
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
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 1000, 1, this);

			mapController.setZoom(7);
			logger.info("Initialized!");
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception", e);
		}
	}

	@Override
	protected void onResume() {
		// FIXME(alexknvl): @see `Logger.entering`
		logger.info("MainActivity:onResume");
		// FIXME(alexknvl): Remove all such TODO's.
		// TODO Auto-generated method stub
		super.onResume();
		myLocationOverlay.enableMyLocation();
		myLocationOverlay.enableFollowLocation();
	}

	@Override
	protected void onPause() {
		// FIXME(alexknvl): @see `Logger.entering`
		logger.info("MainActivity:onPause");
		// TODO Auto-generated method stub
		super.onPause();
		myLocationOverlay.disableMyLocation();
		myLocationOverlay.disableFollowLocation();
	}

	@Override
	protected void onDestroy() {
		// FIXME(alexknvl): @see `Logger.entering`
		logger.info("MainActivity:onDestroy");
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		logger.info("MainActivity:onSaveInstanceState");
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
			// TODO
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