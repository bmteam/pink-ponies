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

	public final static int REGISTER_CLIENT = 1;
	public final static int UNREGISTER_CLIENT = 2;
	public final static int CONNECT = 3;
	public final static int DISCONNECT = 4;
	public final static int SEND = 5;

	public final static int CONNECTED = 6;
	public final static int DISCONNECTED = 7;
	public final static int IO_ERROR = 8;
	public final static int PACKET = 9;

	private final NetworkingThread networkingThread = new NetworkingThread(this);
	private final Handler messageHandler = new MessageHandler(this);
	private final LocalBinder binder = new LocalBinder(this);
	private final List<NetworkListener> listeners = new ArrayList<NetworkListener>();

	@Override
	public void onCreate() {
		super.onCreate();

		LOGGER.info("Initializing networking service.");
		this.networkingThread.start();
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {
		LOGGER.info("Service started");
		// TODO Auto-generated method stub
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
		LOGGER.info("Broadcasting message : " + message.toString());
		for (final NetworkListener listener : this.listeners) {
			listener.onMessage(message);
		}
	}

	public void sendMessage(final Object message) {
		Message msg = this.networkingThread.getMessageHandler().obtainMessage();
		msg.obj = message;
		this.networkingThread.getMessageHandler().sendMessage(msg);
	}

	public static final class MessageHandler extends Handler {
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

	public void addListener(final NetworkListener listener) {
		LOGGER.info("Added listener");
		this.listeners.add(listener);
	}

	public void removeListener(final NetworkListener listener) {
		this.listeners.remove(listener);
		LOGGER.info("Removed listener");
	}
}
