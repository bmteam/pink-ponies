package ru.pinkponies.app;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.InvalidParameterException;
import java.util.Iterator;
import java.util.Set;

import ru.pinkponies.protocol.LoginPacket;
import ru.pinkponies.protocol.Packet;
import ru.pinkponies.protocol.Protocol;
import ru.pinkponies.protocol.SayPacket;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class NetworkingThread extends Thread {
    private final String serverIp = "10.55.87.47";
    private final int serverPort = 4264;
    
    private Protocol protocol;
    
    private WeakReference<MainActivity> mainActivity;
    
    private SocketChannel socket;
	private Selector selector;
    
    public MessageHandler messageHandler;
    
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
    		e.printStackTrace();
    		sendMessageToUIThread("Exception: " + e.getMessage());
        }
    }
    
    private void connect() throws IOException {
    	sendMessageToUIThread("Connecting to " + serverIp + ":" + serverPort + "...");
    	
    	socket = SocketChannel.open();
    	socket.configureBlocking(false);
    	socket.connect(new InetSocketAddress(serverIp, serverPort));
    	
    	selector = Selector.open();
    	socket.register(selector, SelectionKey.OP_CONNECT);
    	
    	sendMessageToUIThread("Connection initiated, waiting for finishing...");
    }
    
    private void service() throws IOException {
    	while (selector.select() > 0) {
    		Set<SelectionKey> keys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = keys.iterator();
			
			while (iterator.hasNext()) {
				SelectionKey key = iterator.next();
				SocketChannel channel = (SocketChannel) key.channel();
				iterator.remove();
				
				if (!key.isValid()) {
					continue;
				}
				
				if (key.isConnectable()) {
					if (channel.isConnectionPending()) {
						channel.finishConnect();
						sendMessageToUIThread("Connected!");
					}
					continue;
				}
			}
    	}
    }
    
    private void login() throws IOException {
    	LoginPacket packet = new LoginPacket(Build.BOARD, Build.BOOTLOADER, Build.BRAND, 
    			Build.CPU_ABI, Build.CPU_ABI2, Build.DEVICE);
    	
    	ByteBuffer bb = ByteBuffer.wrap(protocol.pack(packet));
    	socket.write(bb);
    }
    
    private void say(String message) throws IOException {
    	SayPacket packet = new SayPacket(message);
    	
    	ByteBuffer bb = ByteBuffer.wrap(protocol.pack(packet));
    	socket.write(bb);
    }
    
    private void sendPacket(Packet packet) throws IOException {
    	ByteBuffer bb = ByteBuffer.wrap(protocol.pack(packet));
    	socket.write(bb);
    }
    
    private void onMessageFromUIThread(Object message) {
    	try {
	    	sendMessageToUIThread("Got your message: '" + message.toString() + "'!");
	    	
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
    		e.printStackTrace();
            sendMessageToUIThread("Exception: " + e.getMessage());
        }
    }
    
    private void sendMessageToUIThread(String message) {
	    try {
	        Message msg = mainActivity.get().messageHandler.obtainMessage();
	        msg.obj = message;
	        mainActivity.get().messageHandler.sendMessage(msg);
	    } catch (Exception e) {
	    	e.printStackTrace();
	        sendMessageToUIThread("Exception: " + e.getMessage());
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