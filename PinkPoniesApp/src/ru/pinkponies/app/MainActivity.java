package ru.pinkponies.app;

import java.lang.ref.WeakReference;
import java.util.logging.Logger;

import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;

import ru.pinkponiesapp.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
	private static final Logger logger = Logger.getLogger(MainActivity.class.getName());
    private TextView textView;
    private EditText editText;
     
    private LocationManager locationManager;
  
    public Handler messageHandler;
    
    private NetworkingThread networkingThread;
    
    private MapController mapController;
    
    private String login = "";
    
    MyLocationOverlay myLocationOverlay = null;
    
    private MapView mapView;
   
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	logger.info("onCreate");
		try {    		
		    super.onCreate(savedInstanceState);
		    
		    Intent intent = getIntent();
		    
		    Bundle extras = intent.getExtras();
		    login = extras.getString("login");
		    
		    		
		    setContentView(R.layout.activity_main);
		
        	textView = (TextView)findViewById(R.id.text_view);
	        textView.setMovementMethod(new ScrollingMovementMethod());
	        
	        editText = (EditText)findViewById(R.id.edit_message);
		    
		    editText = (EditText)findViewById(R.id.edit_message);
		    
		    
		    networkingThread = new NetworkingThread(this);
	        networkingThread.start();
		    
		   
			mapView = (MapView) findViewById(R.id.MainActivityMapview);
        	mapView.setBuiltInZoomControls(true);
        	mapView.setMultiTouchControls(true);   
        	mapController = mapView.getController();
        	
        	
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

        	logger.info("MainActivity:onCreate");
		    printMessage("Initialized! Your login: " + login);
		} catch (Exception e) {
			e.printStackTrace();
			printMessage("Exception: " + e.getMessage());
		}
    }
    
    @Override
    protected void onResume() {    	
    	logger.info("MainActivity:onResume");
     // TODO Auto-generated method stub
    	super.onResume();
    	myLocationOverlay.enableMyLocation();
    	myLocationOverlay.enableFollowLocation();
    }
    
    @Override
    protected void onPause() {
    	logger.info("MainActivity:onPause");
     // TODO Auto-generated method stub
    	super.onPause();
    	myLocationOverlay.disableMyLocation();
    	myLocationOverlay.disableFollowLocation();
    }
    
    @Override
    protected void onDestroy() {
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
    
    private void printMessage(String message) {
    	textView.append(message + "\n");
    }
    
    public void onLogoutClick(View view) {
    	goToLoginActivity(view);
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
    public void goToLoginActivity(View view)
    {
    	Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        this.onDestroy();
    }
     
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
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