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
	/**
	 * The class wide logger.
	 */
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(NetworkingService.class.getName());

	/**
	 * The networking thread in which most of the IO operations are performed. They are done in a
	 * separate thread since on Android you can not perform IO operations in the UI thread.
	 */
	private final NetworkingThread networkingThread = new NetworkingThread(this);

	/**
	 * The message handler for this service. It is used to receive messages from networking thread.
	 */
	private final Handler messageHandler = new MessageHandler(this);

	/**
	 * The binder which allows binding activity to get a reference to the networking service.
	 */
	private final LocalBinder binder = new LocalBinder(this);

	/**
	 * The array of network listeners.
	 */
	private final List<NetworkListener> listeners = new ArrayList<NetworkListener>();

	/**
	 * Adds a new listener.
	 * 
	 * @param listener
	 *            the listener
	 */
	public void addListener(final NetworkListener listener) {
		this.listeners.add(listener);
	}

	/**
	 * Removes the listener.
	 * 
	 * @param listener
	 *            the listener
	 */
	public void removeListener(final NetworkListener listener) {
		this.listeners.remove(listener);
	}

	/**
	 * Returns the message handler for this networking thread.
	 * 
	 * @return the message handler for this networking thread
	 */
	public Handler getMessageHandler() {
		return this.messageHandler;
	}

	/**
	 * Called when there is a message from networking thread.
	 * 
	 * @param message
	 *            the message
	 */
	private void onMessageFromNetworkingThread(final Object message) {
		for (final NetworkListener listener : this.listeners) {
			listener.onMessage(message);
		}
	}

	/**
	 * Sends the message to the networking thread.
	 * 
	 * @param message
	 *            the message
	 */
	public void sendMessage(final Object message) {
		Message msg = this.networkingThread.getMessageHandler().obtainMessage();
		msg.obj = message;
		this.networkingThread.getMessageHandler().sendMessage(msg);
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
		super.onDestroy();
	}

	@Override
	public IBinder onBind(final Intent intent) {
		return this.binder;
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

	/**
	 * This binder provides the activity which binds this service with access to the service itself
	 * via reference.
	 * 
	 * @author alex
	 * 
	 */
	public static final class LocalBinder extends Binder {
		/**
		 * The weak reference to the networking service.
		 */
		private final WeakReference<NetworkingService> service;

		/**
		 * Creates a new local binder.
		 * 
		 * @param networkingService
		 *            the networking service
		 */
		LocalBinder(final NetworkingService networkingService) {
			this.service = new WeakReference<NetworkingService>(networkingService);
		}

		/**
		 * Returns the reference to the networking service.
		 * 
		 * @return the networking service
		 */
		public NetworkingService getService() {
			return this.service.get();
		}
	}
}
