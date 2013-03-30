package ru.pinkponies.app;

import java.lang.ref.WeakReference;
import java.util.logging.Logger;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;

import ru.pinkponies.app.MainActivity.MessageHandler;
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


public class SecondActivity extends Activity implements LocationListener {
	private static final Logger logger = Logger.getLogger(SecondActivity.class.getName());
    private TextView textView;
    private EditText editText;
     
    private LocationManager locationManager;
  
    
    public Handler messageHandler;
    
    MyLocationOverlay myLocationOverlay = null;
    
   
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	logger.info("onCreate");
		try {    		
		    super.onCreate(savedInstanceState);
		    setContentView(R.layout.activity_second);
		
        	textView = (TextView)findViewById(R.id.text_view);
	        textView.setMovementMethod(new ScrollingMovementMethod());
	        
	        editText = (EditText)findViewById(R.id.edit_message);
	        
	        messageHandler = new MessageHandler(this);
		    
		    editText = (EditText)findViewById(R.id.edit_message);
		    
		    messageHandler = new MessageHandler(this);
		    
		        
			final MapView mapView = (MapView) findViewById(R.id.SecondActivityMapview);
        	mapView.setBuiltInZoomControls(true);
        	mapView.setMultiTouchControls(true);   
        	
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
		} catch (Exception e) {
			e.printStackTrace();
			printMessage("Exception: " + e.getMessage());
		}
    }
    
    @Override
    protected void onResume() {
    	logger.info("onResume");
     // TODO Auto-generated method stub
    	super.onResume();
    	myLocationOverlay.enableMyLocation();
    	myLocationOverlay.enableFollowLocation();
    }
    
    @Override
    protected void onPause() {
    	logger.info("onPause");
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
   //     sendMessageToNetworkingThread(message);
    }
 /*   
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
   */ 
    public void goToMainActivity(View view)
    {
    	Intent intent = new Intent(SecondActivity.this, MainActivity.class);
        startActivity(intent);
    }
    
    static public class MessageHandler extends Handler {
        private WeakReference<SecondActivity> activity;
        
        MessageHandler(SecondActivity secondActivity) {
            activity = new WeakReference<SecondActivity>(secondActivity);
        }
    }
 
/*		@Override
        public void handleMessage(Message msg) {
            activity.get().onMessageFromNetworkingThread((String)msg.obj);
        }
   	}
*/


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