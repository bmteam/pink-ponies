package ru.pinkponiesapp;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.msgpack.MessagePack;
import org.msgpack.packer.BufferPacker;

import ru.pinkponies.protocol.Login;

public class NetworkingThread extends Thread {
    private final String serverIp = "77.232.25.36";
    private final int serverPort = 4264;
    
    private WeakReference<MainActivity> mainActivity;
    
    private SocketChannel socket;
	private Selector selector;
    
    public MessageHandler messageHandler;
    
    NetworkingThread(MainActivity activity) {
    	mainActivity = new WeakReference<MainActivity>(activity);
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
    	MessagePack msgpack = new MessagePack();
    	msgpack.register(Login.class);
    	BufferPacker packer = msgpack.createBufferPacker();
    	
    	packer.write(new Login(Build.BOARD, Build.BOOTLOADER, Build.BRAND, 
    			Build.CPU_ABI, Build.CPU_ABI2, Build.DEVICE));
    	
    	ByteBuffer bb = ByteBuffer.wrap(packer.toByteArray());
    	socket.write(bb);
    }
    
    private void sendMessage(String message) throws IOException {
    	ByteBuffer bb = ByteBuffer.wrap(message.getBytes());
    	socket.write(bb);
    }
    
    private void onMessageFromUIThread(String message) {
    	try {
	    	sendMessageToUIThread("Got your message: '" + message + "'!");
	        if (message.equals("connect")) {
	        	connect();
	        } else if (message.equals("service")) {
	        	service();
	        } else if (message.equals("login")) {
	        	login();
	        } else {
	        	sendMessage(message);
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
            thread.get().onMessageFromUIThread((String)msg.obj);
        }
    };
}