package ru.pinkponiesapp;

import java.lang.ref.WeakReference;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
    private TextView textView;
    private EditText editText;
    
    private NetworkingThread networkingThread;
    
    public Handler messageHandler;
    MyLocationOverlay myLocationOverlay = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	try {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_main);

			final MapView mapView = (MapView) findViewById(R.id.mapview);
        	mapView.setBuiltInZoomControls(true);
        	mapView.setMultiTouchControls(true);
        
        	//gpsTracker = new GPSTracker(this);	        

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

	        printMessage("Initialized!");

			//printMessage(Boolean.toString(gpsTracker.canGetLocation()));
        	//printMessage(Double.toString(gpsTracker.getLatitude()));
        	//printMessage(Double.toString(gpsTracker.getLongitude()));
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
        }
    }

    private void sendMessageToNetworkingThread(String message) {
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
    };

}