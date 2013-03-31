package ru.pinkponies.app;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.InvalidParameterException;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import ru.pinkponies.protocol.LocationUpdatePacket;
import ru.pinkponies.protocol.LoginPacket;
import ru.pinkponies.protocol.Packet;
import ru.pinkponies.protocol.Protocol;
import ru.pinkponies.protocol.SayPacket;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class NetworkingThread extends Thread {
	private final String SERVER_IP = "10.55.87.47";
	private final int SERVER_PORT = 4268;

	private static final int BUFFER_SIZE = 8192;

	private final static Logger logger = Logger
			.getLogger(NetworkingThread.class.getName());

	private Protocol protocol;

	private WeakReference<MainActivity> mainActivity;

	public MessageHandler messageHandler;

	private SocketChannel socket;
	private Selector selector;

	private ByteBuffer incomingData = ByteBuffer.allocate(BUFFER_SIZE);
	private ByteBuffer outgoingData = ByteBuffer.allocate(BUFFER_SIZE);

	NetworkingThread(MainActivity activity) {
		mainActivity = new WeakReference<MainActivity>(activity);
		protocol = new Protocol();
	}

	public void run() {
		try {
			Looper.prepare();
			messageHandler = new MessageHandler(this);
			sendMessageToUIThread("initialized");
			Looper.loop();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception", e);
		}
	}

	private void connect() throws IOException {
		logger.info("Connecting to " + SERVER_IP + ":" + SERVER_PORT + "...");

		socket = SocketChannel.open();
		socket.configureBlocking(false);
		socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT));

		selector = Selector.open();
		socket.register(selector, SelectionKey.OP_CONNECT);

		logger.info("Connection initiated, waiting for finishing...");
	}

	private void service() throws IOException {
		if (selector.select() > 0) {
			Set<SelectionKey> keys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = keys.iterator();

			while (iterator.hasNext()) {
				SelectionKey key = iterator.next();
				iterator.remove();

				if (!key.isValid()) {
					continue;
				}

				if (key.isConnectable()) {
					finishConnection(key);
				} else if (key.isReadable()) {
					read(key);
				} else if (key.isWritable()) {
					write(key);
				}
			}
		}
	}

	private void finishConnection(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		if (channel.isConnectionPending()) {
			channel.finishConnect();
			channel.register(selector, SelectionKey.OP_READ
					| SelectionKey.OP_WRITE);
			logger.info("Now reading.");

			sendMessageToUIThread("connected");
		}
	}

	private void close(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		channel.close();
		key.cancel();
	}

	private void read(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();

		incomingData.limit(incomingData.capacity());

		int numRead = -1;
		try {
			numRead = channel.read(incomingData);
		} catch (IOException e) {
			close(key);
			logger.log(Level.SEVERE, "Exception", e);
			return;
		}

		if (numRead == -1) {
			close(key);
			return;
		}

		Packet packet = null;

		incomingData.flip();
		try {
			packet = protocol.unpack(incomingData);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception", e);
		}
		incomingData.compact();

		if (packet == null) {
			return;
		}

		if (packet instanceof SayPacket) {
			SayPacket sayPacket = (SayPacket) packet;
			logger.info("Server: " + sayPacket.toString());
		} else if (packet instanceof LocationUpdatePacket) {
			sendMessageToUIThread(packet);
		}
	}

	private void write(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();

		outgoingData.flip();
		channel.write(outgoingData);
		outgoingData.compact();
	}

	private void sendPacket(Packet packet) throws IOException {
		try {
			outgoingData.put(protocol.pack(packet));
		} catch (BufferOverflowException e) {
			logger.log(Level.SEVERE, "Exception", e);
		}
	}

	private void login() throws IOException {
		LoginPacket packet = new LoginPacket(Build.BOARD, Build.BOOTLOADER,
				Build.BRAND, Build.CPU_ABI, Build.CPU_ABI2, Build.DEVICE);
		sendPacket(packet);
	}

	private void say(String message) throws IOException {
		SayPacket packet = new SayPacket(message);
		sendPacket(packet);
	}

	private void onMessageFromUIThread(Object message) {
		try {
			logger.info("MA: " + message.toString());

			if (message.equals("connect")) {
				connect();
			} else if (message.equals("service")) {
				service();
			} else if (message.equals("login")) {
				login();
			} else if (message instanceof Packet) {
				sendPacket((Packet) message);
			} else if (message instanceof String) {
				say((String) message);
			} else {
				throw new InvalidParameterException("Unknown message type.");
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception", e);
		}
	}

	private void sendMessageToUIThread(Object message) {
		try {
			Message msg = mainActivity.get().messageHandler.obtainMessage();
			msg.obj = message;
			mainActivity.get().messageHandler.sendMessage(msg);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception", e);
		}
	}

	static public class MessageHandler extends Handler {
		private WeakReference<NetworkingThread> thread;

		MessageHandler(NetworkingThread networkingThread) {
			thread = new WeakReference<NetworkingThread>(networkingThread);
		}

		@Override
		public void handleMessage(Message msg) {
			thread.get().onMessageFromUIThread(msg.obj);
		}
	};
}