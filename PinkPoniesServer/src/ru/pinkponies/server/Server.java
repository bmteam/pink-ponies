package ru.pinkponies.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public final class Server {
	static final int serverPort = 4264;
	
	private ServerSocketChannel serverSocketChannel;
	private Selector selector;
	
	private ByteBuffer readBuffer = ByteBuffer.allocate(8192);
	
	private void initialize() {
		try {
			selector = Selector.open();
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			InetSocketAddress address = new InetSocketAddress(serverPort);
			serverSocketChannel.socket().bind(address);
			SelectionKey key = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			
			System.out.println("serverSocketChannel's registered key is " + key.channel().toString() + ".");
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}
	
	private void startListening() {
		System.out.println("Server is listening on port " + serverSocketChannel.socket().getLocalPort() + ".");
		
		try {
			while(true) {
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
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}
	
	public void accept(SelectionKey key) throws IOException {
		System.out.println("Ready to accept a new client.");
		
		SocketChannel channel = serverSocketChannel.accept();
		channel.configureBlocking(false);
		channel.register(selector, SelectionKey.OP_READ);
		
		onConnect(channel);
	}
	
	public void read(SelectionKey key) throws IOException {
		System.out.println("Ready to receive a packet.");
		
		SocketChannel channel = (SocketChannel) key.channel();
		
		readBuffer.clear();
		
		int numRead;
		try {
			numRead = channel.read(readBuffer);
		} catch (IOException e) {
			channel.close();
			key.cancel();
			return;
		}
		
		if (numRead == -1) {
			channel.close();
			key.cancel();
			return;
		}
		
		readBuffer.flip();
		byte[] data = new byte[readBuffer.limit()];
		readBuffer.get(data);
		
		onMessage(channel, data);
	}
	
	public void onConnect(SocketChannel channel) {
		System.out.println("Client connected from " + channel.socket().getRemoteSocketAddress().toString() + ".");
	}
	
	public void onMessage(SocketChannel channel, byte[] data) {
		System.out.println("Message from " + channel.socket().getRemoteSocketAddress().toString() + ": '" + new String(data) + "'.");
	}
	
	public void sendMessage(SocketChannel channel, byte[] data) throws IOException {
    	ByteBuffer bb = ByteBuffer.wrap(data);
    	channel.write(bb);
	}

	public static void main(String[] args) {
		Server server = new Server();
		server.initialize();
		server.startListening();
	}

}
