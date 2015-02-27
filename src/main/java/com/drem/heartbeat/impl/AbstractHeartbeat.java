package com.drem.heartbeat.impl;

import java.util.logging.Logger;

import com.drem.heartbeat.api.IHeartbeat;
import com.drem.heartbeat.api.IHeartbeatListener;
import com.drem.heartbeat.api.IHeartbeatState;
import com.drem.heartbeat.common.EventListenerList;
import com.drem.heartbeat.event.HeartbeatEvent;

/**
 * Created by dariosd on 2/24/2015.
 */
public abstract class AbstractHeartbeat implements IHeartbeat {
	private static final long serialVersionUID = 6015373468933464677L;
	private static final Logger Log = Logger.getLogger(AbstractHeartbeat.class.getSimpleName());
    private IHeartbeatState state;
    private EventListenerList listeners = new EventListenerList();
    private int pingAttempts = 0;
    private boolean connected;
    /**
     * Defaults to once per second for the first minute.
     */
    private int pingRate;
    /**
     * Defaults to two attepmts
     */
    private int maxRetries;

    public AbstractHeartbeat(Integer pingRate, Integer maxRetries) {
        this.pingRate = pingRate == null ? 1000 : pingRate;
        this.maxRetries = maxRetries == null ? 2 : maxRetries;
    }

    @Override
    public void registerListener(IHeartbeatListener listener) {
        listeners.add(IHeartbeatListener.class, listener);
    }

    @Override
    public void deregisterListener(IHeartbeatListener listener) {
        listeners.remove(IHeartbeatListener.class, listener);
    }

    abstract class HeartBeatThread extends Thread {
        private boolean running;

        @Override
        public void run() {
            this.running = true;
            // Always start in a disconnected state
            state = new DisconnectedState();
            while(running) {
                state.doAction();
            }
        }

        public boolean isRunning() {
            return running;
        }

        public void shutdown() {
            this.running = false;
        }


        /**
         * Pings a the service that implementers care to monitor
         * @return <code>true</code> if the service was found to be alive, otherwise <code>false</code>.
         */
        public abstract boolean pingService();

        /**
         * Represents the connected state of the heartbeat. This state will be active if the
         * heartbeat has detected the services are available.
         *
         * Created by dariosd
         */
        private class ConnectedState implements IHeartbeatState {
            @Override
            public void doAction() {
                while(running && connected) {
                    if (pingService()) {
                        // Reset the attempt counter
                        pingAttempts = 0;
                        try {
                            // This indicates the service is available. Sleep for some time
                            Thread.sleep(pingRate);
                        } catch (InterruptedException e) {
                            Log.severe( e.getMessage());
                        }
                    } else {
                        // If connection is lost, attempt to reestablish connection until max attempts
                        pingAttempts++;
                        if (pingAttempts >= maxRetries) {
                            Log.warning("Max ping attempts reached. Services will be shut down until heartbeat " +
                                    "reestablishes connection.");
                            Log.info("Service not detected! Changing state to Disconnected.");
                            fireServiceDiedEvent(new HeartbeatEvent(this));
                            // Once max retry is reached, stop all services and continue to ping
                            connected = false;
                            // Update state to be disconnected
                            state = new DisconnectedState();
                        }
                    }
                }
            }
        }

        /**
         * Represents the disconnected state of the heart beat. This state will be active if the
         * heartbeat has detected the services are unavailable.
         *
         * Created by dariosd
         */
        private class DisconnectedState implements IHeartbeatState {
            private int shortPing = 1000;
            private int longPing = 60000;
            private int deadlineLimit = 60000;

            @Override
            public void doAction() {
                // Me love you
                long time = System.currentTimeMillis();
                long deadline = time + deadlineLimit; // Add one minute to the current time
                while(running && !connected) {
                    if (pingService()) {
                        Log.info("Service is alive! Changing state to Connected.");
                        connected = true;
                        state = new ConnectedState();
                        fireServiceAliveEvent(new HeartbeatEvent(this));
                    } else {
                        if (hasTimedOut(deadline)) {
                            try {
                                Log.info("Service not detected. Pinging slowly...");
                                Thread.sleep(longPing);
                            } catch (InterruptedException e) {
                                Log.severe(e.getMessage());
                            }
                        } else {
                            try {
                                Log.info("Service not detected. Continuing to ping...");
                                Thread.sleep(shortPing);
                            } catch (InterruptedException e) {
                                Log.severe(e.getMessage());
                            }
                        }
                    }
                }
            }

            private boolean hasTimedOut(long deadline) {
                long currTime = System.currentTimeMillis();
                // If the current time is after the deadline time, this has timed out
                return currTime > deadline;
            }
        }
    }

    protected void fireServiceAliveEvent(HeartbeatEvent event) {
        Object[] listenerList = listeners.getListenerList();
        for (int i = listenerList.length - 2; i >= 0; i -= 2) {
            if (listenerList[i] == IHeartbeatListener.class) {
                ((IHeartbeatListener)listenerList[i + 1]).serviceAlive(event);
            }
        }
    }

    protected void fireServiceDiedEvent(HeartbeatEvent event) {
        Object[] listenerList = listeners.getListenerList();
        for (int i = listenerList.length - 2; i >= 0; i -= 2) {
            if (listenerList[i] == IHeartbeatListener.class) {
                ((IHeartbeatListener)listenerList[i + 1]).serviceDied(event);
            }
        }
    }
}
