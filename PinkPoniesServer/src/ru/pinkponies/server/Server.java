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
import ru.pinkponies.protocol.LoginPacket;
import ru.pinkponies.protocol.Packet;
import ru.pinkponies.protocol.PlayerUpdatePacket;
import ru.pinkponies.protocol.Protocol;
import ru.pinkponies.protocol.QuestActionPacket;
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
	private static final int BUFFER_SIZE = 16777216;

	/**
	 * The distance at which players can pick up apples.
	 */
	private static final double APPLE_RADIUS = 10.0;

	/**
	 * The distance at which players can sign up for quests.
	 */
	private static final double QUEST_RADIUS = 30.0;

	/**
	 * The number of apples that will be created when a quest is accepted.
	 */
	private static final int APPLES_PER_QUEST = 10;

	/**
	 * The maximum distance in meters to the quest at which apples can appear.
	 */
	private static final double QUEST_TO_APPLES_DISTANCE = 50.0;

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

	private final Map<SocketChannel, Player> players = new HashMap<SocketChannel, Player>();
	private final Map<Player, SocketChannel> channels = new HashMap<Player, SocketChannel>();

	private final Map<Long, Quest> quests = new HashMap<Long, Quest>();

	private final IdManager idManager = new IdManager();
	private final Random random = new Random();

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

	private void start() {
		try {
			while (true) {
				this.pumpEvents();
				this.processQuests();
				this.processApples();
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

		this.channels.remove(this.players.get(channel));
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

	public void onConnect(final SocketChannel channel) throws IOException {
		System.out.println("Client connected from " + channel.socket().getRemoteSocketAddress().toString() + ".");

		final long id = this.idManager.newId();
		final Player newPlayer = new Player(id, null, channel);
		this.players.put(channel, newPlayer);
		this.channels.put(newPlayer, channel);

		final ClientOptionsPacket packet = new ClientOptionsPacket(id);
		this.sendPacket(channel, packet);

	}

	public void onPacket(final SocketChannel channel, final Packet packet) throws IOException {
		System.out.println("Message from " + channel.socket().getRemoteSocketAddress().toString() + ":");

		if (packet instanceof SayPacket) {
			final SayPacket sayPacket = (SayPacket) packet;
			this.onSayPacket(channel, sayPacket);
		} else if (packet instanceof PlayerUpdatePacket) {
			final PlayerUpdatePacket playerUpdatePacket = (PlayerUpdatePacket) packet;
			this.onPlayerUpdatePacket(channel, playerUpdatePacket);
		} else if (packet instanceof QuestActionPacket) {
			QuestActionPacket actionPacket = (QuestActionPacket) packet;
			this.onQuestActionPacket(channel, actionPacket);
		} else if (packet instanceof LoginPacket) {
			LoginPacket actionPacket = (LoginPacket) packet;
			this.onLoginPacket(channel, actionPacket);
		} else {
			LOGGER.info("Unknown packet type.");
		}
	}

	public boolean isLoginValid(final long id, final String login, final String password) {
		return true;
	}

	public void onLoginPacket(final SocketChannel channel, final LoginPacket packet) throws IOException {
		final Player newPlayer = this.players.get(channel);
		if (this.isLoginValid(packet.id, packet.login, packet.password) && !newPlayer.isNameChanged()) {
			newPlayer.setName(packet.login);
			System.out.println("Login accepted: <" + packet.login + ">");

			// Send info to new player about others' location.
			for (final Player player : this.players.values()) {
				final PlayerUpdatePacket playerUpdate = new PlayerUpdatePacket(player.getId(), player.getName(),
						player.getLocation());
				this.sendPacket(channel, playerUpdate);
			}

			// Send info to new player about quests.
			for (final Quest quest : this.quests.values()) {
				if (quest.getStatus() != Quest.Status.AVAILABLE) {
					continue;
				}
				final QuestUpdatePacket questPacket = new QuestUpdatePacket(quest.getId(), quest.getLocation(),
						QuestUpdatePacket.Status.APPEARED);
				this.sendPacket(channel, questPacket);
				System.out.println(questPacket.toString());
			}

			// Send info to others player about new player's location.
			for (final Player player : this.players.values()) {
				final PlayerUpdatePacket playerUpdate = new PlayerUpdatePacket(newPlayer.getId(), newPlayer.getName(),
						newPlayer.getLocation());
				this.sendPacket(player.getChannel(), playerUpdate);
			}

		}
	}

	public void onSayPacket(final SocketChannel channel, final SayPacket packet) throws IOException {
		System.out.println(packet.toString());
	}

	public void onPlayerUpdatePacket(final SocketChannel channel, final PlayerUpdatePacket packet) throws IOException {
		System.out.println(packet.toString());

		// Login packet should be first.
		if (!this.players.get(channel).isNameChanged()) {
			return;
		}
		packet.playerId = this.players.get(channel).getId();
		this.players.get(channel).setLocation(packet.location);

		// TODO: -> broadcast only checked (isNameChanged == True)
		this.broadcastPacket(packet);

		// XXX(xairy): temporary.
		for (int i = 0; i < 1; i++) {
			this.createRandomQuest(packet.location, 100);
		}
	}

	public void onQuestActionPacket(final SocketChannel channel, final QuestActionPacket packet) throws IOException {
		Player player = this.players.get(channel);
		Quest quest = this.quests.get(packet.questId);
		System.out
				.println("Player " + player.getId() + " wants to " + packet.action + " Quest " + packet.questId + ".");

		if (quest == null) {
			System.out.println("No such quest!");
			return;
		}

		if (packet.action == QuestActionPacket.Action.JOIN) {
			if (player.getQuest() != null) {
				System.out.println("Player " + player.getId() + " already joined Quest " + quest.getId() + "!");
				return;
			}
			if (quest.getStatus() != Quest.Status.AVAILABLE) {
				System.out.println("Player " + player.getId() + " wants to join Quest " + quest.getId()
						+ ", but it has already started (or ended)!");
				return;
			}

			quest.addParticipant(player);
			player.setQuest(quest);

			for (Quest existingQuest : this.quests.values()) {
				final QuestUpdatePacket questUpdatePacket = new QuestUpdatePacket(existingQuest.getId(),
						existingQuest.getLocation(), QuestUpdatePacket.Status.DISAPPEARED);
				this.sendPacket(player, questUpdatePacket);
				if (existingQuest.isPotentialParticipant(player)) {
					existingQuest.removePotentialParticipant(player);
				}
			}

			final QuestUpdatePacket questUpdatePacket = new QuestUpdatePacket(quest.getId(), quest.getLocation(),
					QuestUpdatePacket.Status.ACCEPTED);
			this.sendPacket(player, questUpdatePacket);

			System.out.println("Player " + player.getId() + " joined Quest " + quest.getId() + ".");
		} else if (packet.action == QuestActionPacket.Action.START) {
			if (player.getQuest() == null) {
				System.out.println("Player " + player.getId() + " have not joined Quest " + quest.getId()
						+ ", but wants to start!");
				return;
			}
			if (player.getQuest() != quest) {
				System.out.println("Player " + player.getId() + " joined Quest " + player.getQuest().getId()
						+ ", but wants to start Quest " + quest.getId() + "!");
				return;
			}
			if (quest.getStatus() != Quest.Status.AVAILABLE) {
				System.out.println("Player " + player.getId() + " wants to join Quest " + quest.getId()
						+ ", but it has already started (or ended)!");
				return;
			}

			quest.setStatus(Quest.Status.STARTED);

			for (Player participant : quest.getParticipants().values()) {
				final QuestUpdatePacket questUpdatePacket = new QuestUpdatePacket(quest.getId(), quest.getLocation(),
						QuestUpdatePacket.Status.STARTED);
				this.sendPacket(participant, questUpdatePacket);
				for (Apple apple : quest.getApples().values()) {
					final AppleUpdatePacket appleUpdatePacket = new AppleUpdatePacket(apple.getId(),
							apple.getLocation(), true);
					this.sendPacket(participant, appleUpdatePacket);
				}
			}

			for (Player nonParticipant : this.players.values()) {
				if (quest.isParticipant(nonParticipant)) {
					continue;
				}
				final QuestUpdatePacket questUpdatePacket = new QuestUpdatePacket(quest.getId(), quest.getLocation(),
						QuestUpdatePacket.Status.DISAPPEARED);
				this.sendPacket(nonParticipant, questUpdatePacket);
			}
		} else if (packet.action == QuestActionPacket.Action.LEAVE) {
			if (player.getQuest() == null) {
				System.out.println("Player " + player.getId() + " have not joined Quest " + quest.getId()
						+ ", but wants to leave!");
				return;
			}
			if (player.getQuest() != quest) {
				System.out.println("Player " + player.getId() + " joined Quest " + player.getQuest().getId()
						+ ", but wants to leave Quest " + quest.getId() + "!");
				return;
			}

			for (Apple apple : quest.getApples().values()) {
				final AppleUpdatePacket appleUpdatePacket = new AppleUpdatePacket(apple.getId(), apple.getLocation(),
						false);
				this.sendPacket(player, appleUpdatePacket);
			}

			final QuestUpdatePacket questUpdatePacket = new QuestUpdatePacket(quest.getId(), quest.getLocation(),
					QuestUpdatePacket.Status.DECLINED);
			this.sendPacket(player, questUpdatePacket);

			for (Quest existingQuest : this.quests.values()) {
				if (existingQuest.getStatus() != Quest.Status.AVAILABLE) {
					continue;
				}
				final QuestUpdatePacket existingQuestUpdatePacket = new QuestUpdatePacket(existingQuest.getId(),
						existingQuest.getLocation(), QuestUpdatePacket.Status.APPEARED);
				this.sendPacket(player, existingQuestUpdatePacket);
			}

			quest.removeParticipant(player);
			player.setQuest(null);

			System.out.println("Player " + player.getId() + " left Quest " + quest.getId() + ".");
		}
	}

	private void sendPacket(final SocketChannel channel, final Packet packet) throws IOException {
		synchronized (this.outgoingData) {
			final byte[] data = this.protocol.pack(packet);
			final ByteBuffer buffer = this.outgoingData.get(channel);

			try {
				buffer.put(data);
			} catch (final BufferOverflowException e) {
				Server.LOGGER.log(Level.SEVERE, "Exception", e);
				throw new IOException(e);
			}
		}
	}

	private void sendPacket(final Player player, final Packet packet) throws IOException {
		this.sendPacket(this.channels.get(player), packet);
	}

	private void broadcastPacket(final Packet packet) throws IOException {
		for (final Player player : this.players.values()) {
			this.sendPacket(player.getChannel(), packet);
		}
	}

	private void createQuest(final Location location) throws IOException {
		final long questId = this.idManager.newId();
		final Quest quest = new Quest(questId, location);
		this.quests.put(questId, quest);

		for (int i = 0; i < APPLES_PER_QUEST; i++) {
			final long appleId = this.idManager.newId();
			final Location appleLocation = this.generateRandomLocation(location, QUEST_TO_APPLES_DISTANCE);
			final Apple apple = new Apple(appleId, appleLocation, questId);
			quest.addApple(apple);
		}

		final QuestUpdatePacket packet = new QuestUpdatePacket(questId, location, QuestUpdatePacket.Status.APPEARED);
		for (Player player : this.players.values()) {
			if (player.getQuest() == null) {
				this.sendPacket(player, packet);
			}
		}

		System.out.println("Added " + quest + ".");
	}

	private void createRandomQuest(final Location location, final double distance) throws IOException {
		this.createQuest(this.generateRandomLocation(location, distance));
	}

	private void processQuests() throws IOException {
		for (Player player : this.players.values()) {
			if (player.getLocation() == null || player.getQuest() != null) {
				continue;
			}
			for (Quest quest : this.quests.values()) {
				if (quest.getStatus() != Quest.Status.AVAILABLE) {
					continue;
				}
				if (player.getLocation().distanceTo(quest.getLocation()) <= QUEST_RADIUS) {
					if (!quest.isPotentialParticipant(player)) {
						System.out.println("Player " + player.getId() + " is near to Quest " + quest.getId() + ".");
						quest.addPotentialParticipant(player);
						QuestUpdatePacket packet = new QuestUpdatePacket(quest.getId(), quest.getLocation(),
								QuestUpdatePacket.Status.AVAILABLE);
						this.sendPacket(player, packet);
					}
				} else {
					if (quest.isPotentialParticipant(player)) {
						System.out.println("Player " + player.getId() + " is far from Quest " + quest.getId() + ".");
						quest.removePotentialParticipant(player);
						QuestUpdatePacket packet = new QuestUpdatePacket(quest.getId(), quest.getLocation(),
								QuestUpdatePacket.Status.UNAVAILABLE);
						this.sendPacket(player, packet);
					}
				}
			}
		}
	}

	private void processApples() throws IOException {
		for (Player player : this.players.values()) {
			if (player.getLocation() == null || player.getQuest() == null) {
				continue;
			}
			if (player.getQuest().getStatus() != Quest.Status.STARTED) {
				continue;
			}
			Iterator<Map.Entry<Long, Apple>> iter = player.getQuest().getApples().entrySet().iterator();
			while (iter.hasNext()) {
				Apple apple = iter.next().getValue();
				if (player.getLocation().distanceTo(apple.getLocation()) <= APPLE_RADIUS) {
					AppleUpdatePacket packet = new AppleUpdatePacket(apple.getId(), apple.getLocation(), false);
					for (Player participant : player.getQuest().getParticipants().values()) {
						this.sendPacket(participant, packet);
					}
					iter.remove();
					// TODO: score points to the player.
				}
			}
		}
	}

	private Location generateRandomLocation(final Location location, final double distance) {
		Location randomLocation = location.randomLocationInCircle(this.random, distance);
		return randomLocation;
	}

	public static void main(final String[] args) {
		final Server server = new Server();
		server.initialize();
		server.start();
	}
}
