package com.drem.heartbeat.impl;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Created by dariosd on 2/24/2015.
 */
public class SocketHeartbeat extends AbstractHeartbeat {

    private static final Logger Log = Logger.getLogger(SocketHeartbeat.class.getSimpleName());
    private static final long serialVersionUID = 6029115156683575493L;
    private HeartBeatThread heartbeat;
    private String host;
    private Integer port;
    private Integer timeout;

    /**
     * Pings the default host and port. This uses default values for ping rate, timeout, and maximum retries.
     * @see #SocketHeartbeat(String, Integer, Integer, Integer, Integer)
     */
    public SocketHeartbeat(String host, Integer port) {
        this(host, port, null, null, null);
        // TODO: Consider throwing exception if host or port are null
    }

    /**
     * Pings the provided host at the provided port at a given ping rate. If the response takes longer than the timeout
     * provided, the Heart Beat will retry pinging until the specified number of retries. If the
     * Heart Beat isn't able to establish a good connection after the allowed retries, an event will
     * be fired to notify listeners that the service it is listening to has died.
     * @param host - The host name of the server to check for a heart beat
     * @param port - The port of the server to check for a heart beat
     * @param pingRate - The rate to check for a heart beat
     * @param maxRetries - The maximum number of attempts to reconnect before claiming service dead
     * @param timeout - The maximum time to wait for a response
     */
    public SocketHeartbeat(String host, Integer port, Integer pingRate,
                         Integer maxRetries, Integer timeout) {
        super(pingRate, maxRetries);
        this.host = host;
        this.port = port;
        this.timeout = timeout == null ? 5000 : timeout;
    }

    @Override
    public void start() {
        heartbeat = new SocketHeartbeatThread();
        heartbeat.start();
    }

    @Override
    public void stop() {
        heartbeat.shutdown();
    }

    @Override
    public boolean isAlive() {
        return (heartbeat != null && heartbeat.isRunning());
    }

    private class SocketHeartbeatThread extends HeartBeatThread {
        /**
         * Pings a Socket. This effectively sends a HEAD request and returns <code>true</code> if the response code is in
         * the 200-399 range.
         * @return <code>true</code> if the given HTTP URL has returned response code 200-399 on a HEAD request within the
         * given timeout, otherwise <code>false</code>.
         */
        public boolean pingService() {
            try {
                Socket t = new Socket();
                t.connect(new InetSocketAddress(host, port), timeout);
                DataInputStream dis = new DataInputStream(t.getInputStream());
                PrintStream ps = new PrintStream(t.getOutputStream());
                ps.println();
                dis.read();
                Log.info("Heartbeat socket response is OK.");
                t.close();
                return true;
            }
            catch (IOException e) {
                Log.severe("Could not get response from heartbeat.");
            }
            return false;
        }
    }
}
