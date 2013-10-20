/**
 * Copyright (c) 2013 Alexander Konovalov, Andrey Konovalov, Sergey Voronov, Vitaly Malyshev. All
 * rights reserved. Use of this source code is governed by a BSD-style license that can be found in
 * the LICENSE file.
 */

package ru.pinkponies.app;

import java.net.InetSocketAddress;
import java.util.logging.Logger;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import ru.pinkponies.app.net.NetworkListener;
import ru.pinkponies.app.net.NetworkingService;
import ru.pinkponies.protocol.AppleUpdatePacket;
import ru.pinkponies.protocol.ClientOptionsPacket;
import ru.pinkponies.protocol.LoginPacket;
import ru.pinkponies.protocol.PlayerUpdatePacket;
import ru.pinkponies.protocol.QuestActionPacket;
import ru.pinkponies.protocol.QuestUpdatePacket;
import ru.pinkponies.protocol.SayPacket;

/**
 * The main activity class.
 */
public final class MainActivity extends Activity implements LocationListener, NetworkListener, Callback {
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
	 * This value is used when the identifier is not yet defined.
	 */
	private static final long BAD_ID = -1;

	/**
	 * The size of objects on the map.
	 */
	private static final double APPLE_RADIUS = 10.0;
	private static final double QUEST_RADIUS = 30.0;

	/**
	 * The default server IP.
	 */
	private static final String SERVER_IP = "192.168.1.33";

	/**
	 * The default server port.
	 */
	private static final int SERVER_PORT = 4264;

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

	private LocationManager locationManager;
	private boolean isLocationChanchedFirstTime = true;

	private GoogleMap map;

	private MapOverlay playersOverlay;
	private MapOverlay applesOverlay;
	private MapOverlay questsOverlay;

	private long playerId = BAD_ID;
	private long availableQuestId = BAD_ID;
	private long acceptedQuestId = BAD_ID;

	private String login;
	private String password;

	private TextView conn_textview;
	final Handler threadHandler = new Handler(this);

	// private TextOverlay textOverlay;

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

		this.login = this.getIntent().getStringExtra("login");
		this.password = this.getIntent().getStringExtra("password");

		LOGGER.info("onCreate " + this.hashCode());

		this.setContentView(R.layout.activity_main);

		this.locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		// Updates requested after the networking service is connected.

		this.map = ((MapFragment) this.getFragmentManager().findFragmentById(R.id.map)).getMap();
		this.map.setMyLocationEnabled(true);

		this.playersOverlay = new MapOverlay(this.map);
		this.applesOverlay = new MapOverlay(this.map);
		this.questsOverlay = new MapOverlay(this.map);

		LOGGER.info("Starting service");
		this.startService(new Intent(this, NetworkingService.class));
		this.bindService(new Intent(this, NetworkingService.class), this.networkingServiceConnection,
				Context.BIND_AUTO_CREATE);

		((Button) this.findViewById(R.id.join_button)).setEnabled(false);
		((Button) this.findViewById(R.id.start_button)).setEnabled(false);
		((Button) this.findViewById(R.id.leave_button)).setEnabled(false);

		this.conn_textview = (TextView) this.findViewById(R.id.conn_state_textview);

		LOGGER.info("Initialized.");

	}

	/**
	 * Called when the activity will start interacting with the user.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		LOGGER.info("onResume " + this.hashCode());
	}

	/**
	 * Called when the system is about to start resuming a previous activity.
	 */
	@Override
	protected void onPause() {
		super.onPause();
		LOGGER.info("onPause");
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
		super.onRestoreInstanceState(outState);
		LOGGER.info("onRestoreInstanceState " + this.hashCode());
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
		LOGGER.info("onNetworkingServiceConnected");

		this.networkingService.addListener(this);

		if (this.networkingService.getState() != NetworkingService.State.CONNECTED) {
			this.networkingService.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT));
		}

		this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_UPDATE_MIN_DELAY,
				LOCATION_UPDATE_MIN_DISTANCE, this);
		this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_UPDATE_MIN_DELAY,
				LOCATION_UPDATE_MIN_DISTANCE, this);
	}

	/**
	 * Called when there is a new message from the networking service.
	 */
	@Override
	public void onMessage(final Object message) {
		LOGGER.info(message.toString());

		if (message.equals("failed")) {
			this.showMessageBox("Socket exception.", null);
		} else if (message instanceof ClientOptionsPacket) {
			final ClientOptionsPacket packet = (ClientOptionsPacket) message;
			this.onClientOptonsPacket(packet);
		} else if (message instanceof SayPacket) {
			final SayPacket packet = (SayPacket) message;
			this.onSayPacket(packet);
		} else if (message instanceof PlayerUpdatePacket) {
			final PlayerUpdatePacket packet = (PlayerUpdatePacket) message;
			this.onPlayerUpdatePacket(packet);
		} else if (message instanceof AppleUpdatePacket) {
			final AppleUpdatePacket packet = (AppleUpdatePacket) message;
			this.onAppleUpdatePacket(packet);
		} else if (message instanceof QuestUpdatePacket) {
			final QuestUpdatePacket packet = (QuestUpdatePacket) message;
			this.onQuestUpdatePacket(packet);
		}
	}

	private void onClientOptonsPacket(final ClientOptionsPacket packet) {
		this.playerId = packet.clientId;
		LoginPacket loginPacket = new LoginPacket(this.playerId, this.login, this.password);
		this.networkingService.sendPacket(loginPacket);
		this.printIntoConnStateTextView("Everything's fine");
	}

	private void onSayPacket(final SayPacket packet) {
		LOGGER.info(packet.toString());
	}

	private void onPlayerUpdatePacket(final PlayerUpdatePacket packet) {
		if (this.playerId != BAD_ID && packet.playerId != this.playerId) {
			final LatLng location = new LatLng(packet.location.latitude * 180 / Math.PI, packet.location.longitude
					* 180 / Math.PI);
			final String name = packet.playerName;
			this.playersOverlay.removeMarker(name);
			this.playersOverlay.addMarker(name, location,
					BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
		}
	}

	private void onAppleUpdatePacket(final AppleUpdatePacket packet) {
		final String name = "Apple" + String.valueOf(packet.appleId);
		if (packet.status) {
			final LatLng location = new LatLng(packet.location.latitude * 180 / Math.PI, packet.location.longitude
					* 180 / Math.PI);
			this.applesOverlay.addCircle(name, location, APPLE_RADIUS, Color.RED);
		} else {
			this.applesOverlay.removeCircle(name);
		}
		LOGGER.info("Apple " + String.valueOf(packet.appleId) + " updated.");
	}

	private void onQuestUpdatePacket(final QuestUpdatePacket packet) {
		final String name = "Quest" + String.valueOf(packet.questId);
		if (packet.status == QuestUpdatePacket.Status.APPEARED) {
			final LatLng location = new LatLng(packet.location.latitude * 180 / Math.PI, packet.location.longitude
					* 180 / Math.PI);
			this.questsOverlay.addCircle(name, location, QUEST_RADIUS, Color.GREEN);
		} else if (packet.status == QuestUpdatePacket.Status.DISAPPEARED) {
			if (this.availableQuestId == packet.questId) {
				this.availableQuestId = BAD_ID;
				((Button) this.findViewById(R.id.join_button)).setEnabled(false);
			}
			this.questsOverlay.removeCircle(name);
		} else if (packet.status == QuestUpdatePacket.Status.AVAILABLE) {
			this.availableQuestId = packet.questId;
			((Button) this.findViewById(R.id.join_button)).setEnabled(true);
		} else if (packet.status == QuestUpdatePacket.Status.UNAVAILABLE) {
			if (packet.questId == this.availableQuestId) {
				this.availableQuestId = BAD_ID;
				((Button) this.findViewById(R.id.join_button)).setEnabled(false);
			}
		} else if (packet.status == QuestUpdatePacket.Status.ACCEPTED) {
			this.acceptedQuestId = packet.questId;
			((Button) this.findViewById(R.id.join_button)).setEnabled(false);
			((Button) this.findViewById(R.id.start_button)).setEnabled(true);
			((Button) this.findViewById(R.id.leave_button)).setEnabled(true);
			final LatLng location = new LatLng(packet.location.latitude * 180 / Math.PI, packet.location.longitude
					* 180 / Math.PI);
			this.questsOverlay.addCircle(name + "_accepted", location, QUEST_RADIUS, Color.BLUE);
		} else if (packet.status == QuestUpdatePacket.Status.DECLINED) {
			this.acceptedQuestId = BAD_ID;
			((Button) this.findViewById(R.id.start_button)).setEnabled(false);
			((Button) this.findViewById(R.id.leave_button)).setEnabled(false);
			this.questsOverlay.removeCircle(name + "_accepted");
		} else if (packet.status == QuestUpdatePacket.Status.STARTED) {
			((Button) this.findViewById(R.id.start_button)).setEnabled(false);
			this.questsOverlay.removeCircle(name + "_accepted");
		}
		LOGGER.info("Quest " + String.valueOf(packet.questId) + " updated.");
	}

	public void goToLoginActivity() {
		final Intent intent = new Intent(MainActivity.this, LoginActivity.class);
		this.startActivity(intent);
		this.onDestroy();
	}

	@Override
	public void onLocationChanged(final Location location) {
		if (this.playerId == BAD_ID) {
			return;
		}

		final double longitude = location.getLongitude();
		final double latitude = location.getLatitude();
		final double altitude = location.getAltitude();

		if (this.isLocationChanchedFirstTime) {
			LatLng latLng = new LatLng(latitude, longitude);
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
			this.map.animateCamera(cameraUpdate);
			this.isLocationChanchedFirstTime = false;
		}

		final ru.pinkponies.protocol.Location loc = new ru.pinkponies.protocol.Location(longitude / 180 * Math.PI,
				latitude / 180 * Math.PI, altitude);
		final PlayerUpdatePacket packet = new PlayerUpdatePacket(this.playerId, this.login, loc);
		this.networkingService.sendPacket(packet);

		MainActivity.LOGGER.info("Location updated.");
	}

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

	public void onJoinButtonClick(final View view) {
		if (this.availableQuestId == BAD_ID) {
			LOGGER.info("Can't join, bad id!");
			return;
		}
		QuestActionPacket packet = new QuestActionPacket(this.availableQuestId, QuestActionPacket.Action.JOIN);
		this.networkingService.sendPacket(packet);
	}

	public void onStartButtonClick(final View view) {
		if (this.acceptedQuestId == BAD_ID) {
			LOGGER.info("Can't start, bad id!");
			return;
		}
		QuestActionPacket packet = new QuestActionPacket(this.acceptedQuestId, QuestActionPacket.Action.START);
		this.networkingService.sendPacket(packet);
	}

	public void onLeaveButtonClick(final View view) {
		if (this.acceptedQuestId == BAD_ID) {
			LOGGER.info("Can't leave, bad id!");
			return;
		}
		QuestActionPacket packet = new QuestActionPacket(this.acceptedQuestId, QuestActionPacket.Action.LEAVE);
		this.networkingService.sendPacket(packet);
	}

	@Override
	public void onProviderDisabled(final String provider) {
	}

	@Override
	public void onProviderEnabled(final String provider) {
	}

	@Override
	public void onStatusChanged(final String provider, final int status, final Bundle extras) {
	}

	public void printIntoConnStateTextView(final String string) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (MainActivity.this.conn_textview == null) {
					LOGGER.info("#con_tv");
				}
				MainActivity.this.conn_textview.setText(string);

			}
		});
	}

	@Override
	public boolean handleMessage(final Message arg0) {
		// TODO Auto-generated method stub
		return false;
	}
}
