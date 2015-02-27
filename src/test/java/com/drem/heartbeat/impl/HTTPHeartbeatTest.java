package com.drem.heartbeat.impl;

import java.net.URL;

import org.junit.Assert;
import org.junit.BeforeClass;

import com.drem.heartbeat.api.IHeartbeatListener;
import com.drem.heartbeat.event.HeartbeatEvent;


/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class HTTPHeartbeatTest {
    private HTTPHeartbeat heartbeat;
    private HeartbeatTester listener = new HeartbeatTester();

    @BeforeClass
    protected void setUp() throws Exception
    {
        URL url = new URL("http", "localhost", 8080, "rest/heartbeat");
        heartbeat = new HTTPHeartbeat(url);
        heartbeat.registerListener(listener);
    }

    public void testStartStopHeartbeat() throws Exception {
        Assert.assertFalse(heartbeat.isAlive());
        heartbeat.start();
        Thread.sleep(1000); // Sleep to let thread do work
        Assert.assertTrue(heartbeat.isAlive());
        Assert.assertTrue(listener.isAlive);
        heartbeat.stop();
        Thread.sleep(1000); // Sleep to let thread do work
        Assert.assertFalse(heartbeat.isAlive());
    }

    public void testHeartbeat() throws Exception {
        Assert.assertFalse(heartbeat.isAlive());
        heartbeat.start();
        Thread.sleep(10000); // Sleep to let thread do work
        heartbeat.stop();
    }

    private class HeartbeatTester implements IHeartbeatListener {

        public boolean isAlive;

        @Override
        public void serviceAlive(HeartbeatEvent event) {
            isAlive = true;
        }

        @Override
        public void serviceDied(HeartbeatEvent event) {
            isAlive = false;
        }
    }
}