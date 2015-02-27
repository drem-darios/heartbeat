package com.drem.heartbeat.api;

import java.io.Serializable;

/**
 * Created by dariosd on 2/24/2015.
 */
public interface IHeartbeat extends Serializable {
    public void start();
    public void stop();
    public boolean isAlive();
    public void registerListener(IHeartbeatListener listener);
    public void deregisterListener(IHeartbeatListener listener);
}
