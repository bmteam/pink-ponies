package ru.pinkponies.app;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;

import ru.pinkponies.protocol.LocationUpdatePacket;
import ru.pinkponiesapp.R;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity implements LocationListener {   
	private int SERVICE_DELAY = 2000;
	
	private final static Logger logger = Logger.getLogger(MainActivity.class.getName());
	
    private TextView textView;
    private EditText editText;
    
    private LocationManager locationManager;
    
    private NetworkingThread networkingThread;
    
    public Handler messageHandler;
    MyLocationOverlay myLocationOverlay = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	try {
    		logger.info("Initializing...");
    		
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_main);

			final MapView mapView = (MapView) findViewById(R.id.mapview);
        	mapView.setBuiltInZoomControls(true);
        	mapView.setMultiTouchControls(true);        

	        textView = (TextView)findViewById(R.id.text_view);
	        textView.setMovementMethod(new ScrollingMovementMethod());
	        
	        editText = (EditText)findViewById(R.id.edit_message);
	        
	        messageHandler = new MessageHandler(this);
	        
	        networkingThread = new NetworkingThread(this);
	        networkingThread.start();

			myLocationOverlay = new MyLocationOverlay(this, mapView);
        	mapView.getOverlays().add(myLocationOverlay);
        	mapView.postInvalidate();
        
		    myLocationOverlay.runOnFirstFix(new Runnable() {
		    	public void run() {
		    		mapView.getController().animateTo(myLocationOverlay.getMyLocation());
		        }
		    });
		    
		    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    		
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, this);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);

            logger.info("Initialized!");
    	} catch (Exception e) {
    		logger.log(Level.SEVERE, "Exception", e);
        }
    }
    
    @Override
    protected void onResume() {
	 // TODO Auto-generated method stub
	 super.onResume();
	 myLocationOverlay.enableMyLocation();
	 myLocationOverlay.enableFollowLocation();
    }
    
    @Override
    protected void onPause() {
     // TODO Auto-generated method stub
     super.onPause();
     myLocationOverlay.disableMyLocation();
     myLocationOverlay.disableFollowLocation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    public void onSendClick(View view) {
        String message = editText.getText().toString();
        editText.setText("");
        sendMessageToNetworkingThread(message);
    }
    
    private void onMessageFromNetworkingThread(String message) {
        logger.info("NT: " + message);
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
        }
    }

    private void sendMessageToNetworkingThread(String message) {
        Message msg = networkingThread.messageHandler.obtainMessage();
        msg.obj = message;
        networkingThread.messageHandler.sendMessage(msg);
    }
    private void sendMessageToNetworkingThread(Object message) {
        Message msg = networkingThread.messageHandler.obtainMessage();
        msg.obj = message;
        networkingThread.messageHandler.sendMessage(msg);
    }
    
    static public class MessageHandler extends Handler {
        private WeakReference<MainActivity> activity;
        
        MessageHandler(MainActivity mainActivity) {
            activity = new WeakReference<MainActivity>(mainActivity);
        }
        
        @Override
        public void handleMessage(Message msg) {
            activity.get().onMessageFromNetworkingThread((String) msg.obj);
        }
    }

	@Override
	public void onLocationChanged(Location location) {
		String clientID = Build.DISPLAY;
		double longitude = location.getLongitude();
		double latitude = location.getLatitude();
		double altitude = location.getAltitude();
		sendMessageToNetworkingThread(new LocationUpdatePacket(clientID, longitude, latitude, altitude));
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
		
	};

}