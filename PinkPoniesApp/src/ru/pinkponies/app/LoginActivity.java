package ru.pinkponies.app;

import java.util.logging.Level;
import java.util.logging.Logger;

import ru.pinkponiesapp.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class LoginActivity extends Activity {

	private final static Logger logger = Logger.getLogger(LoginActivity.class
			.getName());

	private EditText editLogin, editPassword;

	private String login = "admin";
	private String password = "admin";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			logger.info("Initializing...");

			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_login);

			editLogin = (EditText) findViewById(R.id.login);
			editPassword = (EditText) findViewById(R.id.password);

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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			// do whatever
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onLoginClick(View view) {
		logger.info("LoginActivity:onLoginClick");
		login = editLogin.getText().toString();
		password = editPassword.getText().toString();
		goToMainActivity(view);
	}

	public void goToMainActivity(View view) {
		Intent intent = new Intent(LoginActivity.this, MainActivity.class);

		intent.putExtra("login", login);
		intent.putExtra("password", password);

		startActivity(intent);
		LoginActivity.this.finish();
	};

}