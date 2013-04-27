/**
 * Copyright (c) 2013 Alexander Konovalov, Andrey Konovalov, Sergey Voronov, Vitaly Malyshev. All
 * rights reserved. Use of this source code is governed by a BSD-style license that can be found in
 * the LICENSE file.
 */

package ru.pinkponies.app;

import java.util.logging.Logger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

/**
 * An activity with login form.
 * 
 */
public class LoginActivity extends Activity {
	/**
	 * The class wide logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(LoginActivity.class.getName());

	/**
	 * The login edit box.
	 */
	private EditText loginEditBox;

	/**
	 * The password edit box.
	 */
	private EditText passwordEditBox;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		LOGGER.info("LoginActivity::Initializing...");

		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_login);

		this.loginEditBox = (EditText) this.findViewById(R.id.login);
		this.passwordEditBox = (EditText) this.findViewById(R.id.password);

		LOGGER.info("LoginActivity::Initialized!");
	}

	/**
	 * Called when login button is clicked.
	 * 
	 * @param view
	 *            the view that was clicked.
	 */
	public void onLoginClick(final View view) {
		final String login = this.loginEditBox.getText().toString();
		final String password = this.passwordEditBox.getText().toString();

		this.goToMainActivity(login, password);
	}

	/**
	 * Opens up a new main activity, passing it login and password.
	 * 
	 * @param login
	 *            the login
	 * @param password
	 *            the password
	 */
	public void goToMainActivity(final String login, final String password) {
		final Intent intent = new Intent(LoginActivity.this, MainActivity.class);

		intent.putExtra("login", login);
		intent.putExtra("password", password);

		this.startActivity(intent);
		LoginActivity.this.finish();
	};

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
}
