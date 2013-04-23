package ru.pinkponies.app;

import java.lang.ref.WeakReference;
import java.util.logging.Logger;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

public class NetworkingService extends Service {

	private static final Logger LOGGER = Logger.getLogger(NetworkingService.class.getName());

	private final NetworkingThread networkingThread = new NetworkingThread(this);
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
		LOGGER.info("Service::Initializing.");
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
			this.sendMessageToNetworkingThread(appMessage);
			return;
		}

		if (appMessage.getReceiver() == AppMessage.node.LOGIN_ACTIVITY) {
			this.sendMessageToLoginActivity(appMessage);
			return;
		}

		if (appMessage.getReceiver() == AppMessage.node.MAIN_ACTIVITY) {
			this.sendMessageToUIThread(appMessage);
			return;
		}

	}

	void sendMessageToLoginActivity(final AppMessage message) {
	}

	private void sendMessageToNetworkingThread(final AppMessage message) {
		if (this.networkingThread == null) {
			NetworkingService.LOGGER.info("NT : NULL");
		}
		Message msg = this.networkingThread.getMessageHandler().obtainMessage();
		msg.obj = message;
		this.networkingThread.getMessageHandler().sendMessage(msg);
	}

	private void sendMessageToUIThread(final Object message) {
		if (this.mainActivity == null) {
			NetworkingService.LOGGER.info("MA : NULL");
		}
		Message msg = this.mainActivity.get().getMessageHandler().obtainMessage();
		msg.obj = message;
		this.mainActivity.get().getMessageHandler().sendMessage(msg);
	}

}
