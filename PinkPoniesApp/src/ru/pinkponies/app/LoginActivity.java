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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Intent;

public class LoginActivity extends Activity implements LocationListener {   
	private int SERVICE_DELAY = 1000;
	
	private final static Logger logger = Logger.getLogger(LoginActivity.class.getName());
		   	
    private TextView textView;
    private EditText editText;
    
    private NetworkingThread networkingThread;
    private String login = "admin";
    
    public Handler messageHandler;
    MyLocationOverlay myLocationOverlay = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	try {
    		logger.info("Initializing...");
    		
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_login);

	        textView = (TextView)findViewById(R.id.text_view);
	        textView.setMovementMethod(new ScrollingMovementMethod());
	        
	        editText = (EditText)findViewById(R.id.edit_message);
	        
	        messageHandler = new MessageHandler(this);
	        
	        networkingThread = new NetworkingThread(this);
	        networkingThread.start();


	        logger.info("LoginActivity:onCreate");
     	} catch (Exception e) {
    		logger.log(Level.SEVERE, "Exception", e);
        }
    }
    
    @Override
    protected void onResume() {
    	logger.info("LoginActivity:onResume");
    	// TODO Auto-generated method stub
		super.onResume();
		
    }
    
    @Override
    protected void onPause() {
    	logger.info("LoginActivity:onPause");
        // TODO Auto-generated method stub
		super.onPause();
	}

    @Override
    protected void onDestroy() {
    	logger.info("LoginActivity:onDestroy");
     // TODO Auto-generated method stub
    	super.onDestroy();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    
    
    private void printMessage(String message) {
    	textView.append(message + "\n");
    }
    
    public void onLoginClick(View view) {
        login = editText.getText().toString();
        editText.setText("");
        sendMessageToNetworkingThread(login);
        goToMainActivity(view);
    }
    
    public void goToMainActivity(View view)
    {
    	Intent intent = new Intent(LoginActivity.this, MainActivity.class);
    	intent.putExtra("login", login);
        startActivity(intent);
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
        private WeakReference<LoginActivity> activity;
        
        MessageHandler(LoginActivity mainActivity) {
            activity = new WeakReference<LoginActivity>(mainActivity);
        }
        
        @Override
        public void handleMessage(Message msg) {
            activity.get().onMessageFromNetworkingThread((String) msg.obj);
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