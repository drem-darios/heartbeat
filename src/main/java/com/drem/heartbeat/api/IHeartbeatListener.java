package com.drem.heartbeat.api;

import java.util.EventListener;

import com.drem.heartbeat.event.HeartbeatEvent;

/**
 * Created by dariosd on 2/24/2015.
 */
public interface IHeartbeatListener extends EventListener {

    public void serviceAlive(HeartbeatEvent event);
    public void serviceDied(HeartbeatEvent event);
}
