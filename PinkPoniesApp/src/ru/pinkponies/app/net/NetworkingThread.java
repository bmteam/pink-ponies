/**
 * Copyright (c) 2013 Alexander Konovalov, Andrey Konovalov, Sergey Voronov, Vitaly Malyshev. All
 * rights reserved. Use of this source code is governed by a BSD-style license that can be found in
 * the LICENSE file.
 */

package ru.pinkponies.app.net;

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
import ru.pinkponies.protocol.Packet;
import ru.pinkponies.protocol.PlayerUpdatePacket;
import ru.pinkponies.protocol.Protocol;
import ru.pinkponies.protocol.QuestUpdatePacket;
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
	 * The connect message id. Message object should be an instance of {@link InetSocketAddress}.
	 */
	public static final int MSG_CONNECT = 0;

	/**
	 * The service message id.
	 */
	public static final int MSG_SERVICE = 1;

	/**
	 * The "send packet" message id. Message object should be an instance of {@link Packet}
	 */
	public static final int MSG_SEND_PACKET = 2;

	/**
	 * The default incoming/outgoing buffer size.
	 */
	private static final int BUFFER_SIZE = 8192;

	/**
	 * The protocol helper. Provides methods for serialization and deserialization of packets.
	 */
	private final Protocol protocol;

	/**
	 * The message handler which receives messages for this networking thread.
	 */
	private MessageHandler messageHandler;

	/**
	 * The weak reference to the networking service.
	 */
	private final WeakReference<NetworkingService> networkingSevice;

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
	 * Creates a new networking thread which will communicate and send updates to the given service.
	 * 
	 * @param networkingSevice
	 *            The networking service to which updates will be sent.
	 */
	NetworkingThread(final NetworkingService networkingSevice) {
		this.networkingSevice = new WeakReference<NetworkingService>(networkingSevice);
		this.protocol = new Protocol();
	}

	/**
	 * Returns the message handler associated with this networking thread.
	 * 
	 * @return The message handler.
	 */
	public final Handler getMessageHandler() {
		return this.messageHandler;
	}

	/**
	 * Starts the networking thread.
	 */
	@Override
	public final void run() {
		Looper.prepare();
		this.messageHandler = new MessageHandler(this);
		this.sendMessageToService("initialized");
		Looper.loop();
	}

	/**
	 * Initiates connection to the server.
	 * 
	 * @throws IOException
	 *             If connection could not be initiated.
	 */
	private void connect(final InetSocketAddress address) throws IOException {
		LOGGER.info("Connecting to " + address.toString());

		this.socket = SocketChannel.open();
		this.socket.configureBlocking(false);
		this.socket.connect(address);

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
		final Set<SelectionKey> keys = this.selector.selectedKeys();
		final Iterator<SelectionKey> iterator = keys.iterator();

		while (iterator.hasNext()) {
			final SelectionKey key = iterator.next();
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

			this.sendMessageToService("connected");
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
		final SocketChannel channel = (SocketChannel) key.channel();

		this.incomingData.limit(this.incomingData.capacity());

		int numRead = -1;
		try {
			numRead = channel.read(this.incomingData);
		} catch (final IOException e) {
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
			} catch (final IOException e) {
				LOGGER.log(Level.SEVERE, "IOException during packet unpacking", e);
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
		final SocketChannel channel = (SocketChannel) key.channel();

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
			this.sendMessageToService(packet);
		} else if (packet instanceof SayPacket) {
			this.sendMessageToService(packet);
		} else if (packet instanceof PlayerUpdatePacket) {
			this.sendMessageToService(packet);
		} else if (packet instanceof QuestUpdatePacket) {
			this.sendMessageToService(packet);
		} else if (packet instanceof AppleUpdatePacket) {
			this.sendMessageToService(packet);
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
		} catch (final BufferOverflowException e) {
			LOGGER.log(Level.SEVERE, "Exception", e);
		}
	}

	/**
	 * Called when a new message was received from the service.
	 * 
	 * @param message
	 *            the message
	 */
	private void onMessageFromService(final Message message) {
		try {
			switch (message.what) {
			case MSG_CONNECT:
				this.connect((InetSocketAddress) message.obj);
				break;
			case MSG_SERVICE:
				this.service();
				break;
			case MSG_SEND_PACKET:
				this.sendPacket((Packet) message.obj);
				break;
			default:
				throw new InvalidParameterException("Unknown message type.");
			}
		} catch (final IOException e) {
			this.sendMessageToService("failed");
			LOGGER.log(Level.SEVERE, "Exception", e);
		}
	}

	/**
	 * Sends message to the activity.
	 * 
	 * @param message
	 *            The message.
	 */
	private void sendMessageToService(final Object message) {
		final Message msg = this.networkingSevice.get().getMessageHandler().obtainMessage();
		msg.obj = message;
		this.networkingSevice.get().getMessageHandler().sendMessage(msg);
	}

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
			this.thread.get().onMessageFromService(msg);
		}
	}
}
