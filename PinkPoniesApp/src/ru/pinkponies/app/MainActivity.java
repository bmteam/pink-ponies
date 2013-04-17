/**
 * Copyright (c) 2013 Alexander Konovalov, Andrey Konovalov, Sergey Voronov, Vitaly Malyshev. All
 * rights reserved. Use of this source code is governed by a BSD-style license that can be found in
 * the LICENSE file.
 */

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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;

import ru.pinkponies.protocol.AppleUpdatePacket;
import ru.pinkponies.protocol.ClientOptionsPacket;
import ru.pinkponies.protocol.LocationUpdatePacket;
import ru.pinkponies.protocol.SayPacket;

/**
 * The main activity class.
 */
public final class MainActivity extends Activity implements LocationListener {
	/**
	 * The class wide logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(MainActivity.class.getName());

	/**
	 * The delay between consecutive network IO updates.
	 */
	private static final int SERVICE_DELAY = 1000;

	/**
	 * The minimum time interval between location updates, in milliseconds.
	 */
	private static final long LOCATION_UPDATE_MIN_DELAY = 1000;

	/**
	 * The minimum distance between location updates, in meters.
	 */
	private static final float LOCATION_UPDATE_MIN_DISTANCE = 1;

	/**
	 * The initial map view zoom level.
	 */
	private static final int MAP_VIEW_INITIAL_ZOOM_LEVEL = 15;

	/**
	 * This value is used the identifier is not yet defined.
	 */
	private static final long BAD_ID = -1;

	/**
	 * The message handler which receives messages for this activity.
	 */
	private final Handler messageHandler = new MessageHandler(this);

	/**
	 * The networking thread which provides this activity with an asynchronous access to network IO.
	 */
	private NetworkingThread networkingThread;

	/**
	 * The location service manager.
	 */
	private LocationManager locationManager;

	/**
	 * The map view widget.
	 */
	private MapView mapView;

	/**
	 * The map controller.
	 */
	private MapController mapController;

	/**
	 * The overlay displaying player's location.
	 */
	private MyLocationOverlay myLocationOverlay;

	/**
	 * The overlay displaying other people locations.
	 */
	private MyItemizedOverlay myPersonOverlay;

	/**
	 * The overlay displaying apple locations.
	 */
	private MyItemizedOverlay myAppleOverlay;

	/**
	 * The overlay which displays the path of the player.
	 */
	private PathOverlay myPath;

	/**
	 * The identifier of the player.
	 */
	private long myId = BAD_ID;

	// private TextOverlay textOverlay;

	// private String login = "";
	// private String password = "";

	/**
	 * Creates a new itemized overlay. This overlay will render image markers with the given
	 * resource id.
	 * 
	 * @param resourceId
	 *            Resource id.
	 * @return Created itemized overlay.
	 */
	private MyItemizedOverlay createItemizedOverlay(final int resourceId) {
		final Drawable marker = this.getResources().getDrawable(resourceId);

		final int markerWidth = marker.getIntrinsicWidth();
		final int markerHeight = marker.getIntrinsicHeight();
		marker.setBounds(0, markerHeight, markerWidth, 0);

		final ResourceProxy resourceProxy = new DefaultResourceProxyImpl(this.getApplicationContext());
		return new MyItemizedOverlay(marker, resourceProxy);
	}

	/**
	 * Returns the message handler associated with this activity.
	 * 
	 * @return the message handler
	 */
	public Handler getMessageHandler() {
		return this.messageHandler;
	}

	/**
	 * Called when the activity is first created. Initializes GUI, networking, creates overlays.
	 * 
	 * @param savedInstanceState
	 *            If the activity is being re-initialized after previously being shut down then this
	 *            Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
	 *            Note: Otherwise it is null.
	 */
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LOGGER.info("Initializing.");

		// Intent intent = getIntent();
		// Bundle extras = intent.getExtras();
		// login = extras.getString("login");
		// password = extras.getString("password");

		this.setContentView(R.layout.activity_main);

		this.networkingThread = new NetworkingThread(this);
		this.networkingThread.start();

		this.locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_UPDATE_MIN_DELAY,
				LOCATION_UPDATE_MIN_DISTANCE, this);
		this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_UPDATE_MIN_DELAY,
				LOCATION_UPDATE_MIN_DISTANCE, this);

		// GUI.
		this.mapView = (MapView) this.findViewById(R.id.MainActivityMapview);
		this.mapView.setMultiTouchControls(true);

		this.mapController = this.mapView.getController();
		this.mapController.setZoom(MAP_VIEW_INITIAL_ZOOM_LEVEL);

		this.myLocationOverlay = new MyLocationOverlay(this, this.mapView);
		this.mapView.getOverlays().add(this.myLocationOverlay);

		this.myPath = new PathOverlay(Color.GREEN, this);
		this.mapView.getOverlays().add(this.myPath);

		this.myLocationOverlay.runOnFirstFix(new Runnable() {
			@Override
			public void run() {
				MainActivity.this.mapView.getController()
						.animateTo(MainActivity.this.myLocationOverlay.getMyLocation());
			}
		});

		this.mapView.postInvalidate();

		// textOverlay = new TextOverlay(this, mapView);
		// textOverlay.setPosition(new GeoPoint(55.9, 37.5));
		// textOverlay.setText("Hello, world!");
		// mapView.getOverlays().add(textOverlay);

		this.myPersonOverlay = this.createItemizedOverlay(R.drawable.person);
		this.mapView.getOverlays().add(this.myPersonOverlay);

		this.myAppleOverlay = this.createItemizedOverlay(R.drawable.apple);
		this.mapView.getOverlays().add(this.myAppleOverlay);

		// GeoPoint myPoint = new GeoPoint(55929563, 37523862);
		// this.myAppleOverlay.addItem(myPoint, "Apple");

		LOGGER.info("Initialized.");
	}

	/**
	 * Called when the activity will start interacting with the user.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		this.myLocationOverlay.enableMyLocation();
		this.myLocationOverlay.enableFollowLocation();
	}

	/**
	 * Called when the system is about to start resuming a previous activity.
	 */
	@Override
	protected void onPause() {
		super.onPause();
		this.myLocationOverlay.disableMyLocation();
		this.myLocationOverlay.disableFollowLocation();
	}

	/**
	 * The final call you receive before your activity is destroyed.
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * This method is called before an activity may be killed so that when it comes back some time
	 * in the future it can restore its state.
	 * 
	 * @param outState
	 *            Bundle in which this activity state is placed.
	 */
	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("zoomLevel", this.mapView.getZoomLevel());
	}

	/**
	 * This method is called after onStart() when the activity is being re-initialized from a
	 * previously saved state, given here in savedInstanceState.
	 * 
	 * @param outState
	 *            The data most recently supplied in onSaveInstanceState(Bundle).
	 */
	@Override
	protected void onRestoreInstanceState(final Bundle outState) {
		MainActivity.LOGGER.info("MainActivity:onSaveInstanceState");
		super.onRestoreInstanceState(outState);
		outState.getInt("zoomLevel");
		this.mapController.setZoom(outState.getInt("zoomLevel"));
	}

	/**
	 * Called once, the first time the options menu is displayed.
	 * 
	 * @param menu
	 *            The options menu.
	 * @return True.
	 */
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		this.getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * Called when the logout button is pressed.
	 * 
	 * @param view
	 *            The button widget.
	 */
	public void onLogoutClick(final View view) {
		this.goToLoginActivity();
		this.finish();
	}

	/**
	 * Called when there is a new message from the networking thread.
	 * 
	 * @param message
	 *            The message which was received.
	 */
	private void onMessageFromNetworkingThread(final Object message) {
		MainActivity.LOGGER.info("NT: " + message.toString());

		if (message.equals("initialized")) {
			this.sendMessageToNetworkingThread("connect");
			this.sendMessageToNetworkingThread("service");
		} else if (message.equals("connected")) {
			this.sendMessageToNetworkingThread("login");

			new Timer().scheduleAtFixedRate(new TimerTask() {

				@Override
				public void run() {
					MainActivity.this.sendMessageToNetworkingThread("service");
				}

			}, 0, MainActivity.SERVICE_DELAY);
		} else if (message.equals("failed")) {
			this.showMessageBox("Socket exception.", null);
		} else if (message instanceof ClientOptionsPacket) {
			final ClientOptionsPacket packet = (ClientOptionsPacket) message;
			this.myId = packet.getClientId();
		} else if (message instanceof SayPacket) {
			final SayPacket packet = (SayPacket) message;
			LOGGER.info(packet.toString());
		} else if (message instanceof LocationUpdatePacket) {
			final LocationUpdatePacket packet = (LocationUpdatePacket) message;
			if (this.myId != BAD_ID && packet.getClientId() != this.myId) {
				final GeoPoint point = new GeoPoint(packet.getLocation().getLatitude(), packet.getLocation()
						.getLongitude());
				final String title = "Player" + String.valueOf(packet.getClientId());

				this.myPersonOverlay.removeItem(title);
				this.myPersonOverlay.addItem(point, title);
			}
		} else if (message instanceof AppleUpdatePacket) {
			final AppleUpdatePacket packet = (AppleUpdatePacket) message;
			final String title = "Apple" + String.valueOf(packet.getAppleId());
			if (packet.getStatus()) {
				final GeoPoint point = new GeoPoint(packet.getLocation().getLatitude(), packet.getLocation()
						.getLongitude());
				this.myAppleOverlay.addItem(point, title);
			} else {
				this.myAppleOverlay.removeItem(title);
			}
			LOGGER.info("Apple " + String.valueOf(packet.getAppleId()) + " updated.");
		}
	}

	/**
	 * Asynchronously sends the given message to the networking thread.
	 * 
	 * @param message
	 *            The message to send.
	 */
	private void sendMessageToNetworkingThread(final Object message) {
		final Message msg = this.networkingThread.getMessageHandler().obtainMessage();
		msg.obj = message;
		this.networkingThread.getMessageHandler().sendMessage(msg);
	}

	/**
	 * Switches current activity to login activity.
	 */
	public void goToLoginActivity() {
		final Intent intent = new Intent(MainActivity.this, LoginActivity.class);
		this.startActivity(intent);
		this.onDestroy();
	}

	/**
	 * Called when the player's location is changed.
	 * 
	 * @param location
	 *            The new location, as a Location object.
	 */
	@Override
	public void onLocationChanged(final Location location) {
		final double longitude = location.getLongitude();
		final double latitude = location.getLatitude();
		final double altitude = location.getAltitude();

		final GeoPoint point = new GeoPoint(latitude, longitude);
		this.myPath.addPoint(point);

		final ru.pinkponies.protocol.Location loc = new ru.pinkponies.protocol.Location(longitude, latitude, altitude);
		final LocationUpdatePacket packet = new LocationUpdatePacket(this.myId, loc);
		this.sendMessageToNetworkingThread(packet);

		MainActivity.LOGGER.info("Location updated.");
	}

	/**
	 * Called when the location provider is disabled by the user.
	 * 
	 * @param provider
	 *            The name of the location provider associated with this update.
	 */
	@Override
	public void onProviderDisabled(final String provider) {
	}

	/**
	 * Called when the location provider is enabled by the user.
	 * 
	 * @param provider
	 *            The name of the location provider associated with this update.
	 */
	@Override
	public void onProviderEnabled(final String provider) {
	}

	/**
	 * Called when the provider status changes. This method is called when a provider is unable to
	 * fetch a location or if the provider has recently become available after a period of
	 * unavailability.
	 * 
	 * @param provider
	 *            The name of the location provider associated with this update.
	 * @param status
	 *            The status of the provider.
	 * @param extras
	 *            Extra information about this update.
	 */
	@Override
	public void onStatusChanged(final String provider, final int status, final Bundle extras) {
	}

	/**
	 * Shows a message box with the specified title and message.
	 * 
	 * @param title
	 *            The title of the message box.
	 * @param message
	 *            The message that will be shown in the message box.
	 */
	public void showMessageBox(final String title, final String message) {
		final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle(title);
		alertDialogBuilder.setMessage(message).setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog, final int id) {
					}
				});
		final AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

	/**
	 * A message handler class for this activity.
	 */
	public static final class MessageHandler extends Handler {
		/**
		 * The weak reference to the activity.
		 */
		private final WeakReference<MainActivity> activity;

		/**
		 * Creates a new message handler which handles messages sent to the activity.
		 * 
		 * @param mainActivity
		 *            The activity.
		 */
		MessageHandler(final MainActivity mainActivity) {
			this.activity = new WeakReference<MainActivity>(mainActivity);
		}

		/**
		 * Handles incoming messages and sends them to the activity.
		 * 
		 * @param msg
		 *            The incoming message.
		 */
		@Override
		public void handleMessage(final Message msg) {
			this.activity.get().onMessageFromNetworkingThread(msg.obj);
		}
	}
}
