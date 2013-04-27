/**
 * Copyright (c) 2013 Alexander Konovalov, Andrey Konovalov, Sergey Voronov, Vitaly Malyshev. All
 * rights reserved. Use of this source code is governed by a BSD-style license that can be found in
 * the LICENSE file.
 */

package ru.pinkponies.app;

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
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.View;

import ru.pinkponies.app.net.NetworkListener;
import ru.pinkponies.app.net.NetworkingService;
import ru.pinkponies.protocol.AppleUpdatePacket;
import ru.pinkponies.protocol.ClientOptionsPacket;
import ru.pinkponies.protocol.PlayerUpdatePacket;
import ru.pinkponies.protocol.QuestUpdatePacket;
import ru.pinkponies.protocol.SayPacket;

/**
 * The main activity class.
 */
public final class MainActivity extends Activity implements LocationListener, NetworkListener {
	/**
	 * The class wide logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(MainActivity.class.getName());

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
	private static final int MAP_VIEW_INITIAL_ZOOM_LEVEL = 18;

	/**
	 * This value is used when the identifier is not yet defined.
	 */
	private static final long BAD_ID = -1;

	/**
	 * The size of the objects on the map.
	 */
	private static final int ICON_SIZE = 48;

	/**
	 * The networking service.
	 */
	private NetworkingService networkingService;

	/**
	 * The connection between main activity and networking service.
	 */
	private final ServiceConnection networkingServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(final ComponentName className, final IBinder binder) {
			MainActivity.this.networkingService = ((NetworkingService.LocalBinder) binder).getService();
			MainActivity.this.onNetworkingServiceConnected();
			LOGGER.info("Service connected");
		}

		@Override
		public void onServiceDisconnected(final ComponentName className) {
			MainActivity.this.onNetworkingServiceDisconnected();
			MainActivity.this.networkingService = null;

			LOGGER.info("Service disconnected");
		}

	};

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
	private MyLocationOverlay locationOverlay;

	/**
	 * The overlay displaying other people locations.
	 */
	private MyItemizedOverlay personOverlay;

	/**
	 * The overlay displaying apple locations.
	 */
	private MyItemizedOverlay appleOverlay;

	/**
	 * The overlay displaying quest locations.
	 */
	private MyItemizedOverlay questOverlay;

	/**
	 * The overlay which displays the path of the player.
	 */
	private PathOverlay pathOverlay;

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
		final Bitmap bitmap = ((BitmapDrawable) marker).getBitmap();
		final Drawable bitmapMarker = new BitmapDrawable(this.getResources(), Bitmap.createScaledBitmap(bitmap,
				ICON_SIZE, ICON_SIZE, true));

		final ResourceProxy resourceProxy = new DefaultResourceProxyImpl(this.getApplicationContext());
		return new MyItemizedOverlay(bitmapMarker, resourceProxy);
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

		LOGGER.info("onCreate " + this.hashCode());

		// Intent intent = getIntent();
		// Bundle extras = intent.getExtras();
		// login = extras.getString("login");
		// password = extras.getString("password");

		this.setContentView(R.layout.activity_main);

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

		this.locationOverlay = new MyLocationOverlay(this, this.mapView);
		this.mapView.getOverlays().add(this.locationOverlay);

		this.pathOverlay = new PathOverlay(Color.GREEN, this);
		this.mapView.getOverlays().add(this.pathOverlay);

		this.locationOverlay.runOnFirstFix(new Runnable() {
			@Override
			public void run() {
				MainActivity.this.mapView.getController().animateTo(MainActivity.this.locationOverlay.getMyLocation());
			}
		});

		this.mapView.postInvalidate();

		// textOverlay = new TextOverlay(this, mapView);
		// textOverlay.setPosition(new GeoPoint(55.9, 37.5));
		// textOverlay.setText("Hello, world!");
		// mapView.getOverlays().add(textOverlay);

		this.personOverlay = this.createItemizedOverlay(R.drawable.player);
		this.mapView.getOverlays().add(this.personOverlay);

		this.appleOverlay = this.createItemizedOverlay(R.drawable.apple);
		this.mapView.getOverlays().add(this.appleOverlay);

		this.questOverlay = this.createItemizedOverlay(R.drawable.question);
		this.mapView.getOverlays().add(this.questOverlay);

		// GeoPoint myPoint = new GeoPoint(55929563, 37523862);
		// this.myAppleOverlay.addItem(myPoint, "Apple");

		LOGGER.info("Starting service");
		this.startService(new Intent(this, NetworkingService.class));
		this.bindService(new Intent(this, NetworkingService.class), this.networkingServiceConnection,
				Context.BIND_AUTO_CREATE);

		LOGGER.info("Initialized.");
	}

	/**
	 * Called when the activity will start interacting with the user.
	 */
	@Override
	protected void onResume() {
		super.onResume();

		LOGGER.info("onResume " + this.hashCode());

		this.locationOverlay.enableMyLocation();
		this.locationOverlay.enableFollowLocation();
	}

	/**
	 * Called when the system is about to start resuming a previous activity.
	 */
	@Override
	protected void onPause() {
		super.onPause();

		LOGGER.info("onPause");

		this.locationOverlay.disableMyLocation();
		this.locationOverlay.disableFollowLocation();
	}

	/**
	 * The final call you receive before your activity is destroyed.
	 */
	@Override
	protected void onDestroy() {
		LOGGER.info("onDestroy " + this.hashCode());

		if (this.networkingService != null) {
			this.unbindService(this.networkingServiceConnection);
			this.networkingService.removeListener(this);
			this.networkingService = null;
		}

		this.locationManager.removeUpdates(this);
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
		LOGGER.info("onSaveInstanceState " + this.hashCode());

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
		LOGGER.info("onRestoreInstanceState " + this.hashCode());

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
	 * Called when the networking service is for some reason disconnected from the main activity.
	 */
	protected void onNetworkingServiceDisconnected() {
		if (this.networkingService != null) {
			this.networkingService.removeListener(this);
			this.networkingService = null;
		}
	}

	/**
	 * Called when the networking service is connected to the main activity.
	 */
	protected void onNetworkingServiceConnected() {
		this.networkingService.addListener(this);

		if (this.networkingService.getState() != NetworkingService.State.CONNECTED) {
			this.networkingService.connect();
		}
	}

	/**
	 * Called when there is a new message from the networking service.
	 * 
	 * @param message
	 *            The message which was received.
	 */
	@Override
	public void onMessage(final Object message) {
		LOGGER.info(message.toString());

		if (message.equals("failed")) {
			this.showMessageBox("Socket exception.", null);
		} else if (message instanceof ClientOptionsPacket) {
			final ClientOptionsPacket packet = (ClientOptionsPacket) message;
			this.myId = packet.getClientId();
		} else if (message instanceof SayPacket) {
			final SayPacket packet = (SayPacket) message;
			LOGGER.info(packet.toString());
		} else if (message instanceof PlayerUpdatePacket) {
			final PlayerUpdatePacket packet = (PlayerUpdatePacket) message;
			if (this.myId != BAD_ID && packet.getClientId() != this.myId) {
				final GeoPoint point = new GeoPoint(packet.getLocation().getLatitude(), packet.getLocation()
						.getLongitude());
				final String title = "Player" + String.valueOf(packet.getClientId());

				this.personOverlay.removeItem(title);
				this.personOverlay.addItem(point, title);
			}
		} else if (message instanceof AppleUpdatePacket) {
			final AppleUpdatePacket packet = (AppleUpdatePacket) message;
			final String title = "Apple" + String.valueOf(packet.getAppleId());
			if (packet.getStatus()) {
				final GeoPoint point = new GeoPoint(packet.getLocation().getLatitude(), packet.getLocation()
						.getLongitude());
				this.appleOverlay.addItem(point, title);
			} else {
				this.appleOverlay.removeItem(title);
			}
			LOGGER.info("Apple " + String.valueOf(packet.getAppleId()) + " updated.");
		} else if (message instanceof QuestUpdatePacket) {
			final QuestUpdatePacket packet = (QuestUpdatePacket) message;
			final String title = "Quest" + String.valueOf(packet.getQuestId());
			if (packet.getStatus()) {
				final GeoPoint point = new GeoPoint(packet.getLocation().getLatitude(), packet.getLocation()
						.getLongitude());
				this.questOverlay.addItem(point, title);
			} else {
				this.questOverlay.removeItem(title);
			}
			LOGGER.info("Quest " + String.valueOf(packet.getQuestId()) + " updated.");
		}
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
		this.pathOverlay.addPoint(point);

		final ru.pinkponies.protocol.Location loc = new ru.pinkponies.protocol.Location(longitude, latitude, altitude);
		final PlayerUpdatePacket packet = new PlayerUpdatePacket(this.myId, loc);
		this.networkingService.sendPacket(packet);

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
}
