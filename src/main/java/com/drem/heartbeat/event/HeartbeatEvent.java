package com.drem.heartbeat.event;

import java.util.EventObject;

/**
 * Created by dariosd on 2/24/2015.
 */
public class HeartbeatEvent extends EventObject {
	private static final long serialVersionUID = -4599055149797017047L;

	/**
     * Constructs a new instance of this class.
     *
     * @param source the object which fired the event.
     */
    public HeartbeatEvent(Object source) {
        super(source);
    }
}
