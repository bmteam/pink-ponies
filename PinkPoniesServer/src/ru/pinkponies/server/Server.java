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
import ru.pinkponies.protocol.Location;
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
	 * The distance at which a player picks up an apple.
	 */
	private static final double INTERACTION_DISTANCE = 100.0;

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
	 * ID manager for generating new identifiers.
	 */
	private final IdManager idManager = new IdManager();

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
		} catch (final Exception e) {
			Server.LOGGER.log(Level.SEVERE, "Exception", e);
		}
	}

	/**
	 * Starts this server.
	 */
	private void start() {
		while (true) {
			try {
				this.pumpEvents();
				this.pickupApples();
			} catch (final Exception e) {
				Server.LOGGER.log(Level.SEVERE, "Exception", e);
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

		while (buffer.remaining() > 0) {
			try {
				packet = this.protocol.unpack(buffer);
			} catch (final Exception e) {
				Server.LOGGER.log(Level.SEVERE, "Exception", e);
			}

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
	 */
	public void onConnect(final SocketChannel channel) throws IOException {
		System.out.println("Client connected from " + channel.socket().getRemoteSocketAddress().toString() + ".");

		long id = this.idManager.newId();
		this.players.put(channel, new Player(id, null, channel));

		LoginPacket loginPacket = new LoginPacket(id);
		this.sendPacket(channel, loginPacket);

		for (Apple apple : this.apples.values()) {
			AppleUpdatePacket applePacket = new AppleUpdatePacket(apple.getId(), apple.getLocation(), true);
			this.sendPacket(channel, applePacket);
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
		} else if (packet instanceof LocationUpdatePacket) {
			final LocationUpdatePacket locUpdate = (LocationUpdatePacket) packet;
			locUpdate.clientId = this.players.get(channel).getId();
			this.players.get(channel).setLocation(locUpdate.location);
			System.out.println(locUpdate.toString());

			this.broadcastPacket(locUpdate);
			System.out.println("Location update broadcasted.");

			// XXX(xairy): temporary.
			Random generator = new Random();
			final double longitude = locUpdate.location.getLongitude() + (generator.nextDouble() - 0.5) * 0.01;
			final double latitude = locUpdate.location.getLatitude() + (generator.nextDouble() - 0.5) * 0.01;
			Location appleLocation = new Location(longitude, latitude, 0.0);
			System.out.println("Distance: " + locUpdate.location.distanceTo(appleLocation) + ".");
			this.addApple(appleLocation);
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
	 */
	private void addApple(final Location location) throws IOException {
		long id = this.idManager.newId();
		Apple apple = new Apple(id, location);
		this.apples.put(id, apple);
		AppleUpdatePacket packet = new AppleUpdatePacket(id, location, true);
		System.out.println("Added " + apple + ".");

		this.broadcastPacket(packet);
		System.out.println("Apple update broadcasted.");
	}

	/**
	 * Removes the apple with the specified id.
	 * 
	 * @param id
	 *            The id of the apple being removed.
	 */
	private void removeApple(final long id) throws IOException {
		Location location = this.apples.get(id).getLocation();
		AppleUpdatePacket packet = new AppleUpdatePacket(id, location, false);

		this.broadcastPacket(packet);
		System.out.println("Apple update broadcasted.");

		this.apples.remove(id);
		System.out.println("Removed Apple " + id + ".");
	}

	private void pickupApples() throws IOException {
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
