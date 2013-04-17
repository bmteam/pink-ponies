/**
 * Copyright (c) 2013 Alexander Konovalov, Andrey Konovalov, Sergey Voronov, Vitaly Malyshev. All
 * rights reserved. Use of this source code is governed by a BSD-style license that can be found in
 * the LICENSE file.
 */

package ru.pinkponies.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import ru.pinkponies.protocol.LocationUpdatePacket;
import ru.pinkponies.protocol.LoginPacket;
import ru.pinkponies.protocol.Packet;
import ru.pinkponies.protocol.Protocol;
import ru.pinkponies.protocol.SayPacket;

/**
 * Main server class.
 */
public final class Server {
	/**
	 * The class wide logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

	/**
	 * The port on which the server will listen for incoming connections.
	 */
	private static final int SERVER_PORT = 4264;

	/**
	 * The default incoming/outgoing buffer size.
	 */
	private static final int BUFFER_SIZE = 8192;

	/**
	 * The main socket channel on which the server listens for incoming connections.
	 */
	private ServerSocketChannel serverSocketChannel;

	/**
	 * The selector.
	 */
	private Selector selector;

	/**
	 * Incoming data buffers. One for each socket channel (for each connected peer).
	 */
	private final Map<SocketChannel, ByteBuffer> incomingData = new HashMap<SocketChannel, ByteBuffer>();

	/**
	 * Outgoing data buffers. One for each socket channel (for each connected peer).
	 */
	private final Map<SocketChannel, ByteBuffer> outgoingData = new HashMap<SocketChannel, ByteBuffer>();

	/**
	 * The list of all connected clients.
	 */
	private final List<SocketChannel> clients = new ArrayList<SocketChannel>();

	/**
	 * The protocol helper. Provides methods for serialization and deserialization of packets.
	 */
	private final Protocol protocol = new Protocol();

	/**
	 * Initializes this server.
	 */
	private void initialize() {
		try {
			Server.LOGGER.info("Initializing.");

			this.serverSocketChannel = ServerSocketChannel.open();
			this.serverSocketChannel.configureBlocking(false);
			final InetSocketAddress address = new InetSocketAddress(Server.SERVER_PORT);
			this.serverSocketChannel.socket().bind(address);

			this.selector = Selector.open();
			final SelectionKey key = this.serverSocketChannel.register(this.selector, SelectionKey.OP_ACCEPT);
			Server.LOGGER.info("serverSocketChannel's registered key is " + key.channel().toString() + ".");

			Server.LOGGER.info("Initialized.");
		} catch (final IOException e) {
			Server.LOGGER.log(Level.SEVERE, "IOException during initalization", e);
		}
	}

	/**
	 * Starts this server.
	 */
	private void start() {
		while (true) {
			try {
				this.pumpEvents();
			} catch (final IOException e) {
				Server.LOGGER.log(Level.SEVERE, "IOException during event pumping", e);
			}
		}
	}

	/**
	 * Pumps all available IO events.
	 * 
	 * @throws IOException
	 *             If there was any sort of IO error.
	 */
	private void pumpEvents() throws IOException {
		this.selector.select();
		final Set<SelectionKey> selectedKeys = this.selector.selectedKeys();
		final Iterator<SelectionKey> iterator = selectedKeys.iterator();

		while (iterator.hasNext()) {
			final SelectionKey key = iterator.next();
			iterator.remove();

			if (!key.isValid()) {
				continue;
			}

			try {
				if (key.isAcceptable()) {
					this.accept(key);
				} else if (key.isReadable()) {
					this.read(key);
				} else if (key.isWritable()) {
					this.write(key);
				}
			} catch (final IOException e) {
				this.close(key);
				Server.LOGGER.log(Level.SEVERE, "Exception", e);
			}
		}
	}

	/**
	 * Accepts a new client.
	 * 
	 * @param key
	 *            The selection key.
	 * @throws IOException
	 *             If there was any problem accepting the client.
	 */
	public void accept(final SelectionKey key) throws IOException {
		final SocketChannel channel = this.serverSocketChannel.accept();
		channel.configureBlocking(false);
		channel.register(this.selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

		this.incomingData.put(channel, ByteBuffer.allocate(Server.BUFFER_SIZE));
		this.outgoingData.put(channel, ByteBuffer.allocate(Server.BUFFER_SIZE));

		this.clients.add(channel);

		this.onConnect(channel);
	}

	/**
	 * Closes the connection and selection key.
	 * 
	 * @param key
	 *            The selection key.
	 * @throws IOException
	 *             If there was any problem during client disconnect.
	 */
	public void close(final SelectionKey key) throws IOException {
		final SocketChannel channel = (SocketChannel) key.channel();

		this.incomingData.remove(channel);
		this.outgoingData.remove(channel);

		this.clients.remove(channel);

		channel.close();
		key.cancel();
	}

	/**
	 * Reads available data from the channel corresponding to the given selection key.
	 * 
	 * @param key
	 *            The selection key.
	 * @throws IOException
	 *             If there was any IO error.
	 */
	public void read(final SelectionKey key) throws IOException {
		final SocketChannel channel = (SocketChannel) key.channel();
		final ByteBuffer buffer = this.incomingData.get(channel);

		buffer.limit(buffer.capacity());

		int numRead = -1;
		try {
			numRead = channel.read(buffer);
		} catch (final IOException e) {
			this.close(key);
			Server.LOGGER.log(Level.SEVERE, "Exception", e);
			return;
		}

		if (numRead == -1) {
			this.close(key);
			Server.LOGGER.severe("Read failed.");
			return;
		}

		Packet packet = null;

		buffer.flip();
		try {
			packet = this.protocol.unpack(buffer);
		} catch (final IOException e) {
			Server.LOGGER.log(Level.SEVERE, "IOException during packet unpacking", e);
		}
		buffer.compact();

		if (packet == null) {
			return;
		}

		this.onPacket(channel, packet);
	}

	/**
	 * Writes buffered data to the socket channel corresponding to the given selection key.
	 * 
	 * @param key
	 *            The selection key.
	 * @throws IOException
	 *             If there was any problem writing data.
	 */
	public void write(final SelectionKey key) throws IOException {
		final SocketChannel channel = (SocketChannel) key.channel();

		synchronized (this.outgoingData) {
			final ByteBuffer buffer = this.outgoingData.get(channel);

			buffer.flip();
			channel.write(buffer);
			buffer.compact();
		}
	}

	/**
	 * Called when new connection was established.
	 * 
	 * @param channel
	 *            The socket channel connecting to the new peer.
	 */
	public void onConnect(final SocketChannel channel) {
		System.out.println("Client connected from " + channel.socket().getRemoteSocketAddress().toString() + ".");
	}

	/**
	 * Called when there is new available packet on the given socket channel.
	 * 
	 * @param channel
	 *            The socket channel from which new data has arrived.
	 * @param packet
	 *            The incoming packet.
	 * @throws IOException
	 *             If there was any problem during packet processing.
	 */
	public void onPacket(final SocketChannel channel, final Packet packet) throws IOException {
		System.out.println("Message from " + channel.socket().getRemoteSocketAddress().toString() + ":");

		if (packet instanceof LoginPacket) {
			final LoginPacket loginPacket = (LoginPacket) packet;
			System.out.println(loginPacket.toString());
		} else if (packet instanceof SayPacket) {
			final SayPacket sayPacket = (SayPacket) packet;
			System.out.println(sayPacket.toString());
		} else if (packet instanceof LocationUpdatePacket) {
			final LocationUpdatePacket locUpdate = (LocationUpdatePacket) packet;
			System.out.println(locUpdate.toString());
			this.broadcastPacket(locUpdate);
		}
	}

	/**
	 * Writes raw byte data into the channel's output buffer.
	 * 
	 * @param channel
	 *            The channel to which output buffer data should be written.
	 * @param data
	 *            Raw byte data which should be written.
	 * @throws IOException
	 *             If there is not enough space in the output buffer.
	 */
	public void sendMessage(final SocketChannel channel, final byte[] data) throws IOException {
		synchronized (this.outgoingData) {
			final ByteBuffer buffer = this.outgoingData.get(channel);

			try {
				buffer.put(data);
			} catch (final BufferOverflowException e) {
				Server.LOGGER.log(Level.SEVERE, "Exception", e);
				throw new IOException(e);
			}
		}
	}

	/**
	 * Writes a packet into the channel's output buffer.
	 * 
	 * @param channel
	 *            The channel to which output buffer packet should be written.
	 * @param packet
	 *            The packet which should be written.
	 * @throws IOException
	 *             If there was a error writing to the output buffer (e.g not enough space).
	 */
	private void sendPacket(final SocketChannel channel, final Packet packet) throws IOException {
		this.sendMessage(channel, this.protocol.pack(packet));
	}

	/**
	 * Writes a broadcast packet to all output buffers.
	 * 
	 * @param packet
	 *            The packet which should be written.
	 * @throws IOException
	 *             If there was a error writing to any of the output buffers (e.g not enough space).
	 */
	private void broadcastPacket(final Packet packet) throws IOException {
		for (final SocketChannel client : this.clients) {
			this.sendPacket(client, packet);
		}
	}

	/**
	 * Main server method.
	 * 
	 * @param args
	 *            An array of strings containing command line arguments.
	 */
	public static void main(final String[] args) {
		final Server server = new Server();
		server.initialize();
		server.start();
	}
}
