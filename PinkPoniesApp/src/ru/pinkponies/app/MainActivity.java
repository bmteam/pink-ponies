package ru.pinkponies.app;

import java.lang.ref.WeakReference;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;

import ru.pinkponies.protocol.LocationUpdatePacket;
import ru.pinkponiesapp.R;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity implements LocationListener {
    private TextView textView;
    private EditText editText;
    
    private LocationManager locationManager;
    
    private NetworkingThread networkingThread;
    
    public Handler messageHandler;
    MyLocationOverlay myLocationOverlay = null;
    MyItemizedOverlay myItemizedOverlay = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	try {    		
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

	        printMessage("Initialized!");
	        
	        Drawable marker=getResources().getDrawable(android.R.drawable.btn_plus);
	        int markerWidth = marker.getIntrinsicWidth();
	        int markerHeight = marker.getIntrinsicHeight();
	        marker.setBounds(0, markerHeight, markerWidth, 0);
	         
	        ResourceProxy resourceProxy = new DefaultResourceProxyImpl(getApplicationContext());
	         
	        myItemizedOverlay = new MyItemizedOverlay(marker, resourceProxy);
	        mapView.getOverlays().add(myItemizedOverlay);
	         
	        GeoPoint myPoint1 = new GeoPoint(0*1000000, 0*1000000);
	        myItemizedOverlay.addItem(myPoint1, "myPoint1", "myPoint1");
	        GeoPoint myPoint2 = new GeoPoint(50*1000000, 50*1000000);
	        myItemizedOverlay.addItem(myPoint2, "myPoint2", "myPoint2");
	        
	        
    	} catch (Exception e) {
    		e.printStackTrace();
    		printMessage("Exception: " + e.getMessage());
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
    
    private void printMessage(String message) {
    	textView.append(message + "\n");
    }
    
    public void onSendClick(View view) {
        String message = editText.getText().toString();
        editText.setText("");
        sendMessageToNetworkingThread(message);
    }
    
    private void onMessageFromNetworkingThread(String message) {
        printMessage("NT: " + message);
        if (message.equals("initialized")) {
        	sendMessageToNetworkingThread("connect");
        	sendMessageToNetworkingThread("service");
        	sendMessageToNetworkingThread("login");
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
            activity.get().onMessageFromNetworkingThread((String)msg.obj);
        }
    }

	@Override
	public void onLocationChanged(Location location) {
		double longitude = location.getLongitude();
		double latitude = location.getLatitude();
		double altitude = location.getAltitude();
		sendMessageToNetworkingThread(new LocationUpdatePacket(longitude, latitude, altitude));
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