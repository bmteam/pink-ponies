package ru.pinkponies.app;

import java.lang.ref.WeakReference;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.PathOverlay;
import ru.pinkponies.protocol.LocationUpdatePacket;
import ru.pinkponiesapp.R;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity implements LocationListener {
    private TextView textView;
    private EditText editText;  
    
    private LocationManager locationManager;    
    private NetworkingThread networkingThread;
    
    public Handler messageHandler;
    MyLocationOverlay myLocationOverlay = null;
    MyItemizedOverlay myPersonOverlay = null;
    MyItemizedOverlay myAppleOverlay = null;
    GeoPoint myPoint = new GeoPoint(5592*10000, 3751*10000);	
    PathOverlay myPath = null; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	try {    		
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_main);

			final MapView mapView = (MapView) findViewById(R.id.mapview);
        	mapView.setMultiTouchControls(true);        
        	final  MapController mapController = mapView.getController();
        	mapController.setZoom(13);

	        textView = (TextView)findViewById(R.id.text_view);
	        textView.setMovementMethod(new ScrollingMovementMethod());
	        
	        editText = (EditText)findViewById(R.id.edit_message);
	        
	        messageHandler = new MessageHandler(this);
	        
	        networkingThread = new NetworkingThread(this);
	        networkingThread.start();

			myLocationOverlay = new MyLocationOverlay(this, mapView);
        	mapView.getOverlays().add(myLocationOverlay);
        	myPath = new PathOverlay(Color.GREEN, this);
        	mapView.getOverlays().add(myPath);
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
	        
	        
	        // add Person overlay
	        Drawable marker=getResources().getDrawable(R.drawable.person);
	        int markerWidth = marker.getIntrinsicWidth();
	        int markerHeight = marker.getIntrinsicHeight();
	        marker.setBounds(0, markerHeight, markerWidth, 0);
	         
	        ResourceProxy resourceProxy = new DefaultResourceProxyImpl(getApplicationContext());
	         
	        myPersonOverlay = new MyItemizedOverlay(marker, resourceProxy);
	        mapView.getOverlays().add(myPersonOverlay);
	        
	        //add Apple overlay
	        marker=getResources().getDrawable(R.drawable.apple);
	        markerWidth = marker.getIntrinsicWidth();
	        markerHeight = marker.getIntrinsicHeight();
	        marker.setBounds(0, markerHeight, markerWidth, 0);
	         
	        resourceProxy = new DefaultResourceProxyImpl(getApplicationContext());
	         
	        myAppleOverlay = new MyItemizedOverlay(marker, resourceProxy);
	        mapView.getOverlays().add(myAppleOverlay);
	       	         
	        
	        // player 1	                
	        myPersonOverlay.addItem(myPoint, "player1", "player1");
	        // apples
	        GeoPoint applePoint1 = new 
	        		GeoPoint(myPoint.getLatitudeE6() + 20000,
					myPoint.getLongitudeE6() + 10000);
	        myAppleOverlay.addItem(applePoint1, "Apple1", "Apple1");
	        GeoPoint applePoint2 = new 
	        		GeoPoint(myPoint.getLatitudeE6() + 14000,
					myPoint.getLongitudeE6() - 10000);
	        myAppleOverlay.addItem(applePoint2, "Apple2", "Apple2");
	        GeoPoint applePoint3 = new 
	        		GeoPoint(myPoint.getLatitudeE6() - 7000,
					myPoint.getLongitudeE6() + 10000);
	        myAppleOverlay.addItem(applePoint3, "Apple3", "Apple3");
	        
	       //path
	       final PathOverlay p1Path = new PathOverlay(Color.RED, this);
	       p1Path.addPoint(applePoint1);
	       p1Path.addPoint(applePoint2);
	       p1Path.addPoint(applePoint3);
	       mapView.getOverlays().add(p1Path);
	       
	       // magic button 
	       final Button button = (Button) findViewById(R.id.button1);
	       
	        button.setOnClickListener(new Button.OnClickListener() {
	            public void onClick(View v){
	            	myPoint = new 
	            			GeoPoint(myPoint.getLatitudeE6() + 10000,
	            					myPoint.getLongitudeE6() + 10000);
	            	myPersonOverlay.removeItem("player1");	            	
	            	myPersonOverlay.addItem(myPoint, "player1", "player1");
	            	p1Path.addPoint(myPoint);
	            	mapView.invalidate();
	            }
	        }); 
	        
	        
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
		GeoPoint point = new GeoPoint(latitude, longitude);		
		myPath.addPoint(point);
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