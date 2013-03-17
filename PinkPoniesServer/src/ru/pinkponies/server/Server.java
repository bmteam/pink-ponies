package ru.pinkponies.server;

import java.io.IOException;
import java.net.InetSocketAddress;
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

public final class Server {
	static final int serverPort = 4264;
	
	private ServerSocketChannel serverSocketChannel;
	private Selector selector;
	
	private ByteBuffer readBuffer = ByteBuffer.allocate(8192);
	
	private Map<SocketChannel, List<ByteBuffer>> pendingData = new HashMap<SocketChannel, List<ByteBuffer>>();
	
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
		
		onConnect(channel);
	}
	
	public void read(SelectionKey key) throws IOException {
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
	
	public void write(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		
		synchronized (pendingData) {
			List<ByteBuffer> list = (List<ByteBuffer>) pendingData.get(channel);
			
			while (!list.isEmpty()) {
				ByteBuffer buffer = (ByteBuffer) list.get(0);
				channel.write(buffer);
				if (buffer.remaining() > 0) {
					// Socket buffer filled up.
					break;
				}
				list.remove(0);
			}
			
			if (list.isEmpty()) {
				key.interestOps(SelectionKey.OP_READ);
			}
		}
	}
	
	public void onConnect(SocketChannel channel) {
		System.out.println("Client connected from " + channel.socket().getRemoteSocketAddress().toString() + ".");
	}
	
	public void onMessage(SocketChannel channel, byte[] data) {
		System.out.println("Message from " + channel.socket().getRemoteSocketAddress().toString() + ": '" + new String(data) + "'.");
		//sendMessage(channel, data);
	}
	
	public void sendMessage(SocketChannel channel, byte[] data) {
		synchronized (pendingData) {
			List<ByteBuffer> list = (List<ByteBuffer>) pendingData.get(channel);
			if (list == null) {
				list = new ArrayList<ByteBuffer>();
				pendingData.put(channel, list);
			}
			list.add(ByteBuffer.wrap(data));
		}
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
