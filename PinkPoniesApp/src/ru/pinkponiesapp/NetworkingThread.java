package ru.pinkponiesapp;

import java.lang.ref.WeakReference;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class NetworkingThread extends Thread {
    //private final String serverIp = "192.168.0.198";
    //private final int serverPort = 4263;
    
    private WeakReference<MainActivity> mainActivity;
    
    public MessageHandler messageHandler;
    
    NetworkingThread(MainActivity activity) {
        mainActivity = new WeakReference<MainActivity>(activity);
    }
    
    public void run() {
        try {
            Looper.prepare();
            messageHandler = new MessageHandler(this);
            //Looper.loop();
            
            sendMessageToUIThread("Hello from networking thread!");
        } catch (Exception e) {
            sendMessageToUIThread("Exception: " + e.getMessage());
        }
    }
    
    private void onMessageFromUIThread(String message) {
        sendMessageToUIThread("Got your message: '" + message + "'!");
    }
    
    private void sendMessageToUIThread(String message) {
        Message msg = mainActivity.get().messageHandler.obtainMessage();
        msg.obj = message;
        mainActivity.get().messageHandler.sendMessage(msg);
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