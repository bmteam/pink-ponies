package ru.pinkponies.app;

import java.util.logging.Logger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class LoginActivity extends Activity {
	private final static Logger logger = Logger.getLogger(LoginActivity.class.getName());

	private EditText loginEditBox, passwordEditBox;

	private String login = "default";
	private String password = "default";

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		logger.info("LoginActivity::Initializing...");

		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_login);

		this.loginEditBox = (EditText) this.findViewById(R.id.login);
		this.passwordEditBox = (EditText) this.findViewById(R.id.password);

		logger.info("LoginActivity::Initialized!");
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
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			// do whatever
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		this.getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onLoginClick(final View view) {
		this.login = this.loginEditBox.getText().toString();
		this.password = this.passwordEditBox.getText().toString();

		this.goToMainActivity();
	}

	public void goToMainActivity() {
		Intent intent = new Intent(LoginActivity.this, MainActivity.class);

		intent.putExtra("login", this.login);
		intent.putExtra("password", this.password);

		this.startActivity(intent);
		LoginActivity.this.finish();
	};

}
