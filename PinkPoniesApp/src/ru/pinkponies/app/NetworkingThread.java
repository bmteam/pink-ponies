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

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import ru.pinkponies.protocol.AppleUpdatePacket;
import ru.pinkponies.protocol.ClientOptionsPacket;
import ru.pinkponies.protocol.LocationUpdatePacket;
import ru.pinkponies.protocol.Packet;
import ru.pinkponies.protocol.Protocol;
import ru.pinkponies.protocol.SayPacket;

/**
 * The networking thread which provides asynchronous network IO for the main activity.
 */
public class NetworkingThread extends Thread {
	/**
	 * The class wide logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(NetworkingThread.class.getName());

	/**
	 * The default server ip.
	 */
	private static final String SERVER_IP = "192.168.0.199";

	/**
	 * The default server port.
	 */
	private static final int SERVER_PORT = 4264;

	/**
	 * A message handler class for the networking thread.
	 */
	private static final class MessageHandler extends Handler {
		/**
		 * The weak reference to the networking thread.
		 */
		private final WeakReference<NetworkingThread> thread;

		/**
		 * Creates a new message handler which handles messages sent to the networking thread.
		 * 
		 * @param networkingThread
		 *            The networking thread.
		 */
		MessageHandler(final NetworkingThread networkingThread) {
			this.thread = new WeakReference<NetworkingThread>(networkingThread);
		}

		/**
		 * Handles incoming messages and sends them to the networking thread.
		 * 
		 * @param msg
		 *            The incoming message.
		 */
		@Override
		public void handleMessage(final Message msg) {
			this.thread.get().onMessageFromUIThread(msg.obj);
		}
	};

	/**
	 * The message handler which receives messages for this networking thread.
	 */
	private MessageHandler messageHandler = new MessageHandler(this);

	/**
	 * Returns the message handler associated with this networking thread.
	 * 
	 * @return The message handler.
	 */
	public final Handler getMessageHandler() {
		return this.messageHandler;
	}

	/**
	 * The default incoming/outgoing buffer size.
	 */
	private static final int BUFFER_SIZE = 8192;

	/**
	 * The protocol helper. Provides methods for serialization and deserialization of packets.
	 */
	private final Protocol protocol;

	/**
	 * The weak reference to the main activity.
	 */
	private final WeakReference<MainActivity> mainActivity;

	/**
	 * The socket channel.
	 */
	private SocketChannel socket;

	/**
	 * The selector.
	 */
	private Selector selector;

	/**
	 * The incoming data buffer.
	 */
	private final ByteBuffer incomingData = ByteBuffer.allocate(BUFFER_SIZE);

	/**
	 * The outgoing data buffer.
	 */
	private final ByteBuffer outgoingData = ByteBuffer.allocate(BUFFER_SIZE);

	/**
	 * Creates a new networking thread which will communicate and send updates to the given
	 * activity.
	 * 
	 * @param activity
	 *            The activity to which updates will be sent.
	 */
	NetworkingThread(final MainActivity activity) {
		this.mainActivity = new WeakReference<MainActivity>(activity);
		this.protocol = new Protocol();
	}

	/**
	 * Starts the networking thread.
	 */
	@Override
	public final void run() {
		try {
			Looper.prepare();
			this.messageHandler = new MessageHandler(this);
			this.sendMessageToUIThread("initialized");
			Looper.loop();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Exception", e);
		}
	}

	/**
	 * Initiates connection to the server.
	 * 
	 * @throws IOException
	 *             If connection could not be initiated.
	 */
	private void connect() throws IOException {
		LOGGER.info("Connecting to " + NetworkingThread.SERVER_IP + ":" + NetworkingThread.SERVER_PORT + "...");

		this.socket = SocketChannel.open();
		this.socket.configureBlocking(false);
		this.socket.connect(new InetSocketAddress(NetworkingThread.SERVER_IP, NetworkingThread.SERVER_PORT));

		this.selector = Selector.open();
		this.socket.register(this.selector, SelectionKey.OP_CONNECT);

		LOGGER.info("Connection initiated, waiting for finishing...");
	}

	/**
	 * Pumps all available IO events.
	 * 
	 * @throws IOException
	 *             If there was any sort of IO error.
	 */
	private void service() throws IOException {
		this.selector.select();
		Set<SelectionKey> keys = this.selector.selectedKeys();
		Iterator<SelectionKey> iterator = keys.iterator();

		while (iterator.hasNext()) {
			SelectionKey key = iterator.next();
			iterator.remove();

			if (!key.isValid()) {
				continue;
			}

			if (key.isConnectable()) {
				this.finishConnection(key);
			} else if (key.isReadable()) {
				this.read(key);
			} else if (key.isWritable()) {
				this.write(key);
			}
		}
	}

	/**
	 * Finishes the connection.
	 * 
	 * @param key
	 *            The selection key.
	 * @throws IOException
	 *             If there was a problem finishing the connection.
	 */
	private void finishConnection(final SelectionKey key) throws IOException {
		if (this.socket.isConnectionPending()) {
			this.socket.finishConnect();
			this.socket.register(this.selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

			this.sendMessageToUIThread("connected");
		}
	}

	/**
	 * Closes the connection and selection key.
	 * 
	 * @param key
	 *            The selection key.
	 * @throws IOException
	 *             If there was a problem closing the connection or selection key.
	 */
	private void close(final SelectionKey key) throws IOException {
		this.socket.close();
		key.cancel();
	}

	/**
	 * Reads data available on the socket channel.
	 * 
	 * @param key
	 *            The selection key.
	 * @throws IOException
	 *             If there was a problem reading data.
	 */
	private void read(final SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();

		this.incomingData.limit(this.incomingData.capacity());

		int numRead;
		try {
			numRead = channel.read(this.incomingData);
		} catch (IOException e) {
			this.close(key);
			throw e;
		}

		if (numRead == -1) {
			this.close(key);
			throw new IOException("Read failed.");
		}

		Packet packet = null;

		this.incomingData.flip();

		while (this.incomingData.remaining() > 0) {
			try {
				packet = this.protocol.unpack(this.incomingData);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Exception", e);
			}

			if (packet == null) {
				break;
			}

			this.onPacket(packet);

			this.incomingData.compact();
			this.incomingData.flip();
		}

		this.incomingData.compact();
	}

	/**
	 * Writes all buffered outgoing data to the socket channel.
	 * 
	 * @param key
	 *            The selection key.
	 * @throws IOException
	 *             If there was a problem writing data.
	 */
	private void write(final SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();

		this.outgoingData.flip();
		channel.write(this.outgoingData);
		this.outgoingData.compact();
	}

	/**
	 * Parses a packet.
	 * 
	 * @param packet
	 *            The packet to be parsed.
	 */
	private void onPacket(final Packet packet) {
		if (packet instanceof ClientOptionsPacket) {
			this.sendMessageToUIThread(packet);
		} else if (packet instanceof SayPacket) {
			this.sendMessageToUIThread(packet);
		} else if (packet instanceof LocationUpdatePacket) {
			this.sendMessageToUIThread(packet);
		} else if (packet instanceof AppleUpdatePacket) {
			this.sendMessageToUIThread(packet);
		} else {
			LOGGER.severe("Unknown packet type.");
		}
	}

	/**
	 * Writes a packet into the channel's output buffer.
	 * 
	 * @param packet
	 *            The packet which should be written.
	 * @throws IOException
	 *             If there was a error writing to the output buffer (e.g not enough space).
	 */
	private void sendPacket(final Packet packet) throws IOException {
		try {
			this.outgoingData.put(this.protocol.pack(packet));
		} catch (BufferOverflowException e) {
			LOGGER.log(Level.SEVERE, "Exception", e);
		}
	}

	/**
	 * Writes a login packet to the output buffer.
	 * 
	 * @throws IOException
	 *             If there was a error writing to the output buffer (e.g not enough space).
	 */
	private void login() throws IOException {
		// TODO(xairy): login.
	}

	/**
	 * Writes a say packet to the output buffer.
	 * 
	 * @param message
	 *            The message.
	 * @throws IOException
	 *             If there was a error writing to the output buffer (e.g not enough space).
	 */
	private void say(final String message) throws IOException {
		SayPacket packet = new SayPacket(message);
		this.sendPacket(packet);
	}

	/**
	 * Called when a new message was received from the activity.
	 * 
	 * @param message
	 *            The message.
	 */
	private void onMessageFromUIThread(final Object message) {
		try {
			// LOGGER.info("MA: " + message.toString());

			if (message.equals("connect")) {
				this.connect();
			} else if (message.equals("service")) {
				this.service();
			} else if (message.equals("login")) {
				this.login();
			} else if (message instanceof Packet) {
				this.sendPacket((Packet) message);
			} else if (message instanceof String) {
				this.say((String) message);
			} else {
				throw new InvalidParameterException("Unknown message type.");
			}
		} catch (Exception e) {
			this.sendMessageToUIThread("failed");
			LOGGER.log(Level.SEVERE, "Exception", e);
		}
	}

	/**
	 * Sends message to the activity.
	 * 
	 * @param message
	 *            The message.
	 */
	private void sendMessageToUIThread(final Object message) {
		try {
			Message msg = this.mainActivity.get().getMessageHandler().obtainMessage();
			msg.obj = message;
			this.mainActivity.get().getMessageHandler().sendMessage(msg);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Exception", e);
		}
	}
}
