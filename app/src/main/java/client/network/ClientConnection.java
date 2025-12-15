package client.network;

import java.io.*;
import java.net.Socket;

public class ClientConnection {

    private static ClientConnection instance;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Thread listenerThread;
    private MessageListener listener;

    public static ClientConnection getInstance() {
        if (instance == null) {
            instance = new ClientConnection();
        }
        return instance;
    }

    public boolean connect() {
        try {
            // Stop existing listener if any
            stopListener();
            
            // Close existing connection if any
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
            
            socket = new Socket("localhost", 5050);
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Connected to server");
            
            // Start listener after connection is established
            startListener();
            
            // Give listener thread a moment to start
            Thread.sleep(50);
            
            return true;
        } catch (Exception e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            e.printStackTrace();
            // Reset connection state
            socket = null;
            in = null;
            out = null;
            return false;
        }
    }

    public void setMessageListener(MessageListener listener) {
        this.listener = listener;
        System.out.println("Message listener set");
    }
    
    private void stopListener() {
        if (listenerThread != null && listenerThread.isAlive()) {
            try {
                listenerThread.interrupt();
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    public void send(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }

    public boolean isConnected() {
        return socket != null && !socket.isClosed() && socket.isConnected();
    }

    private void startListener() {
        if (in == null) {
            System.err.println("Cannot start listener - input stream is null!");
            return;
        }
        
        listenerThread = new Thread(() -> {
            try {
                System.out.println("Listener thread started");
                String line;
                while ((line = in.readLine()) != null && !Thread.currentThread().isInterrupted()) {
                    System.out.println("SERVER MESSAGE: " + line);
                    final String message = line; // Final copy for lambda
                    if (listener != null) {
                        System.out.println("Calling listener with message: " + message);
                        listener.onMessage(message);
                    } else {
                        System.out.println("WARNING: Listener is null! Message received: " + message);
                    }
                }
            } catch (Exception e) {
                System.out.println("Listener thread error: " + e.getMessage());
                e.printStackTrace();
            }
            System.out.println("Listener thread ended");
        });
        listenerThread.setDaemon(true);
        listenerThread.setName("ClientConnection-Listener");
        listenerThread.start();
        System.out.println("Listener thread created and started");
    }

    public interface MessageListener {
        void onMessage(String msg);
    }
}

