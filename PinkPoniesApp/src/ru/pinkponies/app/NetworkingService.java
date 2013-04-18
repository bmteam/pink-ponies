package ru.pinkponies.app;

import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

public class NetworkingService extends Service {

	private static final Logger LOGGER = Logger.getLogger(MainActivity.class.getName());

	private NetworkingThread networkingThread;
	private WeakReference<MainActivity> mainActivity;

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

	private final Handler messageHandler = new MessageHandler(this);

	@Override
	public void onCreate() {
		super.onCreate();
		LOGGER.info("NetworkingService::Initializing.");
		this.networkingThread = new NetworkingThread(this);
		this.networkingThread.start();
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {
		this.someTask();
		// TODO: get mainActivity from intent;
		// this.mainActivity = ;
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public IBinder onBind(final Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Handler getMessageHandler() {
		return this.messageHandler;
	}

	private void onMessageFromNetworkingThread(final Object message) {
		NetworkingService.LOGGER.info("NS(<-NT): " + message.toString());
		this.sendMessageToUIThread(message);
	}

	private void onMessageFromUIThread(final Object message) {
		NetworkingService.LOGGER.info("NS(<-UI): " + message.toString());
		this.sendMessageToNetworkingThread(message);
	}

	private void sendMessageToNetworkingThread(final Object message) {
		Message msg = this.networkingThread.getMessageHandler().obtainMessage();
		msg.obj = message;
		this.networkingThread.getMessageHandler().sendMessage(msg);
	}

	private void sendMessageToUIThread(final Object message) {
		try {
			Message msg = this.mainActivity.get().getMessageHandler().obtainMessage();
			msg.obj = message;
			this.mainActivity.get().getMessageHandler().sendMessage(msg);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Exception", e);
		}
	}

	void someTask() {
	}
}
