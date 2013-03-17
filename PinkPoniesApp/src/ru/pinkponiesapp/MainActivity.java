package ru.pinkponiesapp;

import java.lang.ref.WeakReference;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	try {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_main);
	        
	        textView = (TextView)findViewById(R.id.text_view);
	        textView.setMovementMethod(new ScrollingMovementMethod());
	        
	        editText = (EditText)findViewById(R.id.edit_message);
	        
	        messageHandler = new MessageHandler(this);
	        
	        networkingThread = new NetworkingThread(this);
	        networkingThread.start();
	        
	        printMessage("Initialized!");
    	} catch (Exception e) {
    		e.printStackTrace();
    		printMessage("Exception: " + e.getMessage());
        }
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