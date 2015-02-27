package com.drem.heartbeat.impl;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Created by dariosd on 2/24/2015.
 */
public class HTTPHeartbeat extends AbstractHeartbeat {
    private static final Logger Log = Logger.getLogger(HTTPHeartbeat.class.getSimpleName());
    private static final long serialVersionUID = -4427623805027008314L;
    private HeartBeatThread heartbeat;
    private URL url;
    /**
     * Defaults to five seconds
     */
    private int timeout;

    /**
     * Pings the provided URL. This uses default values for ping rate, timeout, and maximum retries.
     * @see #HTTPHeartbeat(java.net.URL, Integer, Integer, Integer)
     * @param url - The URL to check for a heart beat
     */
    public HTTPHeartbeat(URL url) {
        this(url, null, null, null);
    }

    /**
     * Pings the provided URL at a given ping rate. If the response takes longer than the timeout
     * provided, the Heart Beat will retry pinging until the specified number of retries. If the
     * Heart Beat isn't able to establish a good connection after the allowed retries, an event will
     * be fired to notify listeners that the service it is listening to has died.
     * @param url - The URL to check for a heart beat
     * @param pingRate - The rate to check for a heart beat
     * @param maxRetries - The maximum number of attempts to reconnect before claiming service dead
     * @param timeout - The maximum time to wait for a response
     */
    public HTTPHeartbeat(URL url, Integer pingRate, Integer maxRetries, Integer timeout) {
        super(pingRate, maxRetries);
        this.url = url;
        this.timeout = timeout == null ? 5000 : timeout;
    }

    @Override
    public void start() {
        heartbeat = new HTTPHeartBeatThread();
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

    private class HTTPHeartBeatThread extends HeartBeatThread {

        /**
         * Pings a HTTP URL. This effectively sends a HEAD request and returns <code>true</code> if the response code is in
         * the 200-399 range.
         * @return <code>true</code> if the given HTTP URL has returned response code 200-399 on a HEAD request within the
         * given timeout, otherwise <code>false</code>.
         */
        @Override
        public boolean pingService() {
            try {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(timeout);
                connection.setReadTimeout(timeout);
                connection.setRequestMethod("HEAD");
                int responseCode = connection.getResponseCode();
                Log.info("Heartbeat response code: " + responseCode);
                return (200 <= responseCode && responseCode <= 399);
            } catch(IOException e) {
                Log.severe("Could not get response from heartbeat.");
            }
            return false;
        }
    }
}
