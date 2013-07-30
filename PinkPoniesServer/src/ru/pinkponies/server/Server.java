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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import ru.pinkponies.protocol.AppleUpdatePacket;
import ru.pinkponies.protocol.ClientOptionsPacket;
import ru.pinkponies.protocol.Location;
import ru.pinkponies.protocol.Packet;
import ru.pinkponies.protocol.PlayerUpdatePacket;
import ru.pinkponies.protocol.Protocol;
import ru.pinkponies.protocol.QuestUpdatePacket;
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
	 * The distance at which players pick up apples.
	 */
	private static final double INTERACTION_DISTANCE = 50.0;

	/**
	 * The number of apples that will be created when a quest is accepted.
	 */
	private static final int APPLES_PER_QUEST = 5;

	/**
	 * The maximum distance in meters to the quest at which apples can appear.
	 */
	private static final double QUEST_TO_APPLES_DISTANCE = 100;

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
	 * The protocol helper. Provides methods for serialization and deserialization of packets.
	 */
	private final Protocol protocol = new Protocol();

	/**
	 * The map of all connected clients.
	 */
	private final Map<SocketChannel, Player> players = new HashMap<SocketChannel, Player>();

	/**
	 * The map of all existing apples.
	 */
	private final Map<Long, Apple> apples = new HashMap<Long, Apple>();

	/**
	 * The map of all existing quests.
	 */
	private final Map<Long, Quest> quests = new HashMap<Long, Quest>();

	/**
	 * ID manager for generating new identifiers.
	 */
	private final IdManager idManager = new IdManager();

	/**
	 * Random numbers generator.
	 */
	private final Random random = new Random();

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
		try {
			while (true) {
				this.pumpEvents();
				this.processApples();
				this.processQuests();
			}
		} catch (final IOException e) {
			Server.LOGGER.log(Level.SEVERE, "IOException during event pumping", e);
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
				Server.LOGGER.log(Level.SEVERE, "IOException while handling client", e);
				Server.LOGGER.info("Client has been forcefully disconnected.");
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

		this.players.remove(channel);

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

		int numRead = channel.read(buffer);
		if (numRead == -1) {
			throw new IOException("Error while reading packet.");
		}

		Packet packet = null;

		buffer.flip();

		while (buffer.remaining() > 0) {
			packet = this.protocol.unpack(buffer);
			if (packet == null) {
				break;
			}

			this.onPacket(channel, packet);

			buffer.compact();
			buffer.flip();
		}

		buffer.compact();
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
	 * @throws IOException
	 *             if there was any io error.
	 */
	public void onConnect(final SocketChannel channel) throws IOException {
		System.out.println("Client connected from " + channel.socket().getRemoteSocketAddress().toString() + ".");

		final long id = this.idManager.newId();
		this.players.put(channel, new Player(id, null, channel));

		final ClientOptionsPacket packet = new ClientOptionsPacket(id);
		this.sendPacket(channel, packet);

		for (final Player player : this.players.values()) {
			final PlayerUpdatePacket playerUpdate = new PlayerUpdatePacket(player.getId(), player.getLocation());
			this.sendPacket(channel, playerUpdate);
		}

		for (final Apple apple : this.apples.values()) {
			final AppleUpdatePacket applePacket = new AppleUpdatePacket(apple.getId(), apple.getLocation(), true);
			this.sendPacket(channel, applePacket);
		}

		for (final Quest quest : this.quests.values()) {
			final QuestUpdatePacket questPacket = new QuestUpdatePacket(quest.getId(), quest.getLocation(), true);
			this.sendPacket(channel, questPacket);
		}
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

		if (packet instanceof SayPacket) {
			final SayPacket sayPacket = (SayPacket) packet;
			System.out.println(sayPacket.toString());
		} else if (packet instanceof PlayerUpdatePacket) {
			final PlayerUpdatePacket locUpdate = (PlayerUpdatePacket) packet;
			locUpdate.setClientId(this.players.get(channel).getId());
			this.players.get(channel).setLocation(locUpdate.getLocation());
			System.out.println(locUpdate.toString());

			this.broadcastPacket(locUpdate);
			System.out.println("Location update broadcasted.");

			// XXX(xairy): temporary.
			for (int i = 0; i < 5; i++) {
				this.addRandomQuest(locUpdate.getLocation(), 300);
			}
		} else {
			LOGGER.info("Unknown packet type.");
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
		for (final Player player : this.players.values()) {
			this.sendPacket(player.getChannel(), packet);
		}
	}

	/**
	 * Adds a new apple with a specified location.
	 * 
	 * @param location
	 *            Location of the apple added.
	 * @throws IOException
	 *             if there was any problem broadcasting apple update.
	 */
	private void addApple(final Location location) throws IOException {
		final long id = this.idManager.newId();
		final Apple apple = new Apple(id, location);
		this.apples.put(id, apple);
		final AppleUpdatePacket packet = new AppleUpdatePacket(id, location, true);
		System.out.println("Added " + apple + ".");

		this.broadcastPacket(packet);
		System.out.println("Apple update broadcasted.");
	}

	/**
	 * Adds a new apple somewhere near the specified location.
	 * 
	 * @param location
	 *            Near this location an apple will be added.
	 * @param distance
	 *            The maximum distance to an apple in meters.
	 * @throws IOException
	 *             if there was any problem broadcasting apple update.
	 */
	private void addRandomApple(final Location location, final double distance) throws IOException {
		this.addApple(this.generateRandomLocation(location, distance));
	}

	/**
	 * Removes the apple with the specified id.
	 * 
	 * @param id
	 *            The id of the apple being removed.
	 * @throws IOException
	 *             if there was any problem broadcasting apple update.
	 */
	private void removeApple(final long id) throws IOException {
		final Location location = this.apples.get(id).getLocation();
		final AppleUpdatePacket packet = new AppleUpdatePacket(id, location, false);

		this.broadcastPacket(packet);
		System.out.println("Apple update broadcasted.");

		this.apples.remove(id);
		System.out.println("Removed Apple " + id + ".");
	}

	/**
	 * Adds a new quest with a specified location.
	 * 
	 * @param location
	 *            Location of the quest added.
	 * @throws IOException
	 *             if there was any problem broadcasting quest update.
	 */
	private void addQuest(final Location location) throws IOException {
		final long id = this.idManager.newId();
		final Quest quest = new Quest(id, location);
		this.quests.put(id, quest);
		final QuestUpdatePacket packet = new QuestUpdatePacket(id, location, true);
		System.out.println("Added " + quest + ".");

		this.broadcastPacket(packet);
		System.out.println("Quest update broadcasted.");
	}

	/**
	 * Adds a new quest somewhere near the specified location.
	 * 
	 * @param location
	 *            Near this location a quest will be added.
	 * @param distance
	 *            The maximum distance to a quest in meters.
	 * @throws IOException
	 *             if there was any problem broadcasting quest update.
	 */
	private void addRandomQuest(final Location location, final double distance) throws IOException {
		this.addQuest(this.generateRandomLocation(location, distance));
	}

	/**
	 * Removes the quest with the specified id.
	 * 
	 * @param id
	 *            The id of the quest being removed.
	 * @throws IOException
	 *             if there was any problem broadcasting quest update.
	 */
	private void removeQuest(final long id) throws IOException {
		final Location location = this.quests.get(id).getLocation();
		final QuestUpdatePacket packet = new QuestUpdatePacket(id, location, false);

		this.broadcastPacket(packet);
		System.out.println("Quest update broadcasted.");

		this.quests.remove(id);
		System.out.println("Removed Quest " + id + ".");
	}

	/**
	 * Processes all apples and checks if they are being picked up by any player.
	 * 
	 * @throws IOException
	 *             if there was any problem broadcasting apple update.
	 */
	private void processApples() throws IOException {
		for (Player player : this.players.values()) {
			if (player.getLocation() != null) {
				for (Apple apple : this.apples.values()) {
					if (player.getLocation().distanceTo(apple.getLocation()) <= INTERACTION_DISTANCE) {
						System.out.println("Player " + player.getId() + " picked up Apple " + apple.getId() + ".");
						this.removeApple(apple.getId());
						return;
					}
				}
			}
		}
	}

	/**
	 * Processes all quests and checks if they are being accepted up by any player.
	 * 
	 * @throws IOException
	 *             if there was any problem broadcasting quest update.
	 */
	private void processQuests() throws IOException {
		for (Player player : this.players.values()) {
			if (player.getLocation() != null) {
				for (Quest quest : this.quests.values()) {
					if (player.getLocation().distanceTo(quest.getLocation()) <= INTERACTION_DISTANCE) {
						System.out.println("Player " + player.getId() + " accepted Quest " + quest.getId() + ".");
						this.removeQuest(quest.getId());
						for (int i = 0; i < APPLES_PER_QUEST; i++) {
							this.addRandomApple(quest.getLocation(), QUEST_TO_APPLES_DISTANCE);
						}
						return;
					}
				}
			}
		}
	}

	/**
	 * Generates a random location near the specified location.
	 * 
	 * @param location
	 *            Near this location a new location will be generated.
	 * 
	 * @param distance
	 *            The maximum distance to a new location in meters.
	 */
	private Location generateRandomLocation(final Location location, final double distance) {
		Location randomLocation = location.randomLocationInCircle(this.random, distance);
		System.out.println("Distance: " + location.distanceTo(randomLocation) + ".");
		return randomLocation;
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
