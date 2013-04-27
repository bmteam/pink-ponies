/**
 * Copyright (c) 2013 Alexander Konovalov, Andrey Konovalov, Sergey Voronov, Vitaly Malyshev. All
 * rights reserved. Use of this source code is governed by a BSD-style license that can be found in
 * the LICENSE file.
 */

package ru.pinkponies.app.net;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

public class NetworkingService extends Service {
	private static final Logger LOGGER = Logger.getLogger(NetworkingService.class.getName());

	private final NetworkingThread networkingThread = new NetworkingThread(this);
	private final Handler messageHandler = new MessageHandler(this);
	private final LocalBinder binder = new LocalBinder(this);
	private final List<NetworkListener> listeners = new ArrayList<NetworkListener>();

	public void addListener(final NetworkListener listener) {
		this.listeners.add(listener);
	}

	public void removeListener(final NetworkListener listener) {
		this.listeners.remove(listener);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		this.networkingThread.start();
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public IBinder onBind(final Intent intent) {
		return this.binder;
	}

	public Handler getMessageHandler() {
		return this.messageHandler;
	}

	private void onMessageFromNetworkingThread(final Object message) {
		for (final NetworkListener listener : this.listeners) {
			listener.onMessage(message);
		}
	}

	public void sendMessage(final Object message) {
		Message msg = this.networkingThread.getMessageHandler().obtainMessage();
		msg.obj = message;
		this.networkingThread.getMessageHandler().sendMessage(msg);
	}

	/**
	 * A message handler class for the networking service.
	 */
	public static final class MessageHandler extends Handler {
		/**
		 * The weak reference to the networking service.
		 */
		private final WeakReference<NetworkingService> service;

		MessageHandler(final NetworkingService networkingService) {
			this.service = new WeakReference<NetworkingService>(networkingService);
		}

		@Override
		public void handleMessage(final Message msg) {
			this.service.get().onMessageFromNetworkingThread(msg.obj);
		}
	}

	public static final class LocalBinder extends Binder {
		private final WeakReference<NetworkingService> service;

		LocalBinder(final NetworkingService networkingService) {
			this.service = new WeakReference<NetworkingService>(networkingService);
		}

		public NetworkingService getService() {
			return this.service.get();
		}
	}
}
