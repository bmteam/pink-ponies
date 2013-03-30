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
import java.util.Set;

import ru.pinkponies.protocol.LocationUpdatePacket;
import ru.pinkponies.protocol.LoginPacket;
import ru.pinkponies.protocol.Packet;
import ru.pinkponies.protocol.Protocol;
import ru.pinkponies.protocol.SayPacket;

public final class Server {
	private static final int SERVER_PORT = 4264;
	private static final int BUFFER_SIZE = 8192;
	
	private ServerSocketChannel serverSocketChannel;
	private Selector selector;
	
	private Map<SocketChannel, ByteBuffer> incomingData = new HashMap<SocketChannel, ByteBuffer>();
	private Map<SocketChannel, ByteBuffer> outgoingData = new HashMap<SocketChannel, ByteBuffer>();
	
	private Protocol protocol;
	
	private void initialize() {
		try {
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			InetSocketAddress address = new InetSocketAddress(SERVER_PORT);
			serverSocketChannel.socket().bind(address);
			
			selector = Selector.open();
			SelectionKey key = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);		
			System.out.println("serverSocketChannel's registered key is " + key.channel().toString() + ".");
			
			protocol = new Protocol();
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}
	
	private void start() throws IOException {
		System.out.println("Server is listening on port " + serverSocketChannel.socket().getLocalPort() + ".");

		while(true) {
			pumpEvents();
		}
	}
	
	private void pumpEvents() throws IOException {
		selector.select();
		Set<SelectionKey> selectedKeys = selector.selectedKeys();
		Iterator<SelectionKey> iterator = selectedKeys.iterator();
		
		while (iterator.hasNext()) {
			SelectionKey key = (SelectionKey) iterator.next();
			iterator.remove();
			
			if (!key.isValid()) {
				continue;
			}
			
			if (key.isAcceptable()) {
				accept(key);
			} else if (key.isReadable()) {
				read(key);
			} else if (key.isWritable()) {
				write(key);
			}
		}
	}
	
	public void accept(SelectionKey key) throws IOException {		
		SocketChannel channel = serverSocketChannel.accept();
		channel.configureBlocking(false);
		channel.register(selector, SelectionKey.OP_READ);
		//channel.register(selector, SelectionKey.OP_WRITE);
		
		incomingData.put(channel, ByteBuffer.allocate(BUFFER_SIZE));
		outgoingData.put(channel, ByteBuffer.allocate(BUFFER_SIZE));
		
		onConnect(channel);
	}
	
	public void close(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		
		incomingData.remove(channel);
		outgoingData.remove(channel);
		
		channel.close();
		key.cancel();
	}
	
	public void read(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		ByteBuffer buffer = incomingData.get(channel);
		
		buffer.limit(buffer.capacity());
		
		int numRead = -1;
		try {
			numRead = channel.read(buffer);
		} catch (IOException e) {
			close(key);
			return;
		}
		
		if (numRead == -1) {
			close(key);
			return;
		}
		
		onMessage(channel, buffer);
	}
	
	public void write(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		
		synchronized (outgoingData) {
			ByteBuffer buffer = outgoingData.get(channel);
			
			buffer.flip();
			channel.write(buffer);
			buffer.compact();
			
			if (buffer.remaining() == 0) {
				key.interestOps(SelectionKey.OP_READ);
			}
		}
	}
	
	public void onConnect(SocketChannel channel) {
		System.out.println("Client connected from " + channel.socket().getRemoteSocketAddress().toString() + ".");
	}
	
	public void onMessage(SocketChannel channel, ByteBuffer buffer) throws IOException {
		System.out.println("Message from " + channel.socket().getRemoteSocketAddress().toString() + ":");
		
		Packet packet = null;
		
		buffer.flip();
		try {
			packet = protocol.unpack(buffer);
		} catch (Exception e) {
			e.printStackTrace();
		}
		buffer.compact();
		
		if (packet == null) {
			return;
		}
		
		if (packet instanceof LoginPacket) {
			LoginPacket loginPacket = (LoginPacket) packet;
			System.out.println(loginPacket.toString());
		} else if (packet instanceof SayPacket) {
			SayPacket sayPacket = (SayPacket) packet;
			System.out.println(sayPacket.toString());
		} else if (packet instanceof LocationUpdatePacket) {
			LocationUpdatePacket locUpdate = (LocationUpdatePacket) packet;
			System.out.println(locUpdate.toString());
			//say(channel, "Thank you!"); // XXX.
		}
	}
	
	public void sendMessage(SocketChannel channel, byte[] data) {
		synchronized (outgoingData) {
			ByteBuffer buffer = outgoingData.get(channel);
			
			try {
				buffer.put(data);
			} catch(BufferOverflowException e) {
				e.printStackTrace();
			}
		}
	}
	
    private void sendPacket(SocketChannel channel, Packet packet) throws IOException {
    	sendMessage(channel, protocol.pack(packet));
    }
    
    private void say(SocketChannel channel, String message) throws IOException {
    	SayPacket packet = new SayPacket(message);
    	sendPacket(channel, packet);
    }
	
	public static void main(String[] args) {
		try {
			Server server = new Server();
			server.initialize();
			server.start();
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}

}
