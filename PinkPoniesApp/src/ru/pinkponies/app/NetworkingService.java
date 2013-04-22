package ru.pinkponies.app;

import java.lang.ref.WeakReference;
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
	private WeakReference<LoginActivity> loginActivity;

	public static final class MessageHandler extends Handler {
		private final WeakReference<NetworkingService> service;

		MessageHandler(final NetworkingService networkingService) {
			this.service = new WeakReference<NetworkingService>(networkingService);
		}

		@Override
		public void handleMessage(final Message msg) {
			this.service.get().onMessage(msg);
		}
	}

	private final Handler messageHandler = new MessageHandler(this);

	public void setMainActivity(final MainActivity activity) {
		this.mainActivity = new WeakReference<MainActivity>(activity);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		LOGGER.info("NetworkingService::Initializing.");
		this.networkingThread = new NetworkingThread(this);
		this.networkingThread.start();
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {
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

	void onMessage(final Message message) {
		AppMessage appMessage = (AppMessage) message.obj;
		if (appMessage.getReceiver() == AppMessage.node.NETWORKING_THREAD) {
			this.onMessageFromNetworkingThread(appMessage);
			return;
		}

		if (appMessage.getReceiver() == AppMessage.node.LOGIN_ACTIVITY) {
			this.onMessageFromLoginActivity(appMessage);
			return;
		}

		if (appMessage.getReceiver() == AppMessage.node.LOGIN_ACTIVITY) {
			this.onMessageFromUIThread(appMessage);
			return;
		}

	}

	void onMessageFromLoginActivity(final AppMessage appMessage) {
	}

	void sendMessageToLoginActivity(final Object message) {
	}

	private void onMessageFromNetworkingThread(final Object message) {
		if (message instanceof String) {
			NetworkingService.LOGGER.info("NS::(NT->MA):: " + message.toString());
		}
		this.sendMessageToUIThread(message);
	}

	private void sendMessageToNetworkingThread(final Object message) {
		Message msg = this.networkingThread.getMessageHandler().obtainMessage();
		msg.obj = message;
		this.networkingThread.getMessageHandler().sendMessage(msg);
	}

	private void onMessageFromUIThread(final Object message) {
		if (message instanceof String) {
			NetworkingService.LOGGER.info("NS::(MA->NT):: " + message.toString());
		}
		this.sendMessageToNetworkingThread(message);
	}

	private void sendMessageToUIThread(final Object message) {
		Message msg = this.mainActivity.get().getMessageHandler().obtainMessage();
		msg.obj = message;
		this.mainActivity.get().getMessageHandler().sendMessage(msg);
	}

}
