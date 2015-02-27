package com.drem.heartbeat.impl;


import org.junit.Assert;
import org.junit.BeforeClass;

import com.drem.heartbeat.api.IHeartbeatListener;
import com.drem.heartbeat.event.HeartbeatEvent;


/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class SocketHeartbeatTest {
    private SocketHeartbeat heartbeat;
    private HeartbeatTester listener = new HeartbeatTester();

    @BeforeClass
    protected void setUp() throws Exception
    {
        heartbeat = new SocketHeartbeat("localhost", 7);
        heartbeat.registerListener(listener);
    }

    public void testStartStopHeartBeat() throws Exception {
        Assert.assertFalse(heartbeat.isAlive());
        heartbeat.start();
        Thread.sleep(1000); // Sleep to let thread do work
        Assert.assertTrue(heartbeat.isAlive());
        Assert.assertTrue(listener.isAlive);
        heartbeat.stop();
        Thread.sleep(1000); // Sleep to let thread do work
        Assert.assertFalse(heartbeat.isAlive());
    }

    public void testHeartBeat() throws Exception {
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