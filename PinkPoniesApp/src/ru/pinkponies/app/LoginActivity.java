package ru.pinkponies.app;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.osmdroid.views.overlay.MyLocationOverlay;

import ru.pinkponiesapp.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity {   
	
	private final static Logger logger = Logger.getLogger(LoginActivity.class.getName());
		   	
    private TextView textView;
    private EditText editText;
    
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
	        	        
	      //  logger.info("LoginActivity:onCreate");
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
    
    public void onLoginClick(View view) {
        login = editText.getText().toString();
        editText.setText("");
  //      sendMessageToNetworkingThread(login);
        goToMainActivity(view);
    }
    
    public void goToMainActivity(View view)
    {
    	Intent intent = new Intent(LoginActivity.this, MainActivity.class);
    	intent.putExtra("login", login);
        startActivity(intent);
  	};

}