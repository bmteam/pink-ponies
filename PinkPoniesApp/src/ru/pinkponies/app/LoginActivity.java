package ru.pinkponies.app;

import java.util.logging.Logger;

import ru.pinkponies.app.R;
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

	private EditText loginEditBox, passwordEditBox;

	private String login = "default";
	private String password = "default";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		logger.info("MainActivity:Initializing...");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		loginEditBox = (EditText) findViewById(R.id.login);
		passwordEditBox = (EditText) findViewById(R.id.password);

		logger.info("MainActivity:Initialized!");
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
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
		login = loginEditBox.getText().toString();
		password = passwordEditBox.getText().toString();

		goToMainActivity();
	}

	public void goToMainActivity() {
		Intent intent = new Intent(LoginActivity.this, MainActivity.class);

		intent.putExtra("login", login);
		intent.putExtra("password", password);

		startActivity(intent);
		LoginActivity.this.finish();
	};

}